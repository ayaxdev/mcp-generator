import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.file.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Program {

    private static final String[] VERSIONS = new String[] {"908", "918", "928", "931", "937", "940"};

    private final String projectNameArg;
    private final String projectGroupArg;
    private final String projectVersionArg;
    private final String directoryArg;
    private final String versionArg;

    public Program(String projectNameArg, String projectGroupArg, String projectVersionArg, String directoryArg, String versionArg) {
        this.projectNameArg = projectNameArg;
        this.projectGroupArg = projectGroupArg;
        this.projectVersionArg = projectVersionArg;
        this.directoryArg = directoryArg;
        this.versionArg = versionArg;
    }

    private Path targetDir, workingDir, zipFile, srcDir, jarsDir;

    public void run() {
        final long startTime = System.currentTimeMillis();

        boolean validVersion = false;
        for (final String version : VERSIONS) {
            if (versionArg.equals(version)) {
                validVersion = true;
                break;
            }
        }

        System.out.println("Preparing paths");

        if (!validVersion) {
            System.err.println("Invalid version");
            System.exit(1);
        }

        try {
            preparePaths();
        } catch (final Exception e) {
            System.err.printf("%s : %s%n", e.getClass().getSimpleName(), e.getMessage());
            System.err.println("Failed to prepare internal paths");
            System.exit(1);
        }

        try {
            prepareTargetDirectory();
        } catch (final Exception e) {
            System.err.printf("%s : %s%n", e.getClass().getSimpleName(), e.getMessage());
            System.err.println("Failed to prepare target directory");
            System.exit(1);
        }

        System.out.println("Beginning download & extraction");

        try {
            downloadMCP();
        } catch (final Exception e) {
            System.err.printf("%s : %s%n", e.getClass().getSimpleName(), e.getMessage());
            System.err.println("Failed to download and/or extract MCP");
            System.exit(-1);
        }

        System.out.println("Decompiling");

        try {
            decompile();
        } catch (final Exception e) {
            System.err.printf("%s : %s%n", e.getClass().getSimpleName(), e.getMessage());
            System.err.println("Failed to decompile");
            System.exit(-1);
        }

        System.out.println("Copying select files to project folder");

        try {
            copy();
        } catch (final Exception e) {
            System.err.printf("%s : %s%n", e.getClass().getSimpleName(), e.getMessage());
            System.err.println("Failed to copy all necessary files to the project folder");
            System.exit(-1);
        }

        final long endTime = System.currentTimeMillis();

        System.out.printf("Finished in %.1fs", (endTime - startTime) / 1000d);
    }

    private void preparePaths() throws Exception {
        workingDir = Files.createTempDirectory("Ajax's MCP Generator");
        zipFile = Paths.get(workingDir.toString(), "zipped");
        srcDir = Paths.get(workingDir.toString(), "src", "minecraft");
        jarsDir = Paths.get(workingDir.toString(), "jars");
    }

    private void prepareTargetDirectory() throws Exception {
        this.targetDir = Paths.get(this.directoryArg);
        final boolean exists = Files.exists(targetDir);

        if (exists && !PathUtils.isEmptyDirectory(targetDir)) {
            System.err.println("Target directory already exists and is not empty");
            System.exit(1);
        }

        if (!exists) {
            Files.createDirectories(targetDir);
        }
    }

    private void downloadMCP() throws Exception {
        NetUtils.download(String.format("http://www.modcoderpack.com/files/mcp%s.zip", versionArg), zipFile);

        try (final ZipFile zipFile = new ZipFile(this.zipFile.toFile())) {
            zipFile.extractAll(workingDir.toString());
        }
    }

    private void decompile() throws Exception {
        final ProcessBuilder pb = new ProcessBuilder("\"" + workingDir.toRealPath().toString() + "\\runtime\\bin\\python\\python_mcp\"", "runtime\\decompile.py", "%*");
        pb.directory(workingDir.toFile());
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        
        final Process process = pb.start();

        process.waitFor();

        final int exitCode = process.exitValue();

        if (exitCode != 0) {
            System.err.printf("Unknown error. Exit code: %d%n", exitCode);
            System.err.println("Failed to decompile");
            System.exit(-1);
        }
    }

    private void copy() {
        final Path runDir = Paths.get(targetDir.toString(), "run");
        final Path librariesDir = Paths.get(targetDir.toString(), "libraries");
        final Path srcDir = Paths.get(targetDir.toString(), "src", "main", "java");

        final Path buildFile = Paths.get(targetDir.toString(), "build.gradle");
        final Path settingsFile = Paths.get(targetDir.toString(), "settings.gradle");

        final Path tempJarsDir = Paths.get(runDir.toString(), "libraries");

        try {
            Files.createDirectories(runDir);
            Files.createDirectories(librariesDir);
            Files.createDirectories(srcDir);

            Files.createFile(buildFile);
            Files.writeString(buildFile, GradleUtil.getBuild(projectGroupArg, projectVersionArg));

            Files.createFile(settingsFile);
            Files.writeString(settingsFile, GradleUtil.getSettings(projectNameArg));
        } catch (final Exception e) {
            System.err.printf("%s : %s%n", e.getClass().getSimpleName(), e.getMessage());
            System.err.println("Failed to prepare project files");
            System.exit(1);
        }

        try {
            PathUtils.copyDirectory(this.srcDir, srcDir, PathUtils.EMPTY_COPY_OPTIONS);
            PathUtils.copyDirectory(this.jarsDir, runDir, PathUtils.EMPTY_COPY_OPTIONS);

            //PathUtils.copyDirectory(tempJarsDir, librariesDir, PathUtils.EMPTY_COPY_OPTIONS);
            Files.walk(tempJarsDir).forEach(path -> {
                if (Files.isRegularFile(path)) {
                    final String fileName = PathUtils.getFileNameString(path);
                    final Path filePath = Paths.get(librariesDir.toString(), fileName);
                    try {
                        Files.copy(path, filePath);
                    } catch (final Exception e) {
                        Logger.error(String.format("Failed to copy library '%s'. See stacktrace for details.", fileName));
                    }
                }
            });
            PathUtils.deleteDirectory(tempJarsDir);
        } catch (final Exception e) {
            System.err.printf("%s : %s%n", e.getClass().getSimpleName(), e.getMessage());
            System.err.println("Failed to copy decompiled code");
            System.exit(1);
        }
    }

}
