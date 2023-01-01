import os
import http.client
import io
import datetime
import itertools
import time

class IRFClient:
    def __init__(self):
        self.url="www2.irf.se"
        self.path = "/maggraphs/rt.txt"

    def process(self, numBytes, current_ts_utc):
        self.current_ts_utc = current_ts_utc
        data = self.get_http_content(numBytes)
        if (data == ""):
            return []
        lines = self.parse_results(data)
        return lines

    def get_http_content(self, numBytes):
        try:
            headers = {'Range': 'Bytes=-'+str(numBytes)}
            conn = http.client.HTTPSConnection(self.url)
            conn.request("GET", self.path, {}, headers)
            response = conn.getresponse()
            data = response.read().decode("utf-8")

            print(response.status, response.reason, len(data))
            conn.close()
            if response.status == 206:
                time.sleep(10)
            elif response.status == 200:
                time.sleep(5)
                return ""
            return data
        except Exception as e:
            print("exception")
            print(e)
            conn.close()
            time.sleep(20)
            return ""

    def process_line(self, line):
        parts = line.split("  ")
        if (len(parts) > 1):
            components = parts[1].split()
            #MagRecord magRecord = new MagRecord();
            ts_str = parts[0]
            if len(ts_str)<14:
                return {"cnt": 0, "x": 0, "y": 0}
            ts = datetime.datetime.strptime(ts_str, '%Y%m%d%H%M%S')
            if ts > self.current_ts_utc and len(components)>1:
                self.current_ts_utc = ts
                return {"cnt": 1, "ts": ts, "x": float(components[0].strip()), "y": float(components[1].strip())}
        return {"cnt": 0, "x": 0, "y": 0}

    def parse_results(self, msg):
        buf = io.StringIO(msg)
        lines = []
        while True:
            line = buf.readline()
            if not line:
                break
            processed = self.process_line(line)
            if processed['cnt'] > 0:
                lines.append(processed)
        print("new lines ", str(len(lines)))

        return lines
