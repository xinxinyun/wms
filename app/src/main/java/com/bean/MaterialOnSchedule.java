package com.bean;

/**
 * 盘点信息实体
 */
public class MaterialOnSchedule extends  MaterialInfo{

   private boolean isInventory=false;

   private String rfidCode;

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
}
