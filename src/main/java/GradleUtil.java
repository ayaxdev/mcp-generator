public class GradleUtil {

    private static final String BUILD_TEMPLATE = """
            plugins {
                id 'java'
            }
            
            group = '%s'
            version = '%s'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation fileTree(dir: 'libraries', include: '*.jar')
            }
            """;

    private static final String SETTINGS_TEMPLATE = """
            rootProject.name = '%s'
            """;

    public static String getBuild(final String group, final String version) {
        return String.format(BUILD_TEMPLATE, group, version);
    }

    public static String getSettings(final String name) {
        return String.format(SETTINGS_TEMPLATE, name);
    }

}
