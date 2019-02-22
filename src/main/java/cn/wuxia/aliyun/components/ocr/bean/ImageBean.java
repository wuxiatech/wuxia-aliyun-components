/*
* Created on :2017年3月2日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ocr.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import cn.wuxia.aliyun.components.ocr.enums.DataTypeEnum;
import cn.wuxia.aliyun.components.ocr.utils.DataValueFormatUtil;

public class ImageBean implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    DataTypeEnum dataType;

    String dataValue;

    public ImageBean() {

    }

    public ImageBean(InputStream stream) {
        this.dataType = DataTypeEnum.String;
        try {
            this.dataValue = DataValueFormatUtil.format(stream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public DataTypeEnum getDataType() {
        return dataType;
    }

    public void setDataType(DataTypeEnum dataType) {
        this.dataType = dataType;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

}
