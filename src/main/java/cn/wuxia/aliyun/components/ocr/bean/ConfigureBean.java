/*
* Created on :2017年3月2日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ocr.bean;

import java.io.Serializable;
import java.util.Map;

import cn.wuxia.aliyun.components.ocr.enums.DataTypeEnum;
import cn.wuxia.common.util.JsonUtil;

public class ConfigureBean implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -8657580595295793093L;

    DataTypeEnum dataType;

    Map<String, String> dataValue;

    public ConfigureBean() {
    }

    public ConfigureBean(Map<String, String> dataValue) {
        this.dataType = DataTypeEnum.String;
        this.dataValue = dataValue;
    }

    public DataTypeEnum getDataType() {
        return dataType;
    }

    public void setDataType(DataTypeEnum dataType) {
        this.dataType = dataType;
    }

    public Map<String, String> getDataValue() {
        return dataValue;
    }

    public void setDataValue(Map<String, String> dataValue) {
        this.dataValue = dataValue;
    }
}
