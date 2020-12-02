from concurrent.futures import ThreadPoolExecutor, wait, ALL_COMPLETED
from insertRecords import insertRecord
import pymysql
from dbutils.pooled_db import PooledDB
from timeDecorator import spentTime

maxConnections = 50
pool = PooledDB(pymysql, maxConnections, host='localhost', user='root', port=3306, passwd='password', db='stocks',
                use_unicode=True)

def insertRecordMultiThread(stockCode: str):
    db_connector = pool.connection()
    insertRecord(stockCode, db_connector)
    db_connector.close()
    print(stockCode + " finished")

@spentTime
def insertAllRecordsMultiThread():
    with open("./StockCodes.txt", "r", encoding="utf-8") as f:
        lines = f.readlines()
        stockCodes = [line.split(",")[0] for line in lines]
    with ThreadPoolExecutor(max_workers=25) as executor:
        tasks = [executor.submit(insertRecordMultiThread, stockCode) for stockCode in stockCodes]
        wait(tasks, timeout=None, return_when=ALL_COMPLETED)


if __name__ == "__main__":
    insertAllRecordsMultiThread()

