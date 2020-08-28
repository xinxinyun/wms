package com.bean;

/**
 * 盘点信息实体
 */
public class MaterialOnSchedule extends MaterialInfo {

    private boolean isInventory = false;

    private String rfidCode;

    private String produceDateStr;

    private String expireDateStr;

    public boolean isInventory() {
        return isInventory;
    }

    public void setInventory(boolean inventory) {
        isInventory = inventory;
    }

    public String getRfidCode() {
        return rfidCode;
    }

    public void setRfidCode(String rfidCode) {
        this.rfidCode = rfidCode;
    }

    public String getProduceDateStr() {
        return produceDateStr;
    }

    public void setProduceDateStr(String produceDateStr) {
        this.produceDateStr = produceDateStr;
    }

    public String getExpireDateStr() {
        return expireDateStr;
    }

    public void setExpireDateStr(String expireDateStr) {
        this.expireDateStr = expireDateStr;
    }
}
