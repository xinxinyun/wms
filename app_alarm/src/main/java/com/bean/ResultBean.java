package com.bean;

import java.util.Map;

public class ResultBean {

    private Integer code;
    private String errorMsg;
    private Map<String,Boolean> data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Map<String,Boolean> getData() {
        return data;
    }

    public void setData(Map<String,Boolean> data) {
        this.data = data;
    }
}
