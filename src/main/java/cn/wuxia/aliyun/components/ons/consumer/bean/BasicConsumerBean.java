/*
* Created on :2017年1月20日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ons.consumer.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wuxia.aliyun.components.ons.bean.ONSAccountBean;


public abstract class BasicConsumerBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ConsumerONSBean consumerBean;

    protected ONSAccountBean accountBean;
    /**
     * Description of the method
     * @author songlin
     */
    public abstract void start();

    public abstract void shutdown();

    public ConsumerONSBean getConsumerBean() {
        return consumerBean;
    }

    public void setConsumerBean(ConsumerONSBean consumerBean) {
        this.consumerBean = consumerBean;
    }

    public ONSAccountBean getAccountBean() {
        return accountBean;
    }

    public void setAccountBean(ONSAccountBean accountBean) {
        this.accountBean = accountBean;
    }

}
