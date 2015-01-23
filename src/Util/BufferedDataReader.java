package Util;

import java.lang.Exception;
import java.lang.Math;
import java.util.Arrays;
import java.io.InputStream;
import java.io.IOException;
import Util.SocketEncoder;

public class BufferedDataReader {

    public static final int BUFSIZE = 1024;
    
    int bufSize;
    byte[] buffer;
    SocketEncoder encoder;
    InputStream ins;
    int head = 0;
    int tail = 0;
    int dataLen = 0; // effective data length

    /**
     * constructors
     * @param bufSize buffer size
     * @param encoder data encoder
     */
    public BufferedDataReader(InputStream ins, int bufSize, SocketEncoder encoder) {
        this.ins = ins;
        this.bufSize = bufSize;
        this.encoder = encoder;
        buffer = new byte[bufSize];
    }

    public BufferedDataReader(InputStream ins) {
        this(ins, BUFSIZE, (SocketEncoder)null);
    }

    public BufferedDataReader(InputStream ins, int bufSize) {
        this(ins, bufSize, (SocketEncoder)null);
    }

    public BufferedDataReader(InputStream ins,SocketEncoder encoder) {
        this(ins, BUFSIZE, (SocketEncoder)encoder);
    }


    /**
     * read 1 byte
     * @return byte
     */
    public byte read() throws IOException {
        byte b = buffer[tail++];
        dataLen--;
        int re = 0;
        if (dataLen == 0 && (re = refill()) <= 0) {
            return -1;
        }
        return b;
    }

    /**
     * read bytes to buf
     * @param buf user buf where data is copied to
     * @param off offset of user buf
     * @param len read length
     * @return actual length read
     */
    public int read(byte[] buf, int off, int len) throws Exception, IOException {
        if (off + len >= buf.length) {
            throw new Exception("Exceeds ArrayBound Exception" + 
                             off + " + " + len + " > " + buf.length);
        }
        int readLenLeft = len;
        while (readLenLeft > 0) {
            int copyLen = Math.min(dataLen, readLenLeft);
            System.arraycopy(buffer, tail, buf, off, copyLen);
            off += copyLen;
            tail += copyLen;
            dataLen -= copyLen;
            readLenLeft -= copyLen;
            int re = 0;
            if (dataLen == 0 && (re = refill()) < 0) {
                break;
            }
        }
        return len - readLenLeft;
    }

    /**
     * read one line
     * @return String line
     */
    public String readLine() throws IOException {
        StringBuffer lineBuf = new StringBuffer();
        int index = tail;
        
        while (true) {
            if ((char)buffer[tail] == '\n') {
                tail++;
                dataLen -= 1;
                if (dataLen == 0) {
                    refill();
                }
                break;
            }
            else {
                lineBuf.append((char)buffer[tail++]);
                dataLen--;
                // if stream end has been reached without a complete line, return null
                int re = 0;
                if (dataLen == 0 && (re = refill()) < 0) {
                    return null;
                }
            }
        }

        // if end with \n and the previous char is an 'r' that has been saved
        if (lineBuf.length() > 0 && lineBuf.charAt(lineBuf.length()-1) == '\r') {
            lineBuf.setLength(lineBuf.length() - 1);
        }
        return lineBuf.toString();
    }

    /**
     * close the reader
     * @raturn void
     */
    public void close() throws IOException {
        ins.close();
    }

    /**
     * reset and refill the buffer
     * @return number of dataLen or -1 if stream end has been reached
     */
    private int refill() throws IOException {
        head = 0;
        tail = 0;
        int readLen = ins.read(buffer, 0, bufSize);
        if (readLen <= 0) {
            dataLen = 0;
        }
        else {
            dataLen = readLen;
        }
        return readLen;
    }

}