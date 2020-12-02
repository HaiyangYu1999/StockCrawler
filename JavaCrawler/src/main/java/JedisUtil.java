import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.LocalTime;

/**
 * redis连接类, 作用是读取设置降价通知的用户的邮箱和期望, 如果爬虫爬取的股票价格低于用户设置的期望, 则向用户
 * 发送邮件通知. 每订阅一次仅通知一次, 一旦触发通知之后就删除此次订阅, 再次满足条件就把不会通知了.
 * 原理类似于Zookeeper中的Watcher机制.
 */
public class JedisUtil {
    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    private static final JedisPoolConfig config = new JedisPoolConfig();
    static {
        config.setMaxTotal(32);         //最大连接数
        config.setMaxIdle(16);          //最大空闲连接数
        config.setMaxWaitMillis(10000); //连接池连接用尽后, 调用者的最大等待时间
        config.setTestOnBorrow(true);   //向资源池借用连接时是否做连接有效性检测(ping),检测到的无效连接将会被移除。
        config.setTestOnReturn(false);  //向资源池归还连接时是否做连接有效性检测(ping),检测到的无效连接将会被移除。
    }
    private static final JedisPool jedisPool = new JedisPool(config, HOST, PORT, 1000);
    public static JedisPool getJedisPoolInstance(){
        return jedisPool;
    }

    public static Jedis getJedisInstance(){
        return jedisPool.getResource();
    }

}
