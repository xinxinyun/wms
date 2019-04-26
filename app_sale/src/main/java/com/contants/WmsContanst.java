package com.contants;

/**
 * 常量定义参数
 */
public interface WmsContanst {

    /**
     * 主机IP
     */
    public static final String HOST = "http://140.143.58.93:8888/api";

    /**
     * 物资销售出库
     */
    public static final String STORGE_MATERIALINFL_INVENTORY_SUBMIT = HOST + "/materialOut/materialSellOut";

    /**
     * 波特率
     */
    public static final int baud = 115200;

    /**
     * 串口号
     */
    public static final String TTYS1 = "/dev/ttyS4";

    public static final String TTYMXC2 = "/dev/ttymxc2";

}
