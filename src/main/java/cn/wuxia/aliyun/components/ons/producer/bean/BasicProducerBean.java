/*
* Created on :2017年1月20日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ons.producer.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.openservices.ons.api.SendResult;

import cn.wuxia.aliyun.components.ons.bean.MessageBean;
import cn.wuxia.aliyun.components.ons.bean.ONSAccountBean;
import cn.wuxia.aliyun.components.ons.exception.MQException;
import cn.wuxia.common.exception.ValidateException;


public abstract class BasicProducerBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ProducerONSBean producerBean;

    protected ONSAccountBean accountBean;

    /**
     * Description of the method
     * @author songlin
     */
    public abstract void start();

    public abstract void shutdown();

    public ProducerONSBean getProducerBean() {
        return producerBean;
    }

    public void setProducerBean(ProducerONSBean producerBean) {
        this.producerBean = producerBean;
    }

    public abstract SendResult send(MessageBean bean) throws MQException, ValidateException;

    public ONSAccountBean getAccountBean() {
        return accountBean;
    }

    public void setAccountBean(ONSAccountBean accountBean) {
        this.accountBean = accountBean;
    }

}
