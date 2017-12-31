package org.mac;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
public class MACConsumer {

    private static final String EXCHANGE_NAME = "microstack";

    private static int dataNum;

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
        channel.queueBind(queueName, EXCHANGE_NAME, BindingKeySet.SIMULATE_CHANNEL);

        System.out.println(" [*] Waiting for messages about ALOHA. To exit press CTRL+C");

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);

        while (true)
        {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            String routingKey = delivery.getEnvelope().getRoutingKey();

            macThroughput(routingKey,message);

            /*System.out.println(" [x] Received routingKey = " + routingKey
                    + ",msg = " + message + ".");*/
        }


    }

    public static void macThroughput(String routingKey, String message){
        JSONObject jsonObject = JSON.parseObject(message);
        int dataSize = 0;

        if (routingKey.equals(BindingKeySet.DRT_PLUS_RECV_DATA_NTF) &&
                jsonObject.getIntValue("layerid") == 2 &&
                jsonObject.getString("role").equals("r")){
            JSONObject mac = jsonObject.getJSONObject("mac");
            if(mac.getString("type").equals("data")){
                //1.数据包大小
                JSONObject packet = jsonObject.getJSONObject("packet");
                dataSize = (packet.getIntValue("tail") - packet.getIntValue("head"))*8;
                //2.接收数据包的数量
                dataNum++;
                //3.时间
                System.out.println("数据包大小为：" + dataSize + " bit. 数据包数量为：" + dataNum);

            }
        }

        //2.接收数据包的数量
        /*if (routingKey.equals(BindingKeySet.DRT_PLUS_RECV_DATA_NTF) &&
                jsonObject.getIntValue("layerid") == 2 &&
                jsonObject.getString("role").equals("r")){
            JSONObject mac = jsonObject.getJSONObject("mac");
            if(mac.getString("type").equals("data")){
                dataNum++;
            }
        }*/

        //3.

        //String start = decodeUnicode(packet.getString("start"));
        //System.out.println("吞吐量为：" + PerformanceAnalysisUtils.throughput(dataSize,,));
    }


}
