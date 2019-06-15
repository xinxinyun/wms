package com.wms.contants;

/**
 * 常量定义参数
 */
public interface WmsContanst {

    /**
     * 主机IP
     */
    public static final String HOST = "http://140.143.58.93:8888/api";

    /**
     * 物资盘点清单
     */
    public static final String STORGE_MATERIALINFL = HOST + "/materialCheck/getInventoryRfidList";

    /**
     * 仓储库存物资清单盘点结果提交
     */
    public static final String STORGE_MATERIALINFL_INVENTORY_SUBMIT = HOST + "/materialCheck/commitInventoryCheckRfid";

    /**
     * 销售库存物资清单
     */
    public static final String SALE_MATERIALINFL = HOST + "/materialCheck/getInventoryRfidList";

    /**
     * 销售库存物资清单盘点结果提交
     */
    public static final String SALE_MATERIALINFL_INVENTORY_SUBMIT = HOST + "/api/getMaa";

    /**
     * 临期物资清单
     */
    public static final String OUTTIME_INVENTORY_SUBMIT = HOST + "/materialCheck/getAdvanceCheckList";

    /**
     * 波特率
     */
    public static final int baud = 115200;

    /**
     * 串口号
     */
    public static final String TTYS1 = "/dev/ttyS4";

    public static final String CONTENT_TYPE = "application/json; charset=utf-8";


}
