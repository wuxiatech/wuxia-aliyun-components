/*
* Created on :2017年1月20日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ons.consumer.bean;

import java.util.Properties;

import cn.wuxia.common.util.StringUtil;
import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;

public class UnorderConsumerBean extends BasicConsumerBean {

    private MessageListener messageListener;

    private Consumer consumer;

    /**
     * Description of the method
     *
     * @author songlin
     */
    public void start() {
        Properties consumerProperties = accountBean.toProperties();
        consumerProperties.setProperty("ConsumerId", consumerBean.ConsumerId);
        // 消息消费失败时的最大重试次数
        consumerProperties.put(PropertyKeyConst.MaxReconsumeTimes, "2");
        //设置每条消息消费的最大超时时间,超过这个时间,这条消息将会被视为消费失败,等下次重新投递再次消费. 每个业务需要设置一个合理的值. 单位(分钟)
        consumerProperties.put(PropertyKeyConst.ConsumeTimeout, "3");
        consumer = ONSFactory.createConsumer(consumerProperties);
        consumer.subscribe(consumerBean.getTopic(), StringUtil.isBlank(consumerBean.getExpression()) ? "*" : consumerBean.getExpression(), messageListener);
        consumer.start();
        logger.info("开始监听无序消费队列：{}", consumerBean);
    }

    public void shutdown() {
        consumer.shutdown();
    }

    public MessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

}
