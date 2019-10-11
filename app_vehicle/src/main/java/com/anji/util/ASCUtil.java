package com.anji.util;

/**
 * 版权：xx公司 版权所有
 *
 * @author 周宇
 * 版本：1.0
 * 创建日期：${date}${hour}
 * 描述：ASCUtil
 */
public class ASCUtil {

    private static int ascNum;
    private static char strChar;

    /**
     * 字符转ASC
     *
     * @param st
     * @return
     */
    public static int getAsc(String st) {
        byte[] gc = st.getBytes();
        ascNum = (int) gc[0];
        return ascNum;
    }

    /**
     * ASC转字符
     *
     * @param backnum
     * @return
     */
    public static char backchar(int backnum) {
        strChar = (char) backnum;
        return strChar;
    }


    /**
     * 十二位的十六进制转换为十七位，十六进制存储的为ASC码，需转为相应的字符才能识别
     * @param ascCode
     * @return
     */
    public static String str12to17(String ascCode) {

        int[] sz17 = new int[17];

        String[] sz12 = ascCode.split(" ");
        int[] sz12Int = new int[sz12.length];
        for (int i = 0; i < sz12.length; i++) {
            sz12Int[i] = Integer.parseInt(sz12[i], 16);
        }

        sz17[16] = sz12Int[11] & 0x0f;
        sz17[15] = (sz12Int[11] & 0xf0) >> 4;
        sz17[14] = sz12Int[10] & 0x0f;
        sz17[13] = (sz12Int[10] & 0xf0) >> 4;
        sz17[12] = sz12Int[9] & 0x3f;

        sz17[11] = ((sz12Int[9] & 0xc0) >> 6) | ((sz12Int[8] & 0x0f) << 2);
        sz17[10] = ((sz12Int[8] & 0xf0) >> 4) | ((sz12Int[7] & 0x03) << 4);

        sz17[9] = (sz12Int[7] & 0xfc) >> 2;
        sz17[8] = sz12Int[6] & 0x3f;

        sz17[7] = ((sz12Int[6] & 0xc0) >> 6) | ((sz12Int[5] & 0x0f) << 2);
        sz17[6] = ((sz12Int[5] & 0xf0) >> 4) | ((sz12Int[4] & 0x03) << 4);

        sz17[5] = (sz12Int[4] & 0xfc) >> 2;
        sz17[4] = sz12Int[3] & 0x3f;

        sz17[3] = ((sz12Int[3] & 0xc0) >> 6) | ((sz12Int[2] & 0x0f) << 2);
        sz17[2] = ((sz12Int[2] & 0xf0) >> 4) | ((sz12Int[1] & 0x03) << 4);

        sz17[1] = (sz12Int[1] & 0xfc) >> 2;
        sz17[0] = sz12Int[0] & 0x3f;

        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < 17; j++) {
            sz17[j] = sz17[j] + 0x30;
            sb.append(backchar(sz17[j]));
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(str12to17("1C 8E 64 D2 09 E8 06 61 E0 02 93 14"));
    }
}
