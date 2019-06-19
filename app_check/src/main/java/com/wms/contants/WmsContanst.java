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
     * 临期物资清单
     */
    public static final String OUTTIME_INVENTORY_SUBMIT = HOST + "/materialCheck/getAdvanceCheckList";

}
