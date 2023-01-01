-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
	telegram varchar(255) NOT NULL,
	min_q int4 NOT NULL DEFAULT 3,
	is_new bool NULL DEFAULT true,
	telegramnotification bool NULL DEFAULT true,
	bz_notify bool NULL DEFAULT false,
	max_bz int4 NULL DEFAULT '-5'::integer,
	CONSTRAINT users_pkey PRIMARY KEY (telegram)
);

INSERT INTO public.users (telegram,min_q,is_new,telegramnotification,bz_notify,max_bz) VALUES
	 ('391911767',3,true,true,false,0),
	 ('421673318',6,true,true,false,0),
	 ('287725775',7,true,true,false,0),
	 ('-677324504',3,true,true,false,0),
	 ('143193483',6,true,true,false,0),
	 ('1781071627',4,true,true,true,-6);