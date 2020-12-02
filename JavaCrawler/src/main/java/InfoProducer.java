import org.apache.http.HttpEntity;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class InfoProducer {
    private static final String PREFIX1 = "http://push2.eastmoney.com/api/qt/stock/get?secid=0.";
    private static final String PREFIX2 = "http://push2.eastmoney.com/api/qt/stock/get?secid=1.";
    private static final String SUFFIX = "&fields=f57,f43";

    private BlockingQueue<String> blockingQueue;         //生产者放入的消息队列
    private List<String> codeList = null;        //所有的股票代码
    private static final String CODELISTLOCATION = "D:/Files/java_projects/crawler/StockCodes.txt";
    /*----生产者线程池----*/
    ThreadPoolExecutor threadPool;
    /*----连接池参数----*/
    PoolingHttpClientConnectionManager connectionManager; // 连接池管理器
    HttpRequestRetryHandler myRequestRetryHandler; // 失败重试机制
    RequestConfig config;  //请求超时配置
    CloseableHttpClient httpClient; // http客户端配置

    public InfoProducer(BlockingQueue<String> buffer)
    {
        blockingQueue = buffer;
        codeList = readCodes();
        initPool();
        threadPool = new ThreadPoolExecutor(100,100, 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(25000)); //开启线程, 多线程爬取股票数据, 一个httpClient对应多个线程
    }

    public void produce()         //生产者, 爬取的信息放到缓冲区(阻塞队列)中
    {
        for (String code : codeList) {
            threadPool.execute(() -> {
                HttpGet httpGet = new HttpGet((code.charAt(0) == '6' ? PREFIX2 : PREFIX1) + code + SUFFIX);
                HttpEntity entity = null;
                try {
                    entity = httpClient.execute(httpGet).getEntity();
                    String info = EntityUtils.toString(entity);
                    blockingQueue.put(info);
                    //System.out.println(info);
                } catch (Exception ignored) {
                    ;
                } finally {
                    httpGet.releaseConnection();      //用完了就把连接放回连接池
                }
            });
        }
    }

    private List<String> readCodes() {    //读取所有股票代码
        BufferedReader bin = null;
        try{
            List<String> list = new ArrayList<>();
            bin = new BufferedReader(new InputStreamReader(
                    new FileInputStream(CODELISTLOCATION), StandardCharsets.UTF_8));
            String line = null;
            while((line = bin.readLine()) != null)
            {
                list.add(line.split(",")[0]);
            }
            bin.close();
            return list;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private void initPool()    //初始化连接池配置
    {
        connectionManager = new PoolingHttpClientConnectionManager(50000, TimeUnit.MILLISECONDS);
        connectionManager.setMaxTotal(500);             //连接池中最多有500个连接
        connectionManager.setDefaultMaxPerRoute(500);   //每个路由最大连接数为500

        myRequestRetryHandler = new DefaultHttpRequestRetryHandler(5, false); //失败重试5次
        config = RequestConfig.custom()
                .setConnectTimeout(10000)               //客户端和服务器建立连接的超时时间
                .setSocketTimeout(10000)                //客户端和服务器建立连接后，客户端从服务器读取数据的超时时间
                .setConnectionRequestTimeout(10000)        //从连接池取出连接的超时时间
                .build();
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(config)
                .setRetryHandler(myRequestRetryHandler)
                .build();
    }

    public void release() throws IOException {
        httpClient.close();             //关闭客户端
        connectionManager.close();      //关闭连接池管理器
        threadPool.shutdown();
    }

    public int getQueueSize(){
        return threadPool.getQueue().size();
    }

}
