public class Logger {

    public static void info(final String message) {
        System.out.printf("[!] %s%n", message);
    }

    public static void success(final String message) {
        System.out.printf("[✓] %s%n", message);
    }

    public static void error(final String message) {
        System.err.printf("[X] %s%n", message);
    }

    public static void logExit(final String message, final Exception e) {
        System.err.println(e.getMessage());
        Logger.error(String.format("%s See exception stacktrace for details. Exiting.", message));
    }

}
