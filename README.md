# aurora

1. download file https://www2.irf.se/maggraphs/rt.txt or local.
2. load current sync point
3. setup job -> every one minute scan url.
4. read csv since last timestamp log records.
5. configure filter for logdate https://www.elastic.co/guide/en/logstash/current/plugins-filters-date.html. 6 use grok
   for parsing spring log or direct event API?
7. setup anomaly job.

TODO:
- JobsHistoryManager
- try initialize lastTimestamp where job stopped (from last file)
- emote: Support for password authentication was removed on August 13, 2021. Please use a personal access token instead. remote: Please see https://github.blog/2020-12-15-token-authentication-requirements-for-git-operations/ for more information. Authentication failed for 'https://github.com/tias112/aurora/
