from kiruna.mag_client import MagWatcher
from kiruna.k_calculator import KIndexCalculator
from kiruna.k_calculator import get_probability
from client.telegram_bot import telegram_bot_sendtext
import time
import sys

if len(sys.argv)!=5:
    print("Usage: python start_observer.py <mode> <freq_sec> <bot_token> <bot_chat_id>")
    sys.exit(2)
bot_token = sys.argv[4]
bot_chatID = sys.argv[3]
mode = sys.argv[1]
freq = int(sys.argv[2])

watcher = MagWatcher(freq = freq)
calculator = KIndexCalculator()
watcher.service()
prev_q=0
while True:
    try:
        q=calculator.get_q(mode, watcher.get_data())
        if prev_q!=q and q>2:
            #bot_token = '5039169534:AAHfWFIAYkLfGIhx02epDj9RM3S_R5cwEsY'
            #bot_chatID = '-773104628'
            test = telegram_bot_sendtext(bot_token, bot_chatID, "probability for aurora: "+ get_probability(q)+"(q="+str(q)+")")
        prev_q=q
        time.sleep(60)
    except Exception as e:
        print(e)
        time.sleep(5)