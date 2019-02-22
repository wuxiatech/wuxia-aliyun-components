/*
* Created on :2017年2月12日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ons.producer.bean;

import cn.wuxia.aliyun.components.ons.bean.BasicONSBean;
import org.hibernate.validator.constraints.NotBlank;

public class ProducerONSBean extends BasicONSBean {

    String ProducerId;

    public ProducerONSBean() {
    }

    public ProducerONSBean(String ProducerId) {
        this.ProducerId = ProducerId;
    }


    public ProducerONSBean(String ProducerId, boolean isOrder) {
        this(ProducerId);
        this.setIsorder(isOrder);
    }

    public ProducerONSBean(String producerBeanName, String ProducerId, boolean isOrder) {
        this(ProducerId, isOrder);
        this.setName(producerBeanName);
    }

    @NotBlank
    public String getProducerId() {
        return ProducerId;
    }

    public void setProducerId(String producerId) {
        ProducerId = producerId;
    }

    @Override
    public String toString() {
        return String.format("[ProducerId=%s;topic=%s;tag=%s]", ProducerId, getTopic(), getExpression());
    }
}
