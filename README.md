# aurora

K index realtime calculation based on kiruna magnetogram data from www2.irf.se. If value of K index > 3 then message is
sent to telegram chat.

1. setup python
    - Ensure Python 3 is installed on your system
    - Create a virtual environment: `python -m venv venv`
    - Start the virtual environment: `venv/Scripts/activate`
    - Check install tooling is up to date `python -m pip install -U pip wheel setuptools`
    - Install the requirements `pip install -r requirements.txt`
2. create .env file or just `cp .env.example .env` with parameters :
   - Q_LIMIT: default minimum q for reporting
   - FREQ_SEC: interval in seconds to collect data from IRF and NOA web sites (minimum: 60)
   - TIME_ZONE: time zone of application with IRF timezone which is UTC (default: 0, if app is running on server with shift from UTC then set time_zone. For example, in timezone (UTC +2)) (TODO: be calculated based on latest timestamp in kiruna snapshot)
   - BOT_TOKEN: token of telegram bot
   - BOT_CHAT_ID: chat_id for logging info
   - NIGHT_TIME: optional night time start/end (for ex : 17:5)
3. start observer
```python start_observer.py ```


