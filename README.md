# aurora

1. download file https://www2.irf.se/maggraphs/rt.txt or local.
2. load current sync point
3. setup job -> every one minute scan url.
4. read csv since last timestamp log records.
5. configure filter for logdate https://www.elastic.co/guide/en/logstash/current/plugins-filters-date.html. 6 use grok
   for parsing spring log or direct event API?
7. setup anomaly job.

TODO:
- try initialize lastTimestamp where job stopped (from last file)
