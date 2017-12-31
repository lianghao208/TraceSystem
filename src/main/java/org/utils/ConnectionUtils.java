package org.utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ连接工具类
 */
public class ConnectionUtils {
    private static String host;
    private static String userName;
    private static String password;
    private static int port;
    private static Connection connection;
    private static ConnectionUtils connectionUtils;
    private ConnectionUtils(String host,
                            String userName,
                            String password,
                            int port){
        this.host = host;
        this.userName = userName;
        this.password = password;
        this.port = port;
    }
    public synchronized static ConnectionUtils init(String host,
                           String userName,
                           String password,
                           int port){
        if(connectionUtils == null){
            connectionUtils = new ConnectionUtils(host,userName,password,port);
        }
        return connectionUtils;
    }

    public static synchronized Connection getConnection() throws IOException, TimeoutException {
        if(connection == null) {
            //生成线程池
            ExecutorService es = Executors.newCachedThreadPool();
            //创建连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            //设置RabbitMQ相关信息
            factory.setHost(host);
            factory.setUsername(userName);
            factory.setPassword(password);
            factory.setPort(port);
            //创建一个新的连接
            connection = factory.newConnection(es);
            return connection;
        }
        return connection;
    }
}
