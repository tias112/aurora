import pandas as pd
from sklearn import preprocessing
from sklearn.tree import DecisionTreeClassifier
import logging
import threading

from client.db_client import DBClient

def max_amplitude(subrange):
    return max(subrange) - min(subrange)
def get_K_index(delta):
    if 0<delta<15:
        return 0
    if 15<=delta<30:
        return 1
    if 30<=delta<60:
        return 2
    if 60<delta<120:
        return 3
    if 120<delta<210:
        return 4
    if 210<delta<360:
        return 5
    if 360<delta<600:
        return 6
    if 600<delta<990:
        return 7
    if 990<delta<1500:
        return 8
    if 1500<delta:
        return 9
    return 0

def get_probability(q, bz):
    if 0<q<3:
        return 'low'
    if 2<q<7 and bz <-7:
        return 'high'
    if 2<q<7 and bz >-7:
        return 'medium'
    if 6<q<10:
        return 'high'


class KIndexCalculator:
    def __init__(self):
        def encode_labels(df):
            le = preprocessing.LabelEncoder()
            for column in df.columns:
                if df[column].dtype == type(object):
                    df[column] = le.fit_transform(df[column])
            return df
        print("init KIndexCalculator", flush=True)
        self.__db = DBClient()
        training_df = pd.read_csv('maggraphs/files/train_data.csv')
        kiruna_Q = training_df["kiruna_Q"]
        training_df = training_df.drop('kiruna_Q', axis=1)
        training_df = encode_labels(training_df)
        self.model = DecisionTreeClassifier().fit(training_df.values, kiruna_Q)
        self.prev_q=0
        self.prev_bz=0
        self.prev_grad=0
        self.last_notify=0

    def get_q(self, mode, xy_data):
        q = 0
        bz = self.calculate_min_bz(xy_data)
        bz_current = self.calculate_current_bz(xy_data)
        if mode=="ml":
            q = self.calculate_q_by_ml(xy_data)
            print("q: ",q)
        elif mode=="formula":
            q = self.calculate_q_by_ml(xy_data)
            print("q: ",q)
        elif mode=="both":
            q = self.calculate_q_by_ml(xy_data)
            q_form = self.calculate_q_by_formula(xy_data)
            print(f"ml:{q} formula:{q_form} bz: {bz}", flush=True)
            q = max(q, q_form)
        return  {'q': q, 'bz': bz, 'bz_current': bz_current}

    def calculate_min_bz(self, xy_data):
        #print(xy_data['bz_window'])
        return min(xy_data['bz_window'])

    def calculate_current_bz(self, xy_data):
        #print(xy_data['bz_window'])
        return min(xy_data['bz_current'])

    def calculate_q_by_formula(self, xy_data):
        x_window_30 = xy_data['x_window_30']
        y_window_30 = xy_data['y_window_30']
        x_window_15 = xy_data['x_window_15']
        y_window_15 = xy_data['y_window_15']

        K_index = get_K_index(max_amplitude(x_window_30))
        if (K_index > 3):
            test_q = {
             'x_range': max_amplitude(x_window_15),
             'y_range': max_amplitude(y_window_15),
             'z_range': max_amplitude(x_window_15),
             'max_q': get_K_index(max_amplitude(x_window_15))
            }
            return self.calculate_q_3(test_q, 0, 0)
        else:
            test_q = {
             'x_range': max_amplitude(x_window_30),
             'y_range': max_amplitude(y_window_30),
             'z_range': max_amplitude(x_window_30),
             'max_q': get_K_index(max_amplitude(x_window_30))
            }
            return self.calculate_q_3(test_q, 0, 0)

    def calculate_q_by_ml(self, xy_data):
        xAmp_30 = max_amplitude(xy_data['x_window_30'])
        xAmp_15 = max_amplitude(xy_data['x_window_15'])
        yAmp_15 = max_amplitude(xy_data['y_window_15'])
        amplitudes = [xAmp_30,
                        max_amplitude(xy_data['y_window_30']),
                        xAmp_15,
                        yAmp_15,
                        get_K_index(xAmp_30),
                        get_K_index(xAmp_15),
                        get_K_index(yAmp_15)]

        prediction = self.model.predict([amplitudes])
        return prediction[0]

    def calculate_gradient(self, xy_data, xy_data_before):
        xAmp_15 = max_amplitude(xy_data['x_window_15'])
        xAmp_from_30_to_15 = max_amplitude(xy_data_before['x_window_15'])
        K_index_prev = get_K_index(xAmp_from_30_to_15)
        K_index = get_K_index(xAmp_15)
        if K_index>K_index_prev:
            return abs(K_index-K_index_prev)
        return 0

    def get_users_to_notify(self, mode, kiruna_watcher, bot_chatID, limit_q):
        notify_users = []
        data = self.get_q(mode, kiruna_watcher.get_data())
        q = data['q']
        bz = data['bz']
        bz_current = data['bz_current']
        grad = self.calculate_gradient(kiruna_watcher.get_data(), kiruna_watcher.get_data_before())
        if bz_current < self.prev_bz:
            users = self.__db.execute_fetch_all(
                        "SELECT telegram FROM users WHERE bz_notify=true and max_bz > %s ",
                        (bz_current,))
            for t in users:
                notify_users.append([t[0], "possible aurora in ~1.5h, Bz:" + str(bz_current)])

        if q > self.prev_q:
            users = self.__db.execute_fetch_all(
                        "SELECT telegram FROM users WHERE telegramnotification = true and min_q <= %s ",
                        (q,))
            for t in users:
                notify_users.append([t[0], "probability for aurora: q="+str(q)+" ( " + get_probability(q, bz) + " Bz:"+ str(bz)+ " )"])

            #notify on high gradient
            if grad!=self.prev_grad and grad>=2 and self.last_notify>15:
                for t in users:
                    notify_users.append([t[0], "high q gradient: +"+str(grad)+" q="+str(q)])
                self.last_notify = 0
        self.last_notify+=1
        self.prev_q=q
        self.prev_grad=grad
        self.prev_bz=bz_current
        return notify_users

    def calculate_q_1(test_q):
        xQ = get_K_index(test_q['x_range'])
        yQ = get_K_index(test_q['y_range'])
        zQ = get_K_index(test_q['z_range'])
        q = max(xQ,yQ)
        return q
    def calculate_q_2(test_q, avgX,avgY):
        xQ = get_K_index(test_q['x_range'])
        yQ = get_K_index(test_q['y_range'])

        x_avgQ = get_K_index(abs(test_q['max_x']-avgX))
        y_avgQ = get_K_index(abs(test_q['max_y']-avgY))
        q = max(xQ,yQ,x_avgQ-1,y_avgQ-1)
        return q

    def calculate_q_3(self,test_q, avgX,avgY):
        xQ = get_K_index(test_q['x_range'])
        yQ = get_K_index(test_q['y_range'])
        zQ = get_K_index(test_q['z_range'])
        maxQ = test_q['max_q']
        q = max(xQ,yQ,zQ)
        return max(q, maxQ)

    def calculate_q_4(self, test_q, test_q_prev):
        if 'max_x' in test_q_prev.keys() and 'max_x' in test_q.keys():
            xQ = get_K_index(max(test_q['max_x'], test_q_prev['max_x']) - min(test_q['min_x'], test_q_prev['min_x']))
            yQ = get_K_index(max(test_q['max_y'], test_q_prev['max_y']) - min(test_q['min_y'], test_q_prev['min_y']))
            zQ = get_K_index(test_q['z_range'])
            #print([test_q['max_x'], test_q_prev['max_x']])
            #print([test_q['min_x'], test_q_prev['min_x']])
            maxQ = test_q['max_q']
            q = max(xQ,yQ,zQ)
            return max(q, maxQ)
        elif 'max_x' in test_q_prev.keys():
            return self.calculate_q_3(test_q_prev, 0, 0)
        elif 'max_x' in test_q.keys():
            return self.calculate_q_3(test_q, 0, 0)
        return 0

    def stop(self):
        self.__db.close_connection()