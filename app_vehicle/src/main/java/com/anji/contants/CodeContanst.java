package com.anji.contants;

import java.util.HashMap;

/**
 * 版权：xx公司 版权所有
 *
 * @author 周宇
 * 版本：1.0
 * 创建日期：${date}${hour}
 * 描述：MainActivity
 */
public interface CodeContanst {

    /**
     * 返回码常量
     */
    public static final HashMap<String, String> codeMap = new HashMap<String, String>() {
        {
            put("0000", "存入盘库数据成功");
            put("1300", "客户不存在或被禁用");
            put("1303", "没有该接口的访问权限");
            put("1304", "reqData参数json格式错误");
            put("1305", "传入参数为空或格式错误");
            put("4001", "数据签名错误");
            put("9999", "系统异常");
        }
    };
}
