/*
* Created on :2017年3月2日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ocr.enums;

/**
 * 
 * 请求的数据类型
 * @author songlin
 * @ Version : V<Ver.No> <2017年3月2日>
 */
public enum DataTypeEnum {
    Bool(1),

    Int32(10),

    Int64(20),

    Float(30),

    Double(40),

    String(50),

    DateTime(60);

    private int dataType;

    DataTypeEnum(int dataType) {
        this.dataType = dataType;
    }

    public int getDataType() {
        return dataType;
    }

    @Override
    public String toString() {
        return "" + this.dataType;
    }
}
