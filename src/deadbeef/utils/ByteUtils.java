package deadbeef.utils;

public final class ByteUtils {

    private ByteUtils() {
    }

    public static int getByte(byte[] buffer, int index) {
        return buffer[index] & 0xff;
    }

    public static void setByte(byte[] buffer, int index, int val) {
        buffer[index] = (byte)(val);
    }

    public static int getWord(byte[] buffer, int index) {
        return (buffer[index+1] & 0xff) | ((buffer[index] & 0xff) << 8);
    }

    public static void setWord(byte[] buffer, int index, int val) {
        buffer[index]     = (byte)(val >> 8);
        buffer[index + 1] = (byte)(val);
    }

    public static void setDWord(byte[] buffer, int index, int val) {
        buffer[index]     = (byte)(val >> 24);
        buffer[index + 1] = (byte)(val >> 16);
        buffer[index + 2] = (byte)(val >> 8);
        buffer[index + 3] = (byte)(val);
    }
}
