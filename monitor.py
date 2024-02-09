from client.telegram_bot import *
from kiruna.k_calculator import KIndexCalculator
from client.db_client import DBClient
import threading
import time


def background_monitor_q(limit_q, kiruna_watcher, bot_token, bot_chatID):
    MODE = 'both'
    print("start monitor", flush=True)
    print(f"limit_q {limit_q} utc_shift:{kiruna_watcher.utc_shift}", flush=True)
    calculator = KIndexCalculator()

    # for user in get_all_users_to_notify():
    #    telegram_bot_sendtext(bot_token, user[0], "observing q has been started")
    t = threading.current_thread()
    while getattr(t, "do_run", True):
        try:
            telegram = calculator.get_users_to_notify(MODE, kiruna_watcher, bot_chatID, limit_q)
            for t in telegram:
                print("send notification for " + str(t[0]), flush=True)
                telegram_bot_sendtext(bot_token, t[0], t[1])
            time.sleep(60)
        except Exception as e:
            print("monitor !exception!", flush=True)
            print(e, flush=True)
            time.sleep(60)
    calculator.stop()
    print("stopping monitor", flush=True)


def get_all_users_to_notify():
    db = DBClient()
    users = db.execute_fetch_all("SELECT telegram FROM users WHERE telegramnotification = true ", ())
    db.close_connection()
    return users
