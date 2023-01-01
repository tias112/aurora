import http.client
from collections import deque
import io
import datetime
import itertools
import threading
import time
from .k_calculator import get_K_index

class MagWatcher:
    def __init__(self, freq, utc_shift, bz_shift_min = 90):
        self.current_ts_utc = datetime.datetime.now() - datetime.timedelta(minutes=40) - datetime.timedelta(hours=utc_shift)
        self.current_bz_ts_utc = datetime.datetime.now() - datetime.timedelta(minutes=120) - datetime.timedelta(hours=utc_shift)
        print("init current_ts_utc: " + str(self.current_ts_utc))
        self.__PAGE_SIZE_30MIN = 30 * 60
        self.__PAGE_SIZE_15MIN = 15 * 60
        self.__BZ_INTERVAL_MIN = 1
        self.__BZ_MAX_HISTORY_CNT = 3 * 60 # 3 hours
        self.__BZ_WINDOW_MIN = 30
        self.__BZ_CURRENT_SHIFT = bz_shift_min # 1.5 hours ago
        self.frequency = 60
        print("set utc shift: " + str(utc_shift))
        self.utc_shift = utc_shift
        if freq:
            self.frequency = freq

        self.x = deque(self.__PAGE_SIZE_30MIN*[0.0], maxlen=self.__PAGE_SIZE_30MIN )
        self.y = deque(self.__PAGE_SIZE_30MIN*[0.0], maxlen=self.__PAGE_SIZE_30MIN )
        self.bz = deque(self.__BZ_MAX_HISTORY_CNT*[0.0], maxlen=self.__BZ_MAX_HISTORY_CNT)

    def reinit_deque(self, first_x, first_y):
        self.x = deque(self.__PAGE_SIZE_30MIN*[first_x], maxlen=self.__PAGE_SIZE_30MIN )
        self.y = deque(self.__PAGE_SIZE_30MIN*[first_y], maxlen=self.__PAGE_SIZE_30MIN )

    def get_data(self):
        values = dict()
        values['x_window_30'] = self.x
        values['y_window_30'] = self.y

        values['x_window_15'] = list(itertools.islice(self.x, 0, self.__PAGE_SIZE_15MIN))
        values['y_window_15'] = list(itertools.islice(self.y, 0, self.__PAGE_SIZE_15MIN))
        values['bz_window'] = list(itertools.islice(self.bz, self.__BZ_CURRENT_SHIFT-self.__BZ_WINDOW_MIN, self.__BZ_CURRENT_SHIFT + self.__BZ_WINDOW_MIN))
        values['bz_current'] = list(itertools.islice(self.bz, 0, self.__BZ_WINDOW_MIN))
        return values

    def get_data_before(self):
        values = dict()
        values['x_window_15'] = list(itertools.islice(self.x, self.__PAGE_SIZE_15MIN, self.__PAGE_SIZE_30MIN))
        return values

    def calculate_bytes(self):
        delta = datetime.datetime.now()- datetime.timedelta(hours=self.utc_shift)-self.current_ts_utc
        print("delay: " + str(delta))
        return delta.seconds * 36


def is_night_time():
    hour = datetime.datetime.utcnow().hour
    return  (hour >= 16 or hour<=3)

def kiruna_is_broken(data, cnt):
    time_to_refresh = cnt%100
    not_refreshing = False
    if cnt==0 and len(data) > 100000:
        print("kiruna is not refreshing data")
        not_refreshing = True
    return not_refreshing and not time_to_refresh

def watcher_service(watcher, irfClient):
    ## run with 10 sec delays check if thread is stopped
    print("current hour", datetime.datetime.utcnow().hour, " is night time: ", is_night_time())
    t = threading.current_thread()
    n_skip = round(watcher.frequency / 10)
    cnt = 0
    data = ""
    while getattr(t, "do_run", True):
        if cnt%n_skip == 0 and is_night_time() and not kiruna_is_broken(data, cnt):  # time to run
        #if cnt%n_skip == 0  and not kiruna_is_broken(data, cnt):  # time to run
            lines = irfClient.process(watcher.calculate_bytes(), watcher.current_ts_utc)
            # when server restart around 12 - replace zero values in deque with first value
            if watcher.x[0] == 0.0 and watcher.y[0] == 0.0 and len(lines)>0:
                print("init deque first time")
                watcher.reinit_deque(lines[0]['x'], lines[0]['y'])
            if len(lines)>0:
                for line in lines:
                    cnt += line['cnt']
                    if line['cnt'] > 0:
                        watcher.x.appendleft(line['x'])
                        watcher.y.appendleft(line['y'])
                        watcher.current_ts_utc = line['ts']
            else:
                cnt = -1
        cnt += 1
        time.sleep(10)
    print("stopping watcher")

def bz_watcher_service(watcher, noaaClient):
    ## run with 10 sec delays check if thread is stopped
    t = threading.current_thread()
    while getattr(t, "do_run", True):
        if is_night_time():
            bz_lines = noaaClient.process(watcher.current_bz_ts_utc)
            # when server restart around 12 - replace zero values in deque with first value
            for bz_line in bz_lines:
                watcher.bz.appendleft(bz_line['bz'])
                watcher.current_bz_ts_utc = bz_line['ts']
        time.sleep(600)

    print("stopping Bz watcher")