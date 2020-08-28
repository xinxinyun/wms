package com.bean;

public class MaterialInfo {

    private Integer id;

    private String materialName;

    private String materialBarcode;

    private String materialMode;

    private String fridCode;

    private Integer accountQuantity;

    private Integer checkQuantity;

    private String isCompleted;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialBarcode() {
        return materialBarcode;
    }

    public void setMaterialBarcode(String materialBarcode) {
        this.materialBarcode = materialBarcode;
    }

    public String getMaterialMode() {
        return materialMode;
    }

    public void setMaterialMode(String materialMode) {
        this.materialMode = materialMode;
    }

    public String getFridCode() {
        return fridCode;
    }

    public void setFridCode(String fridCode) {
        this.fridCode = fridCode;
    }

    public Integer getAccountQuantity() {
        return accountQuantity;
    }

    public void setAccountQuantity(Integer accountQuantity) {
        this.accountQuantity = accountQuantity;
    }

    public Integer getCheckQuantity() {
        return checkQuantity;
    }

    public void setCheckQuantity(Integer checkQuantity) {
        this.checkQuantity = checkQuantity;
    }

    public String getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(String isCompleted) {
        this.isCompleted = isCompleted;
    }
}
