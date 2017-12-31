package org.topic;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.utils.ConfigUtils;
import org.utils.ConnectionUtils;

/**
 * Created by Administrator on 2017/12/29.
 */
public class Consumer3 {

    private static final String EXCHANGE_NAME = "rabbitMQ.topic";

    public static void main(String[] argv) throws Exception
    {
        // 创建连接和频道
        //ConnectionFactory factory = new ConnectionFactory();
        //factory.setHost(ConfigUtils.HOST_NAME);
        //Connection connection = factory.newConnection();
        Connection connection = ConnectionUtils
                .init(  ConfigUtils.HOST_NAME,
                        ConfigUtils.USER_NAME,
                        ConfigUtils.PASSWORD,
                        ConfigUtils.PORT)
                .getConnection();
        Channel channel = connection.createChannel();
        // 声明转发器
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        // 随机生成一个队列
        String queueName = channel.queueDeclare().getQueue();

        //接收所有与kernel相关的消息
        channel.queueBind(queueName, EXCHANGE_NAME, "key.1");

        System.out.println(" [*] Waiting for messages about kernel. To exit press CTRL+C");

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);

        while (true)
        {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            String routingKey = delivery.getEnvelope().getRoutingKey();

            System.out.println(" [x] Received routingKey = " + routingKey
                    + ",msg = " + message + ".");
        }
    }
}
