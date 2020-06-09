package com.anji.contants;

/**
 * 常量定义参数
 */
public interface VehicleContanst {

    /**
     * 主机IP
     */
    String HOST = "http://visp.anji-logistics.com/api-b";

    /**
     * 车辆盘点结果提交
     */
    String VEHICLE_INVENTORY_ACCESSDATA = HOST + "/business/service/inventory/detail" +
            "/receiveInventoryDetail";

    /**
     * 波特率
     */
    int baud = 115200;

    /**
     * 过门程序串口号
     */
    String TTYMXC2 = "/dev/ttymxc2";

    //public static final String TTYS1 = "/dev/ttyS4";

    /**
     * 接口调用身份凭证
     */
    String IDENGITY = "uid=53&key=e4fae1d7a2bec9b99931828148584789";

    /**
     * 用户身份号
     */
    String USER_ID = "53";

    /**
     * WebSocket设备号
     */
    String DEVICE_ID = "SGM20200609C";

    /**
     * WebSocket连接地址
     */
    String WEBSOCKET_HOST_AND_PORT = "ws://visp.anji-logistics" +
            ".com/websocket/" + DEVICE_ID;

    /**
     * 每隔5秒进行一次对长连接的心跳检测
     */
    long HEART_BEAT_RATE = 5 * 1000;

}