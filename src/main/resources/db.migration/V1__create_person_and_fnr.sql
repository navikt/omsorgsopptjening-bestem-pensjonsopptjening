CREATE SEQUENCE person_seq INCREMENT 50;
CREATE TABLE PERSON
(
    PERSON_ID     BIGINT primary key       not null,
    GJELDENDE_FNR VARCHAR(11)              not null,
    TIMESTAMP     TIMESTAMP with time zone not null
);

CREATE SEQUENCE fnr_seq INCREMENT 50;
CREATE TABLE FNR
(
    FNR_ID       BIGINT primary key       not null,
    FNR       VARCHAR(11)              not null,
    TIMESTAMP TIMESTAMP with time zone not null
);