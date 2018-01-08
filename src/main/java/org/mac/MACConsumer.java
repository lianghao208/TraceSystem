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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Liang Hao on 2017/12/29.
 */
public class MACConsumer {

    private static final String EXCHANGE_NAME = "microstack";

    //数据包总数量
    private static int dataNum;

    //每个数据包的大小
    private static int dataSize;

    //总数据包大小
    private static int dataSizeSum;


    private static double time;

    //所有数据包的总时延
    private static double timeDelaySum;

    //每个数据包的时延
    private static double timeDelay;

    //第一个数据包的接收时间
    private static long startTime;

    //用于记录Phy层向Mac层传输信息包时所对应的时间<数据包号,接收时间>
    private static Map<Integer,Long> receiveTimeMap = new HashMap<Integer, Long>();

    //用于记录Phy层向Mac层传输信息包时所对应的时间<mac字段数据包号+源结点id,接收时间>
    private static Map<String,Long> macSendTimeMap = new HashMap<String, Long>();

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
        channel.queueBind(queueName, EXCHANGE_NAME, BindingKeySet.DRT_PLUS_RECV_DATA_NTF);
        channel.queueBind(queueName, EXCHANGE_NAME, BindingKeySet.ALOHA_MSG_RECV_DATA_NTF);
        channel.queueBind(queueName, EXCHANGE_NAME, BindingKeySet.SIMULATE_CHANNEL);

        System.out.println(" [*] Waiting for messages about ALOHA. To exit press CTRL+C");

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);

        while (true)
        {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            String routingKey = delivery.getEnvelope().getRoutingKey();

            dataProcess(routingKey,message);
            //macAvgDelay(routingKey,message,10);
            System.out.println(" Received routingKey = " + routingKey
                    + ",msg = " + message + ".");
        }


    }


    public static void dataProcess(String routingKey, String message){
        JSONObject jsonObject = JSON.parseObject(message);
        Long sendTime = new Long(0);
        Long receiveTime = new Long(0);
        if (routingKey.equals(BindingKeySet.SIMULATE_CHANNEL) &&
                jsonObject.getIntValue("layerid") == 2 &&
                jsonObject.getString("role").equals("s") &&
                jsonObject.getJSONObject("mac") != null){
            JSONObject mac = jsonObject.getJSONObject("mac");
            if(mac.getString("type").equals("data")){
                //System.out.println("<" + mac.getIntValue("serialnum") + "," + jsonObject.getLongValue("sendtime"));
                macSendTimeMap.put(String.valueOf(mac.getIntValue("serialnum"))+String.valueOf(mac.getIntValue("sid")),jsonObject.getLongValue("sendtime"));
                //sendTime = jsonObject.getLongValue("sendtime");
            }
        }

        if (routingKey.equals(BindingKeySet.DRT_PLUS_RECV_DATA_NTF) &&
                jsonObject.getIntValue("layerid") == 2 &&
                jsonObject.getString("role").equals("r")){
            JSONObject mac = jsonObject.getJSONObject("mac");
            //System.out.println(mac.getIntValue("serialnum"));
            if(mac.getString("type").equals("data") &&
                    macSendTimeMap.get(String.valueOf(mac.getIntValue("serialnum"))+String.valueOf(mac.getIntValue("sid"))) != null){
                //1.传输时延
                timeDelay = macDelay(jsonObject, mac);

                //2.数据包大小
                dataSize = macDataSize(jsonObject);

                //3.计算吞吐量
                System.out.println("数据包大小（/bit）：" + dataSize + " 平均吞吐量为：" + dataSize/(timeDelay/1000.0));

                //4.计算平均吞吐量
                timeDelaySum = macDelaySum(jsonObject, dataSize);
            }
        }
    }

    /**
     * 计算平均吞吐量
     * @param dataSizeSum
     * @param delay
     */
    public static void macThroughput(int dataSizeSum, double delay){
        System.out.println("数据包总大小（/bit）：" + dataSizeSum + " 吞吐量为：" + dataSizeSum/(delay/1000.0));

    }


    /**
     * 传输时延
     * @param jsonObject
     * @param mac
     * @return
     */
    public static double macDelay(JSONObject jsonObject,JSONObject mac){
        Long receiveTime = jsonObject.getLongValue("recvtime");
        double delay = receiveTime - macSendTimeMap.get(String.valueOf(mac.getIntValue("serialnum")) + String.valueOf(mac.getIntValue("sid")));
        System.out.println("端到端时延为（/s）：" + delay/1000.0);
        return delay;
    }

    /**
     * 总传输时延
     * @param jsonObject
     * @param dataSize
     * @return
     */
    public static double macDelaySum(JSONObject jsonObject, int dataSize){
        dataNum++;
        if (dataNum == 1){
            startTime = jsonObject.getLongValue("recvtime");
        }
        dataSizeSum += dataSize;
        Long receiveTime = jsonObject.getLongValue("recvtime");
        double delay = receiveTime - startTime;
        System.out.println("端到端总时延为（/s）：" + delay/1000.0);
        System.out.println("端到端平均时延为（/s）：" + delay/1000.0/dataNum);
        //System.out.println("数据包总大小（/bit）：" + dataSizeSum + " 吞吐量为：" + dataSizeSum/(delay/1000.0));
        macThroughput(dataSizeSum,delay);
        return delay;
    }

    /**
     * 计算数据包大小
     * @param jsonObject
     * @return
     */
    public static int macDataSize(JSONObject jsonObject){
        JSONObject packet = jsonObject.getJSONObject("packet");
        int size = (packet.getIntValue("tail") - packet.getIntValue("head"))*8;
        System.out.println("数据包大小为：" + size);
        return size;
    }

}
