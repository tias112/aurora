# aurora
anomaly detection is based on rt.txt provided by www2.irf.se
1. setup local/http mode
2. start job
4. read csv since last timestamp log records.
5. configure filter for logdate https://www.elastic.co/guide/en/logstash/current/plugins-filters-date.html. 6 use grok
   for parsing spring log or direct event API?
7. setup anomaly job.

TODO:
- JobsHistoryManager: no check for jobs 'd been started later than 1h 
- save lastTimestamp to db/file where job stopped (from last file)
- emote: Support for password authentication was removed on August 13, 2021. Please use a personal access token instead. remote: Please see https://github.blog/2020-12-15-token-authentication-requirements-for-git-operations/ for more information. Authentication failed for 'https://github.com/tias112/aurora/
