package org.mac;


/**
 * Created by Administrator on 2017/12/30.
 * 网络性能指标计算
 */
public class PerformanceAnalysisUtils {

    /**
     * 计算吞吐量
     * @param dataSize 数据包大小（bit）
     * @param recvNum 接收数据包数量
     * @param time 所用时间（s）
     * @return
     */
    public static double throughput(long dataSize,int recvNum,double time){
        System.out.println(dataSize * recvNum / time);
        return dataSize * recvNum / time;
    }

    /**
     * 平均端到端时延
     * @param time 传输总时间
     * @param sendNum 发送的数据包数量
     * @return
     */
    public static double avgDelay(double time,int sendNum){
        return time/sendNum;
    }

    /**
     * 丢包率
     * @param srcNum
     * @param destNum
     * @return
     */
    public static double lossRate(int srcNum, int destNum){
        return (double)srcNum/destNum;
    }

    /**
     * 链路利用率
     * @param time 有效数据传输的总时间(s)
     * @param sendTime 整个传输过程的时间(有消息时才调用)(时间戳long)
     * @return
     */
    public static double useRate(double time,long sendTime){
        System.out.println(time/((System.currentTimeMillis()-sendTime)/1000.0));
        return time/((System.currentTimeMillis()-sendTime)/1000.0);
    }
}
