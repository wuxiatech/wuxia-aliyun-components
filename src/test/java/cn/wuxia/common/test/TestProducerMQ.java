package cn.wuxia.common.test;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.SendResult;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.Message;

public class TestProducerMQ {
    public static void main(String[] args) {
        DefaultMQProducer producer = new DefaultMQProducer("rjjk");
        producer.setNamesrvAddr("101.37.89.111:9876");
        try {
            producer.start();

            Message msg = new Message("test", "push", "1", "Just for test.".getBytes());

            SendResult result = producer.send(msg);
            System.out.println("id:" + result.getMsgId() + " result:" + result.getSendStatus());

            msg = new Message("test", "push", "2", "Just for test.".getBytes());

            result = producer.send(msg);
            System.out.println("id:" + result.getMsgId() + " result:" + result.getSendStatus());

            msg = new Message("test", "pull", "1", "Just for test.".getBytes());

            result = producer.send(msg);
            System.out.println("id:" + result.getMsgId() + " result:" + result.getSendStatus());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.shutdown();
        }
    }
}
