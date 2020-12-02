import redis.clients.jedis.Jedis;

import javax.mail.MessagingException;
import java.util.Map;
import java.util.concurrent.*;

public class InfoConsumer {
    public ConcurrentHashMap<String, String> map;
    private ThreadPoolExecutor threadPool;
    private BlockingQueue<String> blockingQueue;         //消费者获取消息的消息队列
    private int nThreads;
    public static final int infoPerThread = 200;  //每条线程处理的消息数量
    public InfoConsumer(int nThreads, BlockingQueue<String> blockingQueue)
    {
        map = new ConcurrentHashMap<>(550000);
        threadPool = new ThreadPoolExecutor(nThreads, nThreads, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(25000));
        this.blockingQueue = blockingQueue;
        this.nThreads = nThreads;
    }
    public InfoConsumer(BlockingQueue<String> blockingQueue)
    {
        this(23, blockingQueue);
    }

    public void consume(){
        this.consume(nThreads);
    }

    public void consume(int nthreads){
        for (int i = 0; i < nthreads; i++) {
            threadPool.execute(()->{
                for(int j = 0; j < infoPerThread; ++j)
                {
                    long nowTime = System.currentTimeMillis();
                    String jsonString = null;
                    try {
                        jsonString = blockingQueue.poll(500, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ignored) {
                        ;
                    }
                    if (jsonString != null){
                        String[] strings = consumeSingle(jsonString);
                        map.put(strings[0] + ":" + nowTime, strings[1]);
                        checkAndSend(strings[0], strings[1]);
                        //System.out.println(strings[0] + ":" + nowTime + " = " + strings[1]);
                    }
                }
            });

        }
    }

    private static void checkAndSend(String code, String price){          //检查是否满足用户设置的降价条件, 是的话就发送邮件通知用户
        Jedis jedis = JedisUtil.getJedisInstance();
        try{
            Map<String, String> emailPriceMap = jedis.hgetAll(code);
            for(Map.Entry<String, String> entry : emailPriceMap.entrySet())
            {
                if(Double.parseDouble(price) < Double.parseDouble(entry.getValue())){
                    jedis.hdel(code, entry.getKey());
                    String mailTitle = "Stock " + code + "'s price decreased under your expectation!";
                    String mailText = "Stock " + code + "'s now price is " + price + ", under your expectation " + entry.getValue() + "!";
                    System.out.println(mailText);
                    System.out.println(mailTitle);
                    MailSender.sentSimpleMail(mailTitle, mailText, entry.getKey());
                }
            }
        } catch (MessagingException ignored) {
            ;
        } finally {
            jedis.close();
        }
    }

    private String[] consumeSingle(String jsonString)
    { // json string demo {"rc":0,"rt":4,"svr":182995780,"lt":1,"full":1,"data":{"f43":1962,"f57":"000001"}}
        String[] strings = jsonString.split("[,\"]");
        int n = strings.length;
        return new String[]{strings[n-2], strings[n-6].substring(1)};
    }

    public void release()
    {
        threadPool.shutdown();
    }

    public int getQueueSize(){
        return threadPool.getQueue().size();
    }

}
