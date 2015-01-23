package Util;

public class SocketEncoder {
	
	public static byte[] encode(byte[] buffer, int off, int len) {
        for (int i = 0; i < len; i++) {
            buffer[i] = (byte)(255 - (int)((int)buffer[i]&0xFF));
        }
        return buffer;
    }

    public static byte[] decode(byte[] buffer, int off, int len) {
        for (int i = 0; i < len; i++) {
            buffer[i] = (byte)(255 - (int)((int)buffer[i]&0xFF));
        }
        return buffer;
    }
}