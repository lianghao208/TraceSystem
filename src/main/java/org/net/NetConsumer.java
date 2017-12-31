package org.net;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import org.utils.BindingKeySet;
import org.utils.ConfigUtils;
import org.utils.ConnectionUtils;

/**
 * Created by Liang Hao on 2017/12/29.
 */
public class NetConsumer {

    private static final String EXCHANGE_NAME = "microstack";

    private static final String started = "\\u0001       p\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000!\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0002\\u0001 #\\u0000\\u0004\\u0001\\u0001\\u0002\\u0002 M65UFJ4L5VNGTO5CDXVE04D5KLZR5EL4C69LBKSL1LBT8VSRWX1LQS2Y5IAPPXET";
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

        //接收所有ALOHA发送时的相关消息
        channel.queueBind(queueName, EXCHANGE_NAME, BindingKeySet.DRT_PLUS_SEND_DATA_ALL);

        System.out.println(" [*] Waiting for messages about ALOHA. To exit press CTRL+C");

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);

        while (true)
        {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            String routingKey = delivery.getEnvelope().getRoutingKey();
            JSONObject jsonObject = JSON.parseObject(message);
            JSONObject packet = jsonObject.getJSONObject("packet");
            //String start = decodeUnicode(packet.getString("start"));
            System.out.println(" [x] Received routingKey = " + routingKey
                    + ",msg = " + message + ".");
        }
    }


}
