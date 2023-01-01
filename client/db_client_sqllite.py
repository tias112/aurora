import sqlite3
import os
from os.path import expanduser

class DBClient:
    def __init__(self):
        self.__db = expanduser("~") + os.sep + "SiteAurora1.db"
        if not os.path.isfile(self.__db):
            print("[WARNING]: No db found, creating a new one.")
            connection = sqlite3.connect(self.__db)
            connection.execute(
                "CREATE TABLE Users ('telegram' TEXT NOT NULL UNIQUE, 'min_q' INTEGER NOT NULL DEFAULT 3, 'telegramnotification' BOOLEAN NOT NULL DEFAULT TRUE, PRIMARY KEY (telegram));")
            connection.close()
        self.__connection = sqlite3.connect(self.__db, check_same_thread=False)

    def close_connection(self):
        self.__connection.close()

    def execute_query(self, query, parameters):
        self.__connection.execute(query, parameters)
        self.__connection.commit()

    def execute_fetch_all(self, query, parameters):
        saved_sites = self.__connection.execute(query, parameters).fetchall()
        self.__connection.commit()
        return saved_sites
