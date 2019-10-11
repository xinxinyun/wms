package com.anji.contants;

/**
 * 常量定义参数
 */
public interface VehicleContanst {

    /**
     * 主机IP
     */
    public static final String HOST = "http://140.143.58.93:8888/api";

    /**
     * 车辆盘点结果提交
     */
    public static final String VEHICLE_INVENTORY_ACCESSDATA = HOST + "/business/ " +
            "inventoryCar/accessData";

    /**
     * 波特率
     */
    public static final int baud = 115200;

    /**
     * 过门程序串口号
     */
    public static final String TTYMXC2 = "/dev/ttymxc2";

    public static final String TTYS1 = "/dev/ttyS4";

}
