## 项目简介

(Java/Python)爬虫 + MySQL + Redis项目. 

此项目分为2大部分, 离线爬虫和实时爬虫.

离线爬取下来的数据可用于后续的数据分析(我计划将其用于我的毕业设计).

实时爬虫获得的数据可以实现**股票降价通知的功能**.

未来会将数据分析的可视化部署到服务器上, 并添加**股票降价通知的功能**的web接口.

#### 离线爬虫

执行频率为每天1次, 爬取当天的4192支股票的统计信息. 并存放到数据库中.

并定期导出相应的表用于数据分析. 

#### 实时爬虫

执行频率为80秒一轮, 股票交易时间为周一至周五的9:30-11:30以及13:00-15:00. 每轮爬取4192支股票的实时价格信息. 并与Redis中存储的订阅信息进行交互, 如果发现了满足降价通知或下跌预警的条件, 就发送邮件通知订阅者.

每天产生的数据量大约为74万条. 在每天的股票交易结束后写回数据库.

## 运行说明

首先要保证本机已经安装MySQL和Redis. 远程连接也可以, 但是需要修改源代码重新Build.

**注意, 数据库密码和邮箱授权码已经过脱敏处理, 请务必配置成自己的密码和授权码然后再构建**

#### 离线爬虫部分

采用单线程Python爬虫 + MySQL. 主要文件在`./PythonCrawler`中

1. 运行文件`databaseInitialization.py`初始化数据库和相应的表, 数据库连接默认是`localhost:3306`

2. 运行文件`findAllStocksCode.py`获得所有4192支股票的股票代码和名称, 这时会得到`StockCodes.txt`文件

3. 修改`insertRecords.py`等以`"insertRecords"`开头的文件中的股票信息路径, 修改为`StockCodes.txt`文件的绝对路径, `crontab`定时执行需要绝对路径.

   ```python
   with open("/home/haiyang/StockCodes.txt", "r", encoding="utf-8") as f:
   ```

6. 开启定时爬虫`crontab -e`, 如果对每次执行的时间没有限制, **推荐**使用单线程爬虫`insertRecords.py`. 如果对时间有要求, 可以使用进程池, 线程池, 线程池+连接池, 异步IO等方法加速. **但是爬取失败的可能性会增加.** 
   
   + 单线程爬虫`insertRecords.py`的参考时间为406秒

   + 多进程爬虫`insertRecordsMultiProcesses.py` 的参考时间为150秒

   + 多线程爬虫`insertRecordsMultiThread.py`的参考时间为146秒

   + 多线程+数据库连接池爬虫`insertRecordsMultiThreadConnectionPool.py`的参考时间为143秒

5. 爬取的数据可以通过`SQLQuery.java`中的静态方法来查询. 可以查询某支股票最近n天的数据, 也可以给定开始时间和结束时间查询某支股票在此区间内的全部信息. 

#### 实时爬虫部分

采用多线程java爬虫, 使用http连接池加速, 使用Redis存储订阅者信息, 使用生产者消费者模型. 爬虫主要文件在`JavaCrawler`中.

1. 确保已经生成`StockCodes.txt`文件并在中`InfoProducer.java`配置正确的路径

   ```java
   private static final String CODELISTLOCATION = "/home/haiyang/crawler/StockCodes.txt";
   ```


2. 确保可以建立正确的MySQL和Redis连接. 默认为`localhost:3306`和`localhost:6379`. 非默认配置的话请修改`MySQLConnection.java`和`JedisUtil.java`中对应的配置.

3. 执行下面的MySQL命令, 确保在`stocks`数据库中建立正确的储存实时爬虫信息的表. `stocks`数据库是前面离线爬虫中`databaseInitialization.py`建立的数据库.

   ```mysql
   USE stocks;
   DROP TABLE IF EXISTS record_minute;
   CREATE TABLE record_minute(            
             id BIGINT auto_increment unique primary key,
             stockcode char(6),
             recordtime char(20),
             stockprice INT);
   ```

4. 使用maven打包, 生成`.jar`文件. 生成的`.jar`文件可用于开启定时任务. 运行的主类为`Main`.

## 更新日志

#### 2020.10.15

+ 离线爬虫项目大体完工
+ 成功部署到服务器中

#### 2020.11.22

+ 实时爬虫项目大体完工
+ 成功部署到服务器中

#### 2020.11.28

+ 增添log4j日志记录组件
+ 增添历史数据查询接口`SQLQuery.java`

## 计划更新

- [ ] 股票价格下跌预警功能
- [ ] 部署为web应用
- [ ] ECharts可视化
- [ ] 股票价格离线预测功能