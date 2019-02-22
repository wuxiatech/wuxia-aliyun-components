/*
* Created on :2017年2月12日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ons.handler;

import cn.wuxia.aliyun.components.ons.bean.BasicONSBean;
import cn.wuxia.aliyun.components.ons.bean.ONSAccountBean;
import cn.wuxia.aliyun.components.ons.consumer.bean.BasicConsumerBean;
import cn.wuxia.aliyun.components.ons.consumer.bean.ConsumerONSBean;
import cn.wuxia.aliyun.components.ons.consumer.bean.OrderConsumerBean;
import cn.wuxia.aliyun.components.ons.consumer.bean.UnorderConsumerBean;
import cn.wuxia.aliyun.components.ons.exception.MQException;
import cn.wuxia.common.exception.ValidateException;
import cn.wuxia.common.spring.SpringContextHolder;
import cn.wuxia.common.util.*;
import cn.wuxia.common.util.reflection.ReflectionUtil;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * [ticket id] mq 消费者监听类
 *
 * @author songlin @ Version : V<Ver.No> <2017年3月9日>
 */
public class ConsumerHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 将所有的监听对象缓存在Map中
     */
    private Map<String, BasicConsumerBean> consumersMap;

    /**
     * 所有的消费者对象（必填） 根据这个消费对象初始化消费队列
     */
    private List<BasicONSBean> consumers;

    /**
     * 消费的实现类（可选，三选一） 消费的方法为队列的名字+tag名 eg:method1 sendmailValid method2
     * sendmailRegister
     * <p>
     * <pre>
     * mq.sendmail.producerId
     * mq.sendmail.tag=valid,register
     * </pre>
     */
    private Class<?> consumerClass;

    /**
     * 消费的实现对象（可选，三选一） * 消费的方法为队列的名字+tag名 eg:method1 sendmailValid method2
     * sendmailRegister
     * <p>
     * <pre>
     * mq.sendmail.producerId
     * mq.sendmail.tag=valid,register
     * </pre>
     */
    private Object consumerObject;

    /**
     * 消费的实现类所在包 （可选，三选一） 消费的类名为:
     * <p>
     * class1 SendmailValidConsumer class2 SendmailRegisterConsumer
     * <p>
     * <pre>
     * mq.sendmail.producerId
     * mq.sendmail.tag=valid,register
     * </pre>
     */
    private String consumerPackage;

    /**
     * 队列的账号信息（必填）
     */
    private ONSAccountBean accountBean;

    public void start() throws MQException,ValidateException{
        valid();

        if (ListUtil.isEmpty(getConsumers())) {
            logger.info("消费者队列为空");
            return;
        }

        if (MapUtil.isEmpty(consumersMap)) {
            consumersMap = Maps.newHashMap();
        }

        for (BasicONSBean bean : getConsumers()) {
            /**
             * 循环拿到消费者部分并且需要为启动状态
             */
            if (bean != null && bean instanceof ConsumerONSBean && bean.isIstartup()) {
                ConsumerONSBean onsBean = (ConsumerONSBean) bean;
                ValidatorUtil.validate(onsBean);
                if (onsBean.getIsorder()) {
                    OrderConsumerBean consumerBean = new OrderConsumerBean();
                    consumerBean.setConsumerBean(onsBean);
                    consumerBean.setAccountBean(getAccountBean());
                    consumerBean.setMessageListener(getMessageOrderListener(onsBean));
                    consumerBean.start();
                    logger.info("已启动有序消息：{}", onsBean);
                    consumersMap.put(onsBean.getName(), consumerBean);
                } else {
                    UnorderConsumerBean consumerBean = new UnorderConsumerBean();
                    consumerBean.setConsumerBean(onsBean);
                    consumerBean.setAccountBean(getAccountBean());
                    consumerBean.setMessageListener(getMessageListener(onsBean));
                    consumerBean.start();
                    logger.info("启动无序消息：{}", onsBean);
                    consumersMap.put(onsBean.getName(), consumerBean);
                }
            }
        }

    }

    /**
     * 校验参数是否都已传递
     *
     * @author songlin
     */
    private void valid() {
        Assert.notNull(getAccountBean(), "账号信息不能为空");
        if (getConsumerPackage() == null) {
            Assert.notNull(getConsumerObject(), "消费实现对象：consumerObject 还没初始化");
        }
    }

    /**
     * 有序队列的监听
     *
     * @return
     * @author songlin
     */
    private MessageOrderListener getMessageOrderListener(ConsumerONSBean onsBean) throws MQException{


        if (StringUtil.isNotBlank(getConsumerPackage())) {
            String consumerPath = getConsumerPackage();
            if (!StringUtils.endsWith(consumerPath, ".")) {
                consumerPath += ".";
            }
            consumerPath += StringUtil.capitalize(onsBean.getName()) + "Consumer";
            try {
                Class<MessageOrderListener> clazz = ClassLoaderUtil.loadClass(consumerPath);
                logger.info("启动消息监听类：" + clazz.getName());
                return (MessageOrderListener) clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("无法加载"+consumerPath, e.getCause());
                throw  new MQException("无法加载"+consumerPath);
            }
        } else {
            final String consumerMethodName = StringUtils.uncapitalize(onsBean.getName());
            MessageOrderListener listener = new MessageOrderListener() {
                @Override
                public OrderAction consume(Message message, ConsumeOrderContext context) {
                    String info = String.format("第%d次消费有序消息,topic=%s,tag=%s,key=%s,MsgId=%s", message.getReconsumeTimes() + 1, message.getTopic(),
                            message.getTag(), message.getKey(), message.getMsgID());
                    logger.info(info);
                    Method method = ReflectionUtil.getAccessibleMethod(getConsumerObject(), consumerMethodName, new Class[]{Message.class});
                    if (method == null) {
                        logger.warn("{}方法{}无法找到,尝试加载方法{}", getConsumerObject().getClass().getName(), consumerMethodName,
                                StringUtils.uncapitalize(onsBean.getName()));
                        method = ReflectionUtil.getAccessibleMethod(getConsumerObject(), StringUtils.uncapitalize(onsBean.getName()),
                                new Class[]{Message.class});
                    }
                    if (method != null) {
                        try {
                            method.invoke(getConsumerObject(), new Object[]{message});
                        } catch (Exception e) {
                            logger.error("消费失败！！！！！" + info, e.getCause());
                            return OrderAction.Suspend;
                        }
                    } else {
                        logger.error("没有找到监听方法！");
                        return OrderAction.Suspend;
                    }
                    logger.info("成功完成" + info);
                    return OrderAction.Success;
                }
            };
            logger.info("已启动有序消息监听类：{},监听方法：{}", getConsumerObject().getClass().getName(), consumerMethodName);
            return listener;
        }
    }

    /**
     * 无序队列监听
     *
     * @return
     * @author songlin
     */
    private MessageListener getMessageListener(ConsumerONSBean onsBean) throws  MQException{

        if (StringUtil.isNotBlank(getConsumerPackage())) {
            String consumerPath = getConsumerPackage();
            if (!StringUtils.endsWith(consumerPath, ".")) {
                consumerPath += ".";
            }
            consumerPath += StringUtil.capitalize(onsBean.getName()) + "Consumer";
            try {
                Class<MessageListener> clazz = ClassLoaderUtil.loadClass(consumerPath);
                logger.info("启动消息监听类：" + clazz.getName());
                return (MessageListener) clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("无法加载"+consumerPath, e.getCause());
                throw  new MQException("无法加载"+consumerPath);
            }
        } else {
            final String consumerMethodName = StringUtils.uncapitalize(onsBean.getName());
            MessageListener listener = new MessageListener() {
                @Override
                public Action consume(Message message, ConsumeContext context) {
                    String info = String.format("第%d次消费无序消息,topic=%s,tag=%s,key=%s,MsgId=%s", message.getReconsumeTimes() + 1, message.getTopic(),
                            message.getTag(), message.getKey(), message.getMsgID());
                    logger.info(info);
                    Method method = ReflectionUtil.getAccessibleMethod(getConsumerObject(), consumerMethodName, new Class[]{Message.class});
                    if (method == null) {
                        logger.warn("{}方法{}无法找到,尝试加载方法{}", getConsumerObject().getClass().getName(), consumerMethodName,
                                StringUtils.uncapitalize(onsBean.getName()));
                        method = ReflectionUtil.getAccessibleMethod(getConsumerObject(), StringUtils.uncapitalize(onsBean.getName()),
                                new Class[]{Message.class});
                    }
                    if (method != null) {
                        try {
                            method.invoke(getConsumerObject(), new Object[]{message});
                        } catch (Exception e) {
                            logger.error("消费失败！！！！" + info, e.getCause());
                            return Action.ReconsumeLater;
                        }
                    } else {
                        logger.error("没有找到监听方法！请管理员检查代码。");
                        return Action.ReconsumeLater;
                    }
                    logger.info("成功完成" + info);
                    return Action.CommitMessage;
                }
            };
            logger.info("启动无序消息监听类：{},监听方法：{}", getConsumerObject().getClass().getName(), consumerMethodName);
            return listener;
        }
    }

    public void shutdown() {
        if (MapUtil.isNotEmpty(consumersMap)) {
            for (Map.Entry<String, BasicConsumerBean> producer : consumersMap.entrySet()) {
                producer.getValue().shutdown();
            }
        }
    }

    public BasicConsumerBean getConsumerBean(String name) {
        if (MapUtil.isNotEmpty(consumersMap)) {
            return consumersMap.get(name);
        }
        return null;
    }

    public ONSAccountBean getAccountBean() {
        return accountBean;
    }

    public void setAccountBean(ONSAccountBean accountBean) {
        this.accountBean = accountBean;
    }

    public List<BasicONSBean> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<BasicONSBean> consumers) {
        this.consumers = consumers;
    }

    public Class<?> getConsumerClass() {
        return consumerClass;
    }

    public void setConsumerClass(Class<?> consumerClass) {
        this.consumerClass = consumerClass;
    }

    public Object getConsumerObject() {
        if (null == consumerObject && null != getConsumerClass()) {
            consumerObject = SpringContextHolder.getBean(getConsumerClass());
        }
        return consumerObject;
    }

    public void setConsumerObject(Object consumerObject) {
        this.consumerObject = consumerObject;
    }

    public String getConsumerPackage() {
        return consumerPackage;
    }

    public void setConsumerPackage(String consumerPackage) {
        this.consumerPackage = consumerPackage;
    }
}
