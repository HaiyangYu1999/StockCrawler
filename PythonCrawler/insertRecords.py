from findAllStocksCode import getJson
import pymysql
import json
from timeDecorator import spentTime

def insertRecord(stockCode: str, database_connector):
    cursor = database_connector.cursor()
    cursor.execute("USE stocks")
    prefix0 = "http://push2.eastmoney.com/api/qt/stock/get?secid=0."
    prefix1 = "http://push2.eastmoney.com/api/qt/stock/get?secid=1."
    suffix = "&fields=f57,f58,f107,f43,f169,f170,f171,f47,f48,f60,f46,f44,f45,f168,f50,f162,f177"
    url0 = prefix0 + stockCode + suffix
    url1 = prefix1 + stockCode + suffix
    jsonText = json.loads(getJson(url0))
    if not isinstance(jsonText["data"], dict):
        jsonText = json.loads(getJson(url1))
    data = jsonText["data"]
    insertSQL = '''INSERT INTO Record(record_time, f57, f43, f44, f45, f46, f47, f48,  f50, f60, f168, f169, f170)
                                VALUES(NOW(), {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})'''.format(
        data["f57"], data["f43"], data["f44"], data["f45"], data["f46"], data["f47"], data["f48"],
        data["f50"], data["f60"], data["f168"], data["f169"], data["f170"])

    cursor.execute(insertSQL)
    database_connector.commit()
    cursor.close()

@spentTime
def insertAllRecords():
    connectParams = {"host": "localhost", "port": 3306, "user": "root", "password": "password"}
    database_connector = pymysql.connect(**connectParams)
    with open('./StockCodes.txt', "r", encoding="utf-8") as f:
        lines = f.readlines()
        for line in lines:
            stockCode = line.split(",")[0]
            insertRecord(stockCode, database_connector)
            print(stockCode + " finished")
    database_connector.close()


if __name__ == "__main__":
    insertAllRecords()