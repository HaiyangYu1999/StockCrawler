import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class MySQLConnection {     //数据库连接类
    static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String URL = "jdbc:mysql://localhost:3306/stocks?useUnicode=true&characterEncoding=utf-8&useSSL=true";
    static final String USERNAME = "root";
    static final String PASSWORD = "password";
    private static final String INSERTPREFIX = "INSERT INTO record_minute(stockcode, recordtime, stockprice) VALUES";
    public static int updateDatabase(Map<String, String> map, int batchSize) throws SQLException       //将所有爬取的信息一次性写入到数据库, 在爬虫停止之后才可以调用
    {                                                               //批量写入, 一次写batchSize条, 而不是一次1条Insert语句
        /*
         * Create table record_minute(             //写入数据库
         * id BIGINT auto_increment unique primary key,
         * stockcode char(6),
         * recordtime char(20),
         * stockprice INT);
         */
        int cnt = 0; //总插入行数
        Connection connection = null;
        Statement statement = null;
        try{
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            statement = connection.createStatement();
            StringBuilder sb = null;
            for(Map.Entry<String, String> entry : map.entrySet())
            {
                if(cnt % batchSize == 0){
                    if(sb != null && sb.charAt(sb.length()-1) == ','){
                        sb.setCharAt(sb.length()-1, ';');
                        statement.executeUpdate(sb.toString());
                    }
                    sb = new StringBuilder(INSERTPREFIX);
                }
                String[] strings = entry.getKey().split(":");
                String sql = String.format("(\"%s\",\"%s\",%s),",
                        strings[0], strings[1], entry.getValue());
                sb.append(sql);
                ++cnt;
            }
            if(sb != null && sb.charAt(sb.length()-1) == ','){
                sb.setCharAt(sb.length()-1, ';');
                statement.executeUpdate(sb.toString());
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(statement != null)
                statement.close();
            if(connection != null)
                connection.close();
        }
        return cnt;
    }

    public static int updateDatabase(Map<String, String> map) throws SQLException       //将所有爬取的信息一次性写入到数据库, 在爬虫停止之后才可以调用
    {
        return updateDatabase(map, 1000);
    }

}

