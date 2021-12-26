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
   python start_observer.py <mode> <freq_sec> <bot_token> <bot_chat_id>
   mode: ml/formula/both freq_sec: frequency in seconds  (minimum: 60)
   bot_token: token of telegram bot bot_chat_id: chat_id


