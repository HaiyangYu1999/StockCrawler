import pymysql
import traceback


def initializeDatabase():
    try:
        cursor = db.cursor()
        createDatabase = "CREATE DATABASE IF NOT EXISTS stocks"
        cursor.execute(createDatabase)
    except Exception as e:
        traceback.print_exc()


def initializeStockTable():
    try:
        cursor = db.cursor()
        cursor.execute("USE stocks")
        sql = ''' CREATE TABLE IF NOT EXISTS stock(
                stock_id INT AUTO_INCREMENT,
                stock_code CHAR(8) NOT NULL,
                stock_name VARCHAR(20) NOT NULL,
                PRIMARY KEY(stock_id)
                );
            '''
        cursor.execute(sql)
        cursor.execute("SELECT COUNT(*) FROM stock;")
        count = cursor.fetchone()
        if count[0] == 0:
            with open("./StockCodes.txt", "r", encoding="utf-8") as f:
                lines = f.readlines()
                for line in lines:
                    line = line.strip()
                    insert = "INSERT INTO stock(stock_code, stock_name) VALUES('{}','{}')" \
                        .format(line.split(",")[0], line.split(",")[1])
                    cursor.execute(insert)
            db.commit()
        else:
            pass
    except Exception as e:
        traceback.print_exc()


# Table Interpret 用来解释record表中各个列的含义, 以及表中的格式和现实的格式
def initializeInterpretTable():
    cursor = db.cursor()
    cursor.execute("USE stocks")
    sql0 = '''TRUNCATE TABLE Interpret;'''
    cursor.execute(sql0);
    sql = '''CREATE TABLE IF NOT EXISTS Interpret(
            record_column CHAR(5) NOT NULL,
            explanation VARCHAR(20) NOT NULL,
            format_in_record VARCHAR(30) NOT NULL,
            format_in_reality VARCHAR(30) NOT NULL
            );'''
    cursor.execute(sql)
    sqls = []
    sqls.append("INSERT INTO Interpret VALUES('f43', '最新', '5471', '54.71')")
    sqls.append("INSERT INTO Interpret VALUES('f44', '最高', '5817', '58.17')")
    sqls.append("INSERT INTO Interpret VALUES('f45', '最低', '5118', '51.18')")
    sqls.append("INSERT INTO Interpret VALUES('f46', '今开', '5140', '51.40')")
    sqls.append("INSERT INTO Interpret VALUES('f47', '成交量', '836250', '83.62万手')")
    sqls.append("INSERT INTO Interpret VALUES('f48', '成交额', '4616348672.0', '46.16亿')")
    sqls.append("INSERT INTO Interpret VALUES('f50', '量比', '265', '2.65')")
    sqls.append("INSERT INTO Interpret VALUES('f57', '股票代码', '688981', '688981')")
    sqls.append("INSERT INTO Interpret VALUES('f60', '昨收', '5094', '50.94')")
    sqls.append("INSERT INTO Interpret VALUES('f168', '换手率', '804', '8.04%')")
    sqls.append("INSERT INTO Interpret VALUES('f169', '上涨', '647', '6.47')")
    sqls.append("INSERT INTO Interpret VALUES('f170', '上涨百分比', '1270', '12.70%')")
    for sql in sqls:
        cursor.execute(sql)
    db.commit()


def initializeRecordTable():
    cursor = db.cursor()
    cursor.execute("USE stocks")
    sql = '''CREATE TABLE IF NOT EXISTS Record(
                record_id BIGINT AUTO_INCREMENT,
                record_time TIMESTAMP NOT NULL,
                f57 CHAR(8) NOT NULL,
                f43 INT NOT NULL,
                f44 INT NOT NULL,
                f45 INT NOT NULL,
                f46 INT NOT NULL,
                f47 BIGINT NOT NULL,
                f48 DECIMAL(13,1) NOT NULL,
                f50 SMALLINT NOT NULL,
                f60 INT NOT NULL,
                f168 SMALLINT NOT NULL,
                f169 INT NOT NULL,
                f170 INT NOT NULL,
                PRIMARY KEY(record_id)
                );
                '''
    cursor.execute(sql)


if __name__ == "__main__":
    connectParams = {"host": "localhost", "port": 3306, "user": "root", "password": "password"}
    db = pymysql.connect(**connectParams)
    try:
        initializeDatabase()
        initializeStockTable()
        initializeInterpretTable()
        initializeRecordTable()
    except Exception as e:
        traceback.print_exc()
    finally:
        db.close()
