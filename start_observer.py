from kiruna.mag_service import MagWatcher
from kiruna.mag_service import watcher_service
from kiruna.mag_service import bz_watcher_service
from monitor import background_monitor_q
from client.irf_client import IRFClient
from client.noaa_client import NOAAClient
from client.telegram_bot import bot_main
import sys
import threading
import re
import time
import warnings

if __name__ == "__main__":

    warnings.filterwarnings(action='ignore', message='Python 3.6 is no longer supported')
    if len(sys.argv) != 7:
        print("Usage: python start_observer.py <limit_q> <freq_sec> <utc_shift> <bot_token> <bot_chat_id> <night_time>")
        sys.exit(2)

    if re.compile("^[0-9]+:[0-9]+$").match(sys.argv[6]):
        night_time = (sys.argv[6].split(":"))
    else:
        print("Usage of night_time example: 16-3")
        sys.exit(2)

    bot_token = sys.argv[4]
    bot_chatID = sys.argv[5]
    limit_q = int(sys.argv[1])
    freq = int(sys.argv[2])
    utc_shift = int(sys.argv[3])

    print("notifier for aurora has been started")

    kiruna_watcher = MagWatcher(freq=freq, utc_shift=utc_shift, night_start=int(night_time[0]),
                                night_end=int(night_time[1]))
    idf_client = IRFClient()
    noaa_client = NOAAClient()
    print(noaa_client.process(kiruna_watcher.current_ts_utc))  # TODO: calculate based on response
    # start watching kiruna
    t1 = threading.Thread(target=watcher_service, args=(kiruna_watcher, idf_client))
    t1.daemon = True
    t2 = threading.Thread(target=bz_watcher_service, args=(kiruna_watcher, noaa_client))
    t2.daemon = True
    t1.start()
    t2.start()
    time.sleep(5)
    # start notifier job
    t3 = threading.Thread(target=background_monitor_q, args=(limit_q, kiruna_watcher, bot_token, bot_chatID))
    t3.daemon = True
    t3.start()
    bot_main(bot_token)
    t1.do_run = False  # not necessarily as all services are daemons
    t2.do_run = False
    t3.do_run = False
