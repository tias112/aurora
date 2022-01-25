# aurora

Telegram bot that monitors kiruna magnetogram data (www2.irf.se) and sends notifications of high values of Q.

1. setup python
   - Ensure Python 3 is installed on your system
   - Create a virtual environment: `python -m venv venv`
   - Start the virtual environment: `venv/Scripts/activate`
   - Check install tooling is up to date `python -m pip install -U pip wheel setuptools`
   - Install the requirements `pip install -r requirements.txt`
2. start telegram bot:
   python start_observer.py <q_limit> <freq_sec> <utc_shift_hours> <bot_token> <bot_chat_id>

   - q_limit: minimum q for reporting
   - freq_sec: frequency in seconds  (minimum: 60)
   - utc_shift_hours (default: 3)
   - bot_token: token of telegram bot
   - bot_chat_id: chat_id
   


