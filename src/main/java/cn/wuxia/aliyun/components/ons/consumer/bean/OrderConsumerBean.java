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
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderConsumer;

public class OrderConsumerBean extends BasicConsumerBean {

    private MessageOrderListener messageListener;

    private OrderConsumer orderconsumer;

    /**
     * Description of the method
     *
     * @author songlin
     */
    public void start() {
        Properties consumerProperties = accountBean.toProperties();
        consumerProperties.setProperty("ConsumerId", consumerBean.ConsumerId);
        // 顺序消息消费失败进行重试前的等待时间 单位(毫秒)
        consumerProperties.put(PropertyKeyConst.SuspendTimeMillis, "3000");
        // 消息消费失败时的最大重试次数
        consumerProperties.put(PropertyKeyConst.MaxReconsumeTimes, "2");
        //设置每条消息消费的最大超时时间,超过这个时间,这条消息将会被视为消费失败,等下次重新投递再次消费. 每个业务需要设置一个合理的值. 单位(分钟)
        consumerProperties.put(PropertyKeyConst.ConsumeTimeout, "3");
        orderconsumer = ONSFactory.createOrderedConsumer(consumerProperties);
        orderconsumer.subscribe(consumerBean.getTopic(), StringUtil.isBlank(consumerBean.getExpression()) ? "*" : consumerBean.getExpression(), messageListener);
        orderconsumer.start();
        logger.info("开始监听有序消费队列：{}", consumerBean);
    }

    public void shutdown() {
        orderconsumer.shutdown();
    }

    public MessageOrderListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageOrderListener messageListener) {
        this.messageListener = messageListener;
    }

}
