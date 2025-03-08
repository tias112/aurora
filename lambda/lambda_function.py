import base64

import json
import re
import urllib
from common.noaa_client import NOAAClient
import os

print('Loading function')

import requests

import requests


def send_telegram_message(message):
    """
    Sends a message to a Telegram chat using the Telegram Bot API.

    :param bot_token: The token of the Telegram bot (from BotFather).
    :param chat_id: The unique chat ID to send messages to.
    :param message: The message to send.
    """
    bot_token = os.environ.get("TELEGRAM_BOT_TOKEN")
    chat_ids = os.environ.get("CHAT_ID").split(",")

    # Telegram API endpoint
    url = f"https://api.telegram.org/bot{bot_token}/sendMessage"
    for chat_id in chat_ids:
    # Parameters to be sent in the POST request
        chat_url = f"{url}?chat_id={chat_id}"
        payload = {
            'text': message
        }

        try:
            print("send to ", chat_id)
            send_text = 'https://api.telegram.org/bot' + bot_token + '/sendMessage?chat_id=' + chat_id + '&parse_mode=Markdown&text=' + message
            response = requests.get(send_text)
            # Send the POST request to the Telegram API

            if response.status_code == 200:
                print("Message sent successfully!")
            else:
                print(f"Failed to send message. Response: {response.text}")

        except Exception as e:
            print(f"An error occurred: {e}")



def lambda_handler(event, context):
    output = []
    noaa_client = NOAAClient()
    res =  noaa_client.process()
    min_bz = float(os.environ.get("MIN_BZ"))

    bz_window = [t['bz'] for t in res]
    last_hour = bz_window[-(int(len(bz_window)/2)):]
    try:
        for bz in last_hour:
            if float(bz) < min_bz:
                send_telegram_message(f"Possible aurora: {bz}")
                break
    except Exception as e:
        print(e)
    return {'records': output}
