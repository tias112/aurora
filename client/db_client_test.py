import os
from os.path import expanduser

import os
import psycopg2
import numpy as np
from psycopg2.extensions import register_adapter, AsIs
psycopg2.extensions.register_adapter(np.int64, psycopg2._psycopg.AsIs)

DATABASE_URL = os.environ['DATABASE_URL']
try:
    #__connection = psycopg2.connect(DATABASE_URL, sslmode='require')
    __connection = psycopg2.connect(host="localhost", database="mydb", user="aurora1", password="aurora")
    cur = __connection.cursor()
    cur.execute("select * from information_schema.tables where table_name=%s", ('users',))
    if not bool(cur.rowcount):
        print("[WARNING]: No db found, creating a new one.")
        cur.execute(
            "CREATE TABLE users (telegram VARCHAR(255) PRIMARY KEY, min_q INTEGER NOT NULL DEFAULT 3, is_new bool NULL DEFAULT true, telegramnotification bool NULL DEFAULT true, max_bz INTEGER NOT NULL DEFAULT -5, bz_notify bool NULL DEFAULT false);")
    cur.close()
    __connection.commit()
except (Exception, psycopg2.DatabaseError) as error:
        print(error)

