import os
import io
import datetime
import itertools
import time
import requests


class NOAAClient:
    def __init__(self):
        self.url="https://services.swpc.noaa.gov/products/solar-wind/mag-2-hour.json"

    def process(self, current_ts_utc):
        try:
            self.current_ts_utc = current_ts_utc
            res = requests.get(self.url)
            if res:
                return self.parse_results(res.json())
        except Exception as e:
            print("exception")
            print(e)
            time.sleep(20)

        return []


    def process_line(self, line):
        if (len(line) > 1):
            ts_str = line[0]
            bz = line[3]
            ts = datetime.datetime.strptime(ts_str, '%Y-%m-%d %H:%M:%S.000')
            if ts > self.current_ts_utc:
                self.current_ts_utc = ts
                return {"cnt": 1, "ts": ts, "bz": float(bz.strip())}
        return {"cnt": 0, "bz": 0}

    def parse_results(self, msg):

        lines = []
        for line in msg[1:]:
            processed = self.process_line(line)
            if processed['cnt'] > 0:
                lines.append(processed)
        print("new bz lines ", str(len(lines)))

        return lines
