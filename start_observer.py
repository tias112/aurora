from kiruna.mag_client import MagWatcher
from kiruna.mag_client import watcher_service
from kiruna.k_calculator import KIndexCalculator
import time
import sys
import threading
import logging

from client.telegram_bot import *

def background_monitor_q(limit_q, kiruna_watcher, bot_token, bot_chatID):
    MODE = 'both'
    print("start monitor")
    print("limit_q",limit_q, " utc_shift:",kiruna_watcher.utc_shift)
    calculator = KIndexCalculator()
    #telegram_bot_sendtext(bot_token, bot_chatID, "notifier for aurora has been started")
    t = threading.currentThread()
    while getattr(t, "do_run", True):
        try:
            telegram = calculator.get_users_to_notify(MODE, kiruna_watcher, bot_chatID, limit_q)
            for t in telegram:
                print("send notification for", t[0])
                telegram_bot_sendtext(bot_token, t[0], t[1])
            time.sleep(60)
        except Exception as e:
            print("!exception!", e)
            time.sleep(60)
    calculator.stop()
    print("stopping monitor")

print("notifier for aurora has been started")
if len(sys.argv)!=6:
    print("Usage: python start_observer.py <limit_q> <freq_sec> <utc_shift> <bot_token> <bot_chat_id>")
    sys.exit(2)
bot_token = sys.argv[4]
bot_chatID = sys.argv[5]
limit_q = int(sys.argv[1])
freq = int(sys.argv[2])
utc_shift = int(sys.argv[3])
kiruna_watcher = MagWatcher(freq = freq, utc_shift = utc_shift)
#start watching kiruna
t1 = threading.Thread(target=watcher_service, args=(kiruna_watcher,))
t1.start()
time.sleep(5)

#start notifier job
t2 = threading.Thread(target=background_monitor_q, args=(limit_q,kiruna_watcher,bot_token,bot_chatID))
t2.start()
bot_main(bot_token)
t1.do_run = False
t2.do_run = False

