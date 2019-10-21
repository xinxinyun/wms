package com.anji.bean;

/**
 * @author 周宇
 * 版本：1.0
 * 创建日期：20191017
 * 描述：CheckPlan
 * warehouseId:仓库ID
 * planId:计划ID
 * rfSwitch:rfid开关状态
 */
public class CheckPlan {

    /**
     * 盘点计划id
     */
    private Long planId;

    /**
     * 是否盘点
     */
    private Boolean rfSwitch;

    /**
     * 盘点日期
     */
    private Integer warehouseId;

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Boolean getRfSwitch() {
        return rfSwitch;
    }

    public void setRfSwitch(Boolean rfSwitch) {
        this.rfSwitch = rfSwitch;
    }

    public Integer getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Integer warehouseId) {
        this.warehouseId = warehouseId;
    }
}
