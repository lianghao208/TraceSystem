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
import org.utils.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Liang Hao on 2017/12/29.
 */
public class MACConsumer {

    private static final String EXCHANGE_NAME = "microstack";

    //接收数据包总数量
    private static int dataRcvNum;

    //发送数据包总数量
    private static int dataSendNum;

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

    //向python服务器发送post请求参数
    private static Map<String,Object> requestParamMap = new HashMap<String,Object>();

    //记录每个包发送的次数
    private static Map<String,Integer> dataSendNumMap = new HashMap<String, Integer>();

    public static void main(String[] argv) throws Exception
    {
        // 创建连接和频道
        requestParamMap.put("action", "show");
        requestParamMap.put("session", "123");
        requestParamMap.put("layer","mac");
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
        //channel.queueBind(queueName, EXCHANGE_NAME, BindingKeySet.ALOHA_MSG_RECV_DATA_NTF);
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
            //打印出监听的路由键
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
                String key = String.valueOf(mac.getIntValue("serialnum")) + String.valueOf(mac.getIntValue("sid"));
                macSendTimeMap.put(key,jsonObject.getLongValue("sendtime"));
                dataSendNum = macSendTimeMap.size();
                //如果之前发过相同的包
                if (dataSendNumMap.get(key) != null){
                    int num = dataSendNumMap.get(key);
                    dataSendNumMap.put(key,num++);
                }else {
                    dataSendNumMap.put(key,1);
                }
                return;
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

                String key = String.valueOf(mac.getIntValue("serialnum"))+String.valueOf(mac.getIntValue("sid"));

                //1.传输时延
                timeDelay = macDelay(jsonObject, mac);

                //2.数据包大小
                dataSize = macDataSize(jsonObject);

                //3.计算吞吐量
                System.out.println("数据包大小（/bit）：" + dataSize + " 吞吐量为（bit/s）：" + dataSize/(timeDelay/1000.0));
                requestParamMap.put("throughput",dataSize/(timeDelay/1000.0));

                //4.计算平均吞吐量
                timeDelaySum = macDelaySum(jsonObject, dataSize);

                //5.计算丢包率
                //将接收端数据包出栈，判断是否是最旧的包，如果不是，则存在丢包
                System.out.println("接收包数：" + dataRcvNum + " 发送包数：" + dataSendNum);
                System.out.println("丢包率：" + (1-dataRcvNum/(double)dataSendNum));
                requestParamMap.put("lossRate",dataRcvNum/(double)dataSendNum);
                requestParamMap.put("sendDataNum",dataSendNum);
                requestParamMap.put("recvDataNum",dataRcvNum);

                //6.计算每个包的发送数
                System.out.println("数据包序号：" + key + ",发送数为：" + dataSendNumMap.get(key));
                //连接服务器更新数据
                String sr= HttpRequestUtils.sendPost("http://localhost:8000/cart", requestParamMap,"utf-8");
                System.out.println(sr);
            }
        }
    }

    /**
     * 计算平均吞吐量
     * @param dataSizeSum
     * @param delay
     */
    public static void macThroughput(int dataSizeSum, double delay){
        System.out.println("数据包总大小（/bit）：" + dataSizeSum + " 总吞吐量为（bit/s）：" + dataSizeSum/(delay/1000.0));
        requestParamMap.put("dataSizeSum",dataSizeSum);
        requestParamMap.put("throughputSum",dataSizeSum/(delay/1000.0));
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
        requestParamMap.put("delay",delay);
        return delay;
    }

    /**
     * 总传输时延
     * @param jsonObject
     * @param dataSize
     * @return
     */
    public static double macDelaySum(JSONObject jsonObject, int dataSize){
        dataRcvNum++;
        if (dataRcvNum == 1){
            startTime = jsonObject.getLongValue("recvtime");
        }
        dataSizeSum += dataSize;
        Long receiveTime = jsonObject.getLongValue("recvtime");
        double delay = receiveTime - startTime;
        System.out.println("端到端总时延为（/s）：" + delay/1000.0);
        System.out.println("端到端平均时延为（/s）：" + delay/1000.0/dataRcvNum);
        requestParamMap.put("delaySum",delay/1000.0);
        requestParamMap.put("delayAvg",1.2);
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
        requestParamMap.put("dataSize",size);
        return size;
    }

}
