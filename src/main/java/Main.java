import org.apache.commons.cli.*;

public class Main {

    public static void main(final String[] args) {
        final Options options = new Options();

        final Option directoryArgOption = new Option("t", "directory", true, "Path of the target directory");
        directoryArgOption.setRequired(true);
        options.addOption(directoryArgOption);

        final Option versionOption = new Option("v", "version", true, "Target MCP version");
        versionOption.setRequired(true);
        options.addOption(versionOption);

        final Option projectName = new Option("n", "name", true, "Created project's name");
        projectName.setRequired(true);
        options.addOption(projectName);

        final Option projectGroup = new Option("g", "group", true, "Created project's group");
        projectGroup.setRequired(true);
        options.addOption(projectGroup);

        final Option projectVersion = new Option("w", "projectVersion", true, "Created project's version");
        projectVersion.setRequired(true);
        options.addOption(projectVersion);

        final CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("mcp-generator", options);
            System.exit(1);
            return;
        }

        final Program program = new Program(cmd.getOptionValue("name"), cmd.getOptionValue("group"), cmd.getOptionValue("projectVersion"), cmd.getOptionValue("directory"), cmd.getOptionValue("version"));
        program.run();
    }

}
