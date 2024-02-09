
INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('391911767',3,true,true,false,0)


CREATE TABLE users (
	telegram varchar(255) NOT NULL,
	min_q int4 NOT NULL DEFAULT 3,
	is_new bool NULL DEFAULT true,
	telegramnotification bool NULL DEFAULT true,
	bz_notify bool NULL DEFAULT false,
	max_bz int4 NULL DEFAULT '-5'::integer,
	CONSTRAINT users_pkey PRIMARY KEY (telegram)
);

INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('391911767',3,true,true,false,0);
INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('421673318',6,true,true,false,0);
INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('287725775',7,true,true,false,0);
INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('-677324504',3,true,true,false,0);
INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('143193483',6,true,true,false,0);
INSERT INTO users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES ('1781071627',4,true,true,true,-6);

#export DATABASE_URL=postgres://aurora:aurora@localhost:5432/mydb


#sudo -u postgres psql
#postgres=# create database mydb;
#postgres=# create user aurora with encrypted password 'aurora';
#postgres=# grant all privileges on database mydb to aurora;
