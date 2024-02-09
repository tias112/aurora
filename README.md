# aurora

K index realtime calculation based on kiruna magnetogram data from www2.irf.se. If value of K index > 3 then message is
sent to telegram chat.

1. setup python
    - Ensure Python 3 is installed on your system
    - Create a virtual environment: `python -m venv venv`
    - Start the virtual environment: `venv/Scripts/activate`
    - Check install tooling is up to date `python -m pip install -U pip wheel setuptools`
    - Install the requirements `pip install -r requirements.txt`
2. start job:
   python start_observer.py <q_limit> <freq_sec> <time_zone> <bot_token> <bot_chat_id> <bz_shift>
    - q_limit: minimum q for reporting
    - freq_sec: frequency in seconds  (minimum: 60)
    - time_zone: time zone of application (default: 0)
    - bot_token: token of telegram bot
    - bot_chat_id: chat_id
    - bz_shift: shift of current Bz activity. usually its 90 min

example:
```python start_observer.py 0 60 3 5039169534:AAHfWFIAYkLfGIhx02epDj9RM3S_R5cwEsY -773104628 1```