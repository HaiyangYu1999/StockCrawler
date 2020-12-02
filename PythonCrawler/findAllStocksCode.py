import requests
import json
import traceback


def getJson(url: str):
    # 防止因为网络问题第一次爬取失败, 引入失败自动重试机制
    maxAttemps = 10
    attempts = 0
    success = False
    while not success or attempts <= maxAttemps:
        try:
            header = {'Connection': 'close'}
            r = requests.get(url, header)
            r.raise_for_status()
            r.encoding = r.apparent_encoding
            success = True
            return r.text
        except:
            attempts += 1
    if not success:
        raise Exception("getJson Error after retried {} times!".format(maxAttemps))


def resolutionJson(jsonText: str) -> list:
    jsonText = json.loads(jsonText)
    data = jsonText["data"]
    count = data["total"]
    allData = data["diff"]
    allStockInfo = []
    for i in range(count):
        allStockInfo.append([allData[str(i)]["f12"], allData[str(i)]["f14"]])
    return allStockInfo


def saveStockCodes(path: str, data: list):
    with open(path, 'w', encoding='utf-8') as f:
        for item in data:
            f.write(item[0] + ", " + item[1] + "\n")


if __name__ == "__main__":
    webAPI = "http://31.push2.eastmoney.com/api/qt/clist/get?pn=1&pz=10000&fs=m:0+t:6,m:0+t:13,m:0+t:80,m:1+t:2,m:1+t:23&fields=f12,f14"
    jsonText = getJson(webAPI)
    allStockInfo = resolutionJson(jsonText)
    path = "./StockCodes.txt"
    saveStockCodes(path, allStockInfo)
