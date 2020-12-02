import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static Logger LOG = LogManager.getLogger(Main.class);   //输出到日志
    private static BlockingQueue<String> bq = new ArrayBlockingQueue<>(250000);
    private static boolean doesMorningExecute = false;
    private static boolean doesAfternoonExecute = false;
    public static boolean isValidTimeMorning(){     //判断当前时间是不是在上午的股票交易时间9:30 - 11:30
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        if(hour == 9 && minute >= 30)
            return true;
        else if(hour == 10)
            return true;
        else if(hour == 11 && minute < 30)
            return true;
        return false;
    }
    public static boolean isBefore9_30(){
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        return hour < 9 || (hour == 9 && minute < 30);
    }
    public static boolean isBefore13(){
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        return hour < 13;
    }
    public static boolean isValidTimeAfternoon(){     //判断当前时间是不是在下午的股票交易时间13:00 - 15:00
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        return hour >=13 && hour < 15;
    }

    public static void main(String[] args) throws InterruptedException, IOException, SQLException {

        InfoProducer producer = new InfoProducer(bq);
        InfoConsumer consumer = new InfoConsumer(bq);
        int intervalSecond = 75;
        while(Main.isBefore9_30() && !Main.isValidTimeMorning()){
            LOG.debug("Before 9:30, waiting for 9:30...");
            TimeUnit.MINUTES.sleep(1);
        }
        if(Main.isValidTimeMorning()){
            LOG.debug("Morning begin to crawler stock info!");
        }
        //早9:30到11:30, 为股票交易时间, 反复执行这段函数
        while(Main.isValidTimeMorning()) {
            if(!doesMorningExecute){
                doesMorningExecute = true;
            }
            producer.produce();
            TimeUnit.SECONDS.sleep(5);
            consumer.consume();
            TimeUnit.SECONDS.sleep(intervalSecond);
            LOG.debug("ProducerThreadPoolQueueSize: " + producer.getQueueSize());
            LOG.debug("ConsumerThreadPoolQueueSize: " + consumer.getQueueSize());
            LOG.debug("BufferQueueSize: " + bq.size());
            LOG.debug("ConsumerMapSize:" + consumer.map.size());
        }
        if(doesMorningExecute){
            LOG.debug("Morning crawler finished.");
        }
        while(!bq.isEmpty()){
            LOG.debug("Remains "+ bq.size() + " info in BufferQueued");
            consumer.consume(Math.min(bq.size() / InfoConsumer.infoPerThread + 1, 30));        //爬虫结束后处理缓冲区剩下的消息
            TimeUnit.SECONDS.sleep(30);
        }
        if(doesMorningExecute){
            LOG.debug("Morning Consumer finished, " + consumer.map.size() + " info is in map");
        }
        while(Main.isBefore13() && !Main.isValidTimeAfternoon()){
            LOG.debug("Before 13:00, waiting for 13:00...");
            TimeUnit.MINUTES.sleep(1);
        }
        if(Main.isValidTimeAfternoon()){
            LOG.debug("Afternoon begin to crawler stock info!");
        }
        //下午13:00到15:00, 为股票交易时间, 反复执行这段函数
        while(Main.isValidTimeAfternoon()) {
            if(!doesAfternoonExecute){
                doesAfternoonExecute = true;
            }
            producer.produce();
            TimeUnit.SECONDS.sleep(5);
            consumer.consume();
            TimeUnit.SECONDS.sleep(intervalSecond);
            LOG.debug("ProducerThreadPoolQueueSize: " + producer.getQueueSize());
            LOG.debug("ConsumerThreadPoolQueueSize: " + consumer.getQueueSize());
            LOG.debug("BufferQueueSize: " + bq.size());
            LOG.debug("ConsumerMapSize:" + consumer.map.size());
        }
        if(doesAfternoonExecute){
            LOG.debug("Afternoon crawler finished.");
        }
        while(!bq.isEmpty()){
            LOG.debug("Remains "+ bq.size() + " info in BufferQueued.");
            consumer.consume(Math.min(bq.size() / InfoConsumer.infoPerThread + 1, 30));        //爬虫结束后处理缓冲区剩下的消息
            TimeUnit.SECONDS.sleep(30);
        }
        //爬虫结束, 释放资源, 将一天爬取的所有信息写入到数据库
        LOG.debug("Today's crawler finished, release resources.");
        producer.release();
        consumer.release();
        LOG.debug("Released resources successfully.");
        LOG.debug("Begin to insert all info into databases.");
        int count = MySQLConnection.updateDatabase(consumer.map);
        LOG.debug(count + " info has been inserted into databases.");
        LOG.debug("Main over.");
    }
}
