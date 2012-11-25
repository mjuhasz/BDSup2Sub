package bdsup2sub.utils;

public class PlatformUtils {

    private static final String OS_NAME = System.getProperty("os.name");

    public static boolean isWindows() {
        return OS_NAME.toLowerCase().contains("windows");
    }

    public static boolean isMac() {
        return OS_NAME.toLowerCase().contains("mac");
    }

    public static boolean isLinux() {
        return OS_NAME.toLowerCase().contains("linux");
    }
}
