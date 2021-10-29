
--spring-batch-config/--
create table if not exists BATCH_JOB_INSTANCE
(
    JOB_INSTANCE_ID bigint       not null primary key,
    VERSION         bigint,
    JOB_NAME        varchar(100) not null,
    JOB_KEY         varchar(32)  not null,
    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
);

create table if not exists BATCH_JOB_EXECUTION
(
    JOB_EXECUTION_ID           bigint        not null primary key,
    VERSION                    bigint,
    JOB_INSTANCE_ID            bigint        not null,
    CREATE_TIME                timestamp     not null,
    START_TIME                 timestamp default null,
    END_TIME                   timestamp default null,
    STATUS                     varchar(10),
    EXIT_CODE                  varchar(2500),
    EXIT_MESSAGE               varchar(2500),
    LAST_UPDATED               timestamp,
    JOB_CONFIGURATION_LOCATION varchar(2500) null,
    constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
        references BATCH_JOB_INSTANCE (JOB_INSTANCE_ID)
);

create table if not exists BATCH_JOB_EXECUTION_PARAMS  (
	JOB_EXECUTION_ID bigint not null,
	TYPE_CD varchar(6) not null,
	KEY_NAME varchar(100) not null,
	STRING_VAL varchar(250),
	DATE_VAL timestamp default null ,
	LONG_VAL bigint,
	DOUBLE_VAL DOUBLE PRECISION,
	IDENTIFYING char(1) not null,
	constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

create table if not exists BATCH_STEP_EXECUTION
(
    STEP_EXECUTION_ID  bigint       not null primary key,
    VERSION            bigint       not null,
    STEP_NAME          varchar(100) not null,
    JOB_EXECUTION_ID   bigint       not null,
    START_TIME         timestamp    not null,
    END_TIME           timestamp default null,
    STATUS             varchar(10),
    COMMIT_COUNT       bigint,
    READ_COUNT         bigint,
    FILTER_COUNT       bigint,
    WRITE_COUNT        bigint,
    READ_SKIP_COUNT    bigint,
    WRITE_SKIP_COUNT   bigint,
    PROCESS_SKIP_COUNT bigint,
    ROLLBACK_COUNT     bigint,
    EXIT_CODE          varchar(2500),
    EXIT_MESSAGE       varchar(2500),
    LAST_UPDATED       timestamp,
    constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

create table if not exists BATCH_STEP_EXECUTION_CONTEXT
(
    STEP_EXECUTION_ID  bigint        not null primary key,
    SHORT_CONTEXT      varchar(2500) not null,
    SERIALIZED_CONTEXT text,
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
        references BATCH_STEP_EXECUTION (STEP_EXECUTION_ID)
);

create table if not exists BATCH_JOB_EXECUTION_CONTEXT
(
    JOB_EXECUTION_ID   bigint        not null primary key,
    SHORT_CONTEXT      varchar(2500) not null,
    SERIALIZED_CONTEXT text,
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

create sequence if not exists BATCH_STEP_EXECUTION_SEQ maxvalue 9223372036854775807 no cycle;
create sequence if not exists BATCH_JOB_EXECUTION_SEQ maxvalue 9223372036854775807 no cycle;
create sequence if not exists BATCH_JOB_SEQ maxvalue 9223372036854775807 no cycle;
--/spring-batch-config--