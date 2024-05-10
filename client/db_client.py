import os
from os.path import expanduser

import os
import psycopg2
import numpy as np
from psycopg2.extensions import register_adapter, AsIs
psycopg2.extensions.register_adapter(np.int64, psycopg2._psycopg.AsIs)

class DBClient:
    def __init__(self):
        DATABASE_URL = os.environ['DATABASE_URL']
        try:
            self.__connection = psycopg2.connect(DATABASE_URL, sslmode='require')
            #self.__connection = psycopg2.connect(host="localhost", database="interstorage", user="user", password="password")
            cur = self.__connection.cursor()
            cur.execute("select * from information_schema.tables where table_name=%s", ('users',))
            print(cur.rowcount)
            if not bool(cur.rowcount):
                print("[WARNING]: No db found, creating a new one.")
                cur.execute(
                    "CREATE TABLE users (telegram VARCHAR(255) PRIMARY KEY, min_q INTEGER NOT NULL DEFAULT 3, is_new bool NULL DEFAULT true, telegramnotification bool NULL DEFAULT true, max_bz INTEGER NOT NULL DEFAULT -5, bz_notify bool NULL DEFAULT false);")
                cur.execute(
                    "INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('391911767',6,true,true,true,-15);");
                cur.execute(
                    "INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('421673318',6,true,true,true,-15);");
                cur.execute(
                    "INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('287725775',7,true,true,true,-15);")
		# aurora1 chat
                cur.execute(
                    "INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('-677324504',6,true,true,true,-15);")
                cur.execute(
                    "INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('143193483',6,true,true,true,-15);")
		#current
                cur.execute(
                    "INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('1781071627',5,true,true,true,-10);")
            cur.close()
            self.__connection.commit()
        except (Exception, psycopg2.DatabaseError) as error:
                print(error)

    def close_connection(self):
        self.__connection.close()

    def execute_query(self, query, parameters):
        cur = self.__connection.cursor()
        cur.execute(query, parameters)
        self.__connection.commit()
        cur.close()

    def execute_fetch_all(self, query, parameters):
        cur = self.__connection.cursor()
        cur.execute(query, parameters)
        saved_sites = cur.fetchall()
        self.__connection.commit()
        cur.close()
        return saved_sites
