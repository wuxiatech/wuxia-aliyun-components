/*
* Created on :2017年1月20日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ons.bean;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;

import cn.wuxia.common.util.BytesUtil;
import cn.wuxia.common.util.EncodeUtils;

public class MessageBean implements Serializable {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    @NotNull
    Object object;

    //@NotNull
    String key;

    Long delayTime;

    Date startDate;

    public MessageBean() {
    }

    public MessageBean(Object body) {
        setObject(body);
    }

    public MessageBean(String key, Object body) {
        setKey(key);
        setObject(body);
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;

    }

    public byte[] getBody() {
        try {
            return BytesUtil.objectToBytes(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getDelayTime() {
        return delayTime;
    }

    // 延时时间单位为毫秒（ms），指定一个时刻，在这个时刻之后才能被消费，这个例子表示 3秒 后才能被消费
    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;

    }

    public Date getStartDate() {
        return startDate;
    }

    // 定时消息投递，设置投递的具体时间戳，单位毫秒例如2016-03-07 16:21:00投递
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

}
