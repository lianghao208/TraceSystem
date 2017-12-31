package org.utils;

/**
 * Created by Administrator on 2017/12/29.
 * 协议键
 */
public class BindingKeySet {
    //MAC层协议
    public static final String ALOHA_MSG_SEND_DATA_REQ = "NewAloha.MsgSendDataReq";
    public static final String ALOHA_MSG_SEND_DATA_RSP = "NewAloha.MsgSendDataRsp";
    public static final String ALOHA_MSG_RECV_DATA_NTF = "NewAloha.MsgRecvDataNtf";
    public static final String ALOHA_MSG_SEND_DATA_ALL = "NewAloha.*";

    public static final String DRT_PLUS_SEND_DATA_REQ = "DrtPlus.MsgSendDataReq";
    public static final String DRT_PLUS_SEND_DATA_RES = "DrtPlus.MsgSendDataRsp";
    public static final String DRT_PLUS_RECV_DATA_NTF = "DrtPlus.MsgRecvDataNtf";
    public static final String DRT_PLUS_SEND_DATA_ALL = "DrtPlus.#";

    public static final String SIMULATE_CHANNEL = "SimulateChannel.MsgSendDataReq";

    //传输层协议
    public static final String UDP_MSG_SEND_DATA_RES = "Udp.MsgSendDataReq";
    public static final String UDP_MSG_SEND_DATA_RSP = "Udp.MsgSendDataRsp";
    public static final String UDP_MSG_RECV_DATA_NTF = "Udp.MsgRecvDataNtf";
    public static final String UDP_MSG_SEND_DATA_ALL = "Udp.#";


}
