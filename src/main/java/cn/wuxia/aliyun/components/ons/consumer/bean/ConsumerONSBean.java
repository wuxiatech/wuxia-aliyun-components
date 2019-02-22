/*
* Created on :2017年2月12日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ons.consumer.bean;

import cn.wuxia.aliyun.components.ons.bean.BasicONSBean;
import org.hibernate.validator.constraints.NotBlank;

public class ConsumerONSBean extends BasicONSBean {

    String ConsumerId;

    public ConsumerONSBean() {

    }

    public ConsumerONSBean(String ConsumerId) {
        this.ConsumerId = ConsumerId;
    }


    public ConsumerONSBean(String ConsumerId, boolean isOrder) {
        this(ConsumerId);
        this.setIsorder(isOrder);
    }

    public ConsumerONSBean(String consumerBeanName, String ConsumerId, boolean isOrder) {
        this(ConsumerId, isOrder);
        this.setName(consumerBeanName);
    }

    @NotBlank
    public String getConsumerId() {
        return ConsumerId;
    }

    public void setConsumerId(String consumerId) {
        ConsumerId = consumerId;
    }

    @Override
    public String toString() {
        return String.format("[ConsumerId=%s;topic=%s;tag=%s]", ConsumerId, getTopic(), getExpression());
    }
}
