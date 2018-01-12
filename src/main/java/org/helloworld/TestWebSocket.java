package org.helloworld;

import org.utils.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Liang Hao on 2018/1/10.
 */
public class TestWebSocket {
    public static void main(String[] args) {
        //向python服务器发送post请求
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("action", "show");//
        map.put("session", "123");//
        map.put("layer","mac");//
        map.put("dataSize",560);//
        map.put("throughput",2500);//
        map.put("dataSizeSum",1120);//
        map.put("throughputSum",6000);//
        map.put("delay",1.2);//
        map.put("delaySum",2.4);//
        map.put("delayAvg",1.2);//
        map.put("lossRate",0.9);//
        String sr= HttpRequestUtils.sendPost("http://localhost:8000/cart", map,"utf-8");
        System.out.println(sr);
    }
}
