/*
* Created on :2017年2月12日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ons.bean;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections4.MapUtils;
import javax.validation.constraints.NotBlank;

import cn.wuxia.common.util.reflection.BeanUtil;

public class ONSAccountBean {
    protected String AccessKey;

    protected String SecretKey;

    protected String ONSAddr;

    public ONSAccountBean() {
    }

    public ONSAccountBean(String ONSAddr, String AccessKey, String SecretKey) {
        this.ONSAddr = ONSAddr;
        this.AccessKey = AccessKey;
        this.SecretKey = SecretKey;
    }

    @NotBlank
    public String getAccessKey() {
        return AccessKey;
    }

    public void setAccessKey(String accessKey) {
        AccessKey = accessKey;
    }

    @NotBlank
    public String getSecretKey() {
        return SecretKey;
    }

    public void setSecretKey(String secretKey) {
        SecretKey = secretKey;
    }

    @NotBlank
    public String getONSAddr() {
        return ONSAddr;
    }

    public void setONSAddr(String oNSAddr) {
        ONSAddr = oNSAddr;
    }

    public Properties toProperties() {
        Properties properties = new Properties();
        properties.put("SecretKey", SecretKey);
        properties.put("ONSAddr", ONSAddr);
        properties.put("AccessKey", AccessKey);
        //        try {
        //            Map<String, Object> map = BeanUtil.beanToMap(this);
        //            properties = MapUtils.toProperties(map);
        //        } catch (IllegalAccessException | InvocationTargetException | IntrospectionException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        return properties;
    }

}
