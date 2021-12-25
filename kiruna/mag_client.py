import http.client
from collections import deque
import io
import datetime
import itertools
import threading
import time
from .k_calculator import get_K_index

class MagWatcher:
    def __init__(self, freq):
        self.current_ts_utc = datetime.datetime.now() - datetime.timedelta(minutes=40) - datetime.timedelta(hours=3)
        print("init current_ts_utc: " + str(self.current_ts_utc))
        self.__PAGE_SIZE_30MIN = 30 * 60
        self.__PAGE_SIZE_15MIN = 15 * 60
        self.frequency = 60
        if freq:
            self.frequency = freq
        self.x = deque(self.__PAGE_SIZE_30MIN*[0.0], maxlen=self.__PAGE_SIZE_30MIN )
        self.y = deque(self.__PAGE_SIZE_30MIN*[0.0], maxlen=self.__PAGE_SIZE_30MIN )

    def get_data(self):
        #TODO: should take into account current_ts_utc
        #TODO: redundant?
        values = dict()
        values['x_window_30'] = self.x
        values['y_window_30'] = self.y

        values['x_window_15'] = list(itertools.islice(self.x, 0, self.__PAGE_SIZE_15MIN))
        values['y_window_15'] = list(itertools.islice(self.y, 0, self.__PAGE_SIZE_15MIN))
        return values

    def service(self):
        def calculate_bytes():
            delta = datetime.datetime.now()- datetime.timedelta(hours=3)-self.current_ts_utc
            print("delay: " + str(delta))
            return delta.seconds * 36

        def process(line):
            parts = line.split("  ")
            if (len(parts) > 1):
                components = parts[1].split()
                #MagRecord magRecord = new MagRecord();
                ts_str = parts[0]
                if len(ts_str)<14:
                    return 0
                ts = datetime.datetime.strptime(ts_str, '%Y%m%d%H%M%S')
                if ts > self.current_ts_utc and len(components)>1:
                    self.x.appendleft(float(components[0].strip()))
                    self.y.appendleft(float(components[1].strip()))
                    self.current_ts_utc = ts
                    return 1
            return 0
            #if (components.length>1):
            #magRecord.setYcomponent(Float.parseFloat(components[1].trim()));

            #if ( isDateBeforeToday(magRecord.getTimestamp()) && isDateAfterLastTimestamp(magRecord.getTimestamp())) {
             #   magRec.add(magRecord);
            #}

        def parse_results(msg):
            #date_format_str = '%Y-%m-%dT%H:%M:%S.%fZ'
            #start = datetime.strptime('20211206203042', date_format_str)
            buf = io.StringIO(msg)
            cnt = 0
            while True:
                line = buf.readline()
                if not line:
                    break
                cnt += process(line)
                #TODO replace by logging
            print("new lines "+ str(cnt))

        print("start: "+ str(datetime.datetime.now()))
        try:
            headers = {'Range': 'Bytes=-'+str(calculate_bytes())}
            conn = http.client.HTTPSConnection("www2.irf.se")
            conn.request("GET", "/maggraphs/rt.txt", {}, headers)
            response = conn.getresponse()
            data = response.read().decode("utf-8")
            parse_results(data)
            print(response.status, response.reason, len(data))
            conn.close()
            if response.status == 206:
                threading.Timer(self.frequency, self.service).start()
            elif response.status == 200:
                threading.Timer(5, self.service).start()
        except Exception as e:
            print(e)
            conn.close()
            threading.Timer(20, self.service).start()
