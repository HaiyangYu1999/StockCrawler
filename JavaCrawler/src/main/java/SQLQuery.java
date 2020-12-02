import java.sql.*;

/**
 * 历史数据查询功能实现类, 查询的表为stocks.Record
 */
public class SQLQuery {
    // 获取数据库连接
    private static Connection getConnection(String host, int port, String user, String password) throws ClassNotFoundException, SQLException {
        Class.forName(MySQLConnection.DRIVER);
        String url = "jdbc:mysql://" + host + ":" + port + "/stocks?useUnicode=true&characterEncoding=utf-8&useSSL=true";
        return DriverManager.getConnection(url, user, password);
    }

    private static Connection getConnection() throws SQLException, ClassNotFoundException {
        return getConnection("localhost", 3306, MySQLConnection.USERNAME, MySQLConnection.PASSWORD);
    }
    //释放数据库连接
    private static void releaseConnection(Connection connection) throws SQLException {
        connection.close();
    }
    // 查询给定股票数据最近limit天的数据
    public static ResultSet getInfoByStockCode(int stockCode, int limit) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            String sql = "select " +
                    "LPAD(f57,6,0) as `股票代码`," +
                    "record_time as `记录时间`," +
                    "f44/100 as `最高`, " +
                    "f45/100 as `最低`, " +
                    "f46/100 as `今开`," +
                    "f60/100 as `昨收`," +
                    "CONCAT(f47/10000, \"万手\") as `成交量`," +
                    "CONCAT(f48/100000000, \"亿\") as `成交额`," +
                    "f50/100 as `量比`," +
                    "CONCAT(f168/100, \"%\") as `换手率`," +
                    "f169/100 as `上涨`," +
                    "CONCAT(f170/100, \"%\") as `上涨百分比`" +
                    "from Record where f57 = " + stockCode + " limit " + limit + ";";
            return statement.executeQuery(sql);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(statement != null){
                statement.close();
            }
            if (connection != null){
                releaseConnection(connection);
            }
        }
        return null;
    }

    // 查询给定股票数据开始日期和结束日期中的记录, 包括开始日期和结束日期这两天
    public static ResultSet getInfoByDate(int stockCode, int beginYear, int beginMonth, int beginDay, int endYear, int endMonth, int endDay) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            String sql = "select " +
                    "LPAD(f57,6,0) as `股票代码`," +
                    "record_time as `记录时间`," +
                    "f44/100 as `最高`, " +
                    "f45/100 as `最低`, " +
                    "f46/100 as `今开`," +
                    "f60/100 as `昨收`," +
                    "CONCAT(f47/10000, \"万手\") as `成交量`," +
                    "CONCAT(f48/100000000, \"亿\") as `成交额`," +
                    "f50/100 as `量比`," +
                    "CONCAT(f168/100, \"%\") as `换手率`," +
                    "f169/100 as `上涨`," +
                    "CONCAT(f170/100, \"%\") as `上涨百分比` " +
                    "from Record where f57 = " + stockCode + " and record_time >= str_to_date(" +
                    String.format("'%d-%d-%d 00:00:00'", beginYear, beginMonth, beginDay) +
                    ", '%Y-%m-%d %H:%i:%s') and record_time <= str_to_date(" +
                    String.format("'%d-%d-%d 23:59:59'", endYear, endMonth, endDay) +
                    ", '%Y-%m-%d %H:%i:%s');";
            System.out.println(sql);
            return statement.executeQuery(sql);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(statement != null){
                statement.close();
            }
            if (connection != null){
                releaseConnection(connection);
            }
        }
        return null;
    }
    //创建(stockCode, record_time)的联合索引
    public static void createIndex() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            String sql = "CREATE INDEX stockCode_time_index ON Record(f57, record_time);";
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(statement != null){
                statement.close();
            }
            if (connection != null){
                releaseConnection(connection);
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        System.out.println(getInfoByDate(1, 2020, 11, 1, 2020, 12, 1));
    }

}
