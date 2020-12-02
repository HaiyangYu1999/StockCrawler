import time

# 测试某个函数执行时间的装饰器
def spentTime(f):
    def f1():
        t1 = time.time()
        f()
        t2 = time.time()
        print("spent time {}".format(t2 - t1))
    return f1