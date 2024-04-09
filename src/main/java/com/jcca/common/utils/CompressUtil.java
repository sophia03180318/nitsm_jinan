package com.jcca.common.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @ClassName CompressUtil
 * @Description 字符串压缩与解压缩
 * @Date 2020/4/18 13:51
 * @Author hanwone
 */
@Slf4j
public class CompressUtil {
    /***
     * 压缩GZip
     *
     * @param content 用于压缩的字符串
     * @return 压缩后的16进制字符串
     */
    public static String gZip(String content) {
        Assert.notNull(content, "用于压缩的字符串不能为空");
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(content.getBytes());
            gzip.finish();
            gzip.close();
            b = bos.toByteArray();
            bos.close();
        } catch (Exception ex) {
            log.error("压缩异常:{}", ex.getMessage());
        }
        return new String(encodeHex(b));
    }

    /***
     * 解压GZip
     *
     * @param content 要解压的16进制字符串
     * @return 解压后的原字符串
     */
    public static String unGZip(String content) {
        Assert.notNull(content, "要解压16进制字符串不能为空");
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(decodeHex(content.toCharArray()));
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();
        } catch (Exception ex) {
            log.error("解压缩异常:{}", ex.getMessage());
        }
        return new String(b);
    }

    private static final char[] DIGITS_HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F'};

    protected static char[] encodeHex(byte[] data) {
        int l = data.length;
        char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_HEX[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_HEX[0x0F & data[i]];
        }
        return out;
    }

    protected static byte[] decodeHex(char[] data) {
        int len = data.length;
        if ((len & 0x01) != 0) {
            throw new RuntimeException("字符个数应该为偶数");
        }
        byte[] out = new byte[len >> 1];
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f |= toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }
        return out;
    }

    protected static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }
}
