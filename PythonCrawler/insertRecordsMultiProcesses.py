from concurrent.futures import ProcessPoolExecutor, wait, ALL_COMPLETED
from insertRecords import insertRecord
import pymysql
from timeDecorator import spentTime

def insertRecordMultiProcess(stockCode: str):
    connectParams = {"host": "localhost", "port": 3306, "user": "root", "password": "password"}
    db = pymysql.connect(**connectParams)
    insertRecord(stockCode, db)
    db.close()
    print(stockCode + " finished")

@spentTime
def insertAllRecordsMultiProcess():
    with open("./StockCodes.txt", "r", encoding="utf-8") as f:
        lines = f.readlines()
        stockCodes = [line.split(",")[0] for line in lines]
    with ProcessPoolExecutor(max_workers=25) as executor:
        tasks = [executor.submit(insertRecordMultiProcess, stockCode) for stockCode in stockCodes]
        wait(tasks, timeout=None, return_when=ALL_COMPLETED)


if __name__ == "__main__":
    insertAllRecordsMultiProcess()