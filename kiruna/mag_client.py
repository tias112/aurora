import http.client
from collections import deque
import io
import datetime
import itertools
import threading
import time
from .k_calculator import get_K_index

class MagWatcher:
    def __init__(self, freq, utc_shift):
        self.current_ts_utc = datetime.datetime.now() - datetime.timedelta(minutes=40) - datetime.timedelta(hours=utc_shift)
        print("init current_ts_utc: " + str(self.current_ts_utc))
        self.__PAGE_SIZE_30MIN = 30 * 60
        self.__PAGE_SIZE_15MIN = 15 * 60
        self.frequency = 60
        print("set utc shift: " + str(utc_shift))
        self.utc_shift = utc_shift
        if freq:
            self.frequency = freq

        self.x = deque(self.__PAGE_SIZE_30MIN*[0.0], maxlen=self.__PAGE_SIZE_30MIN )
        self.y = deque(self.__PAGE_SIZE_30MIN*[0.0], maxlen=self.__PAGE_SIZE_30MIN )

    def reinit_deque(self, first_x, first_y):
        self.x = deque(self.__PAGE_SIZE_30MIN*[first_x], maxlen=self.__PAGE_SIZE_30MIN )
        self.y = deque(self.__PAGE_SIZE_30MIN*[first_y], maxlen=self.__PAGE_SIZE_30MIN )

    def get_data(self):
        #TODO: should take into account current_ts_utc
        #TODO: redundant?
        values = dict()
        values['x_window_30'] = self.x
        values['y_window_30'] = self.y

        values['x_window_15'] = list(itertools.islice(self.x, 0, self.__PAGE_SIZE_15MIN))
        values['y_window_15'] = list(itertools.islice(self.y, 0, self.__PAGE_SIZE_15MIN))
        return values

    def get_data_before(self):
        values = dict()
        values['x_window_15'] = list(itertools.islice(self.x, self.__PAGE_SIZE_15MIN, self.__PAGE_SIZE_30MIN))
        return values

    def calculate_bytes(self):
        delta = datetime.datetime.now()- datetime.timedelta(hours=self.utc_shift)-self.current_ts_utc
        print("delay: " + str(delta))
        return delta.seconds * 36

def process(watcher, line):
    parts = line.split("  ")
    if (len(parts) > 1):
        components = parts[1].split()
        #MagRecord magRecord = new MagRecord();
        ts_str = parts[0]
        if len(ts_str)<14:
            return 0
        ts = datetime.datetime.strptime(ts_str, '%Y%m%d%H%M%S')
        # when server restart around 12 - replace zero values in deque with first value
        if watcher.x[0] == 0.0 and watcher.y[0] == 0.0:
            print("init deque first time")
            watcher.reinit_deque(float(components[0].strip()), float(components[1].strip()))
        if ts > watcher.current_ts_utc and len(components)>1:
            watcher.x.appendleft(float(components[0].strip()))
            watcher.y.appendleft(float(components[1].strip()))
            watcher.current_ts_utc = ts
            return 1
    return 0

def parse_results(watcher, msg):
    #date_format_str = '%Y-%m-%dT%H:%M:%S.%fZ'
    #start = datetime.strptime('20211206203042', date_format_str)
    buf = io.StringIO(msg)
    cnt = 0
    while True:
        line = buf.readline()
        if not line:
            break
        cnt += process(watcher, line)
    print("new lines ", str(cnt))


def watcher_service(watcher):
        #if (components.length>1):
        #magRecord.setYcomponent(Float.parseFloat(components[1].trim()));

        #if ( isDateBeforeToday(magRecord.getTimestamp()) && isDateAfterLastTimestamp(magRecord.getTimestamp())) {
         #   magRec.add(magRecord);
        #}

    # run with 10 sec delays check if thread is stopped
    t = threading.currentThread()
    n_skip = round(watcher.frequency / 10)
    cnt = 0
    while getattr(t, "do_run", True):
        if (cnt%n_skip == 0):  # time to run
            try:
                headers = {'Range': 'Bytes=-'+str(watcher.calculate_bytes())}
                conn = http.client.HTTPSConnection("www2.irf.se")
                conn.request("GET", "/maggraphs/rt.txt", {}, headers)
                response = conn.getresponse()
                data = response.read().decode("utf-8")
                parse_results(watcher, data)
                print(response.status, response.reason, len(data))
                conn.close()
                if response.status == 206:
                    time.sleep(10)
                elif response.status == 200:
                    cnt = -1
                    time.sleep(5)
            except Exception as e:
                print("exception")
                print(e)
                conn.close()
                time.sleep(20)
                cnt = -1
        cnt += 1
        time.sleep(10)
    print("stopping watcher")