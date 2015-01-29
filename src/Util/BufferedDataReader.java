package Util;

import java.lang.Exception;
import java.lang.NullPointerException;
import java.lang.Math;
import java.util.ArrayList;
import java.nio.charset.Charset;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import Util.SocketEncoder;

public class BufferedDataReader {

    public static final int BUFSIZE = 1024;
    public static final int MAX_BUFSIZE = 4194302;
    
    int bufSize;
    byte[] buffer;
    SocketEncoder encoder = null;
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
        if (bufSize < 0 || bufSize > MAX_BUFSIZE) {
            bufSize = 1024;
        }
        else {
            this.bufSize = bufSize;
        }

        this.ins = ins;
        this.encoder = encoder;
        buffer = new byte[bufSize];
    }

    public BufferedDataReader(InputStream ins) {
        this(ins, BUFSIZE, (SocketEncoder)null);
    }

    public BufferedDataReader(InputStream ins, int bufSize) {
        this(ins, bufSize, (SocketEncoder)null);
    }

    public BufferedDataReader(InputStream ins, SocketEncoder encoder) {
        this(ins, BUFSIZE, (SocketEncoder)encoder);
    }


    /**
     * read 1 byte
     * @return byte
     */
    public int read() throws IOException {
        int re = 0;
        if (dataLen == 0 && (re = refill()) <= 0) {
            return -1;
        }

        byte b = buffer[tail++];
        dataLen--;
        return (int)(((int)b)&0xFF);
    }

    /**
     * read bytes to buf
     * @param buf user buf where data is copied to
     * @param off offset of user buf
     * @param len read length
     * @return actual length read
     */
    public int read(byte[] buf, int off, int len) throws Exception, IOException {
        
        checkArgs(buf, off, len);
        if (len == 0) {
            return 0;
        }
        
        int readLenLeft = len;
        while (readLenLeft > 0) {
            if (dataLen <= 0) {
                if (readLenLeft >= bufSize) {
                    readLenLeft -= ins.read(buf, off, readLenLeft);
                    break;
                }
                else {
                    int re = 0;
                    if ((re = refill()) < 0) {
                        return readLenLeft == len? -1 : len - readLenLeft;
                    }
                }
            }

            int copyLen = Math.min(dataLen, readLenLeft);
            System.arraycopy(buffer, tail, buf, off, copyLen);
            off += copyLen;
            tail += copyLen;
            dataLen -= copyLen;
            readLenLeft -= copyLen;
            if (readLenLeft == 0) {
                break;
            }
            if (ins != null && ins.available() <= 0) {
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
        int re = 0;
        if (dataLen == 0 && (re = refill()) < 0) {
            return null;
        }

        StringBuffer lineBuf = new StringBuffer();
        boolean eof = false;
        while (true) {
            if ((char)buffer[tail] == '\n') {
                tail++;
                dataLen -= 1;
                break;
            }
            else {
                lineBuf.append((char)buffer[tail++]);
                dataLen--;
                if (dataLen == 0 && (re = refill()) < 0) {
                    eof = true;
                    break;
                }
            }
        }

        // if end with \n and the previous char is an 'r' that has been saved
        if (!eof && lineBuf.length() > 0 && lineBuf.charAt(lineBuf.length()-1) == '\r') {
            lineBuf.setLength(lineBuf.length() - 1);
        }
        return new String(lineBuf);
        //return new String(lineBuf.getBytes(), Charset.forName("UTF-8"));
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
        //System.out.println("available: " + ins.available());
        int readLen = ins.read(buffer, 0, bufSize);
        head += readLen;
        if (readLen <= 0) {
            dataLen = 0;
        }
        else {
            dataLen = readLen;
            if (encoder != null) {
                encoder.decode(buffer, 0, dataLen);
            }
        }
        return readLen;
    }

    /**
     * check arguments of read(byte[], int, int)
     * @param buf user buf where data is copied to
     * @param off offset of user buf
     * @param len read length
     */
    private void checkArgs(byte[] buf, int off, int len) throws Exception, IOException {
        if (buf == null) {
            throw new NullPointerException();
        }
        if (off + len > buf.length) {
            throw new Exception("IndexOutOfBoundsException: " + 
                                 off + " + " + len + " > " + buf.length);
        }
        if (len < 0 || off < 0) {
            throw new Exception("IndexOutOfBoundsException: " + ": len = " + len + " < 0");
        }
        if (off < 0) {
            throw new Exception("IndexOutOfBoundsException: " + ": off = " + off + " < 0");
        }
    }

    /**
     * compare a byte buffer with a char buffer
     * @return number of dataLen or -1 if stream end has been reached
     */
    private static boolean dataCompare(byte[] buf1, int off1, char[] buf2, int off2, int len) throws Exception {
        if (off1 + len >= buf1.length) {
            throw new Exception("Exceeds ArrayBound Exception Byte Buffer " + 
                                off1 + " + " + len + " > " + buf1.length);
        }
        if (off2 + len >= buf2.length) {
            throw new Exception("Exceeds ArrayBound Exception Char Buffer " + 
                                off2 + " + " + len + " > " + buf2.length);
        }
        
        for (int i = 0; i < len; i++) {
            if (buf1[off1+i] != (byte)buf2[off2+i]) {
                return false;
            }
        }
        return true;
    }


    public static void testReadLine(String path) throws Exception {
        File folder = new File(path);
        ArrayList<String> filesList = new ArrayList<String>();
        for (File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                //System.out.println(fileEntry.getName());
                filesList.add(fileEntry.getPath());
            }
        }

        System.out.println("\nStart Testing readLine() ...");
        for (String fileName: filesList) {
            //String fileName = "src/Util/SocketEncoder.java";
            for (int size = 1; size < 10000; size++) {
                InputStream ins1 = new FileInputStream(fileName);
                InputStream ins2 = new FileInputStream(fileName);
                BufferedReader br1 = new BufferedReader(new InputStreamReader(ins1));
                BufferedDataReader br2 = new BufferedDataReader(ins2, size);
                String line1 = null, line2 = null;
                while ((line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
                    if (!line1.equals(line2)) {
                        System.err.println("Error: File " + fileName + ", BUF Size = " + size);
                        return;
                    }
                }
                ins1.close();
                ins2.close();
            }
        }
        System.err.println("Passed ^_^");
    }

    public static void testReadSingle(String path) throws Exception {
        File folder = new File(path);
        ArrayList<String> filesList = new ArrayList<String>();
        for (File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                //System.out.println(fileEntry.getName());
                filesList.add(fileEntry.getPath());
            }
        }

        System.out.println("\nStart Testing read() ...");
        for (String fileName: filesList) {
            for (int size = 1; size < 10000; size++) {
                //String fileName = "src/Util/SocketEncoder.java";
                InputStream ins1 = new FileInputStream(fileName);
                InputStream ins2 = new FileInputStream(fileName);
                BufferedReader br1 = new BufferedReader(new InputStreamReader(ins1));
                BufferedDataReader br2 = new BufferedDataReader(ins2, size);
                byte c1 = -1, c2 = -1;
                int re1 = -1, re2 = -1;
                while (true) {
                    re1 = br1.read();
                    re2 = br2.read();
                    c1 = (byte)re1;
                    c2 = (byte)re2;
                    if (re1 <0 || re2 < 0) {
                        break;
                    }
                    if (c1 != c2) {
                        System.err.println("Error: File " + fileName + ", BUF Size = " + size);
                        System.out.println("c1 = " + c1 + ", c2 = " + c2);
                        System.out.println("re1 = " + re1 + ", re2 = " + re2);
                        return;
                    }
                }
                if (re1 != -1 || re1 != -1) {
                    System.err.println("Error: End of File " + fileName + ", BUF Size = " + size);
                    System.out.println("c1 = " + c1 + ", c2 = " + c2);
                    System.out.println("re1 = " + re1 + ", re2 = " + re2);
                    return;
                }
                ins1.close();
                ins2.close();
            }
        }
        System.err.println("Passed ^_^");
    }


    public static void testReadBatch(String path) throws Exception {
        File folder = new File(path);
        ArrayList<String> filesList = new ArrayList<String>();
        for (File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                //System.out.println(fileEntry.getName());
                filesList.add(fileEntry.getPath());
            }
        }

        System.out.println("\nStart Testing read(byte[], int, int) ...");
        for (String fileName: filesList) {
            //String fileName = "src/Util/SocketEncoder.java";
            char[] buf1 = new char[2048];
            byte[] buf2 = new byte[2048];
            int readLen1 = 0, readLen2 = 0;     
            for (int size = 1; size < 10000; size++) {
                InputStream ins1 = new FileInputStream(fileName);
                InputStream ins2 = new FileInputStream(fileName);
                BufferedReader br1 = new BufferedReader(new InputStreamReader(ins1));
                BufferedDataReader br2 = new BufferedDataReader(ins2, size);
                while (true) {
                    int readSize = (int)(1.0 * 2048 * Math.random());
                    readLen1 = br1.read(buf1, 0, readSize);
                    readLen2 = br2.read(buf2, 0, readSize);
                    if (readLen1 != readLen2) {
                        System.err.println("***Len Error***: File " + fileName + ", BUF Size = " + size);
                        System.out.println("readLen1 = " + readLen1 + ", readLen2 = " + readLen2);
                        return;
                    }
                    if (!BufferedDataReader.dataCompare(buf2, 0, buf1, 0, readLen1)) {
                        System.err.println("***Data Error***: File " + fileName + ", BUF Size = " + size);
                        return;
                    }
                    if (readLen1 < 0 || readLen2 < 0) {
                        break;
                    }
                }
                if (readLen1 != -1 || readLen2 != -1) {
                    System.err.println("***Error***: End of File " + fileName + ", BUF Size = " + size);
                    System.out.println("readLen1 = " + readLen1 + ", readLen2 = " + readLen2);
                    return;
                }
                ins1.close();
                ins2.close();
            }
        }
        System.err.println("Passed ^_^");
    }

    /**
     * main funciton for test
     */
    public static void main(String args[]) throws Exception {

        String path = "src/Server/";
        //testReadLine(path);
        //testReadSingle(path);
        testReadBatch(path);

        return;
    }

}