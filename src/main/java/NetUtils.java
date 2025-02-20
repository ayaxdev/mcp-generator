import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class NetUtils {

    public static void download(final String plainURL, final Path file) throws IOException {
        try {
            final URL url = URI.create(plainURL).toURL();
            byte[] bytes;

            try (final InputStream inputStream = url.openStream()) {
                bytes = inputStream.readAllBytes();
            }

            Files.write(file, bytes);
        } catch (final Exception e) {
            throw new IOException("Failed to download", e);
        }
    }

}
