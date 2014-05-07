package com.Guaidaodl.Client;

import java.nio.ByteBuffer;

/**
 * 各种基本类型与byte[]的互相转换
 */
public class TypeConvert {

    /**
     * double转换byte[]
     */
    public static byte[] d2b(double d) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(d);
        return bytes;
    }
    /**
     * byte[]转换成double
     */
    public static double b2d(byte[] b) {
        return ByteBuffer.wrap(b).getDouble();
    }
    /**
     * 将整数转换成对应的byte[]
     */
    public static byte[] i2b(int i) {
        byte[] bytes = new byte[4];
        ByteBuffer.wrap(bytes).putInt(i);
        return bytes;
    }
    /**
     * 将byte[] 转换成相应的整数
     */
    public static int b2i(byte[] b) {
        return ByteBuffer.wrap(b).getInt();
    }

}
