CREATE SEQUENCE person_seq INCREMENT 50;
CREATE TABLE Person
(
    PERSON_ID     BIGINT primary key       not null,
    FODSELSAR     VARCHAR(11)              not null,
    TIMESTAMP     TIMESTAMP with time zone not null
);

CREATE SEQUENCE fnr_seq INCREMENT 50;
CREATE TABLE FNR
(
    FNR_ID    BIGINT primary key       not null,
    FNR       VARCHAR(11)              not null,
    PERSON_ID BIGINT                   not null,
    GJELDENDE BOOLEAN                  not null,
    TIMESTAMP TIMESTAMP with time zone not null,
    CONSTRAINT fk_person
        FOREIGN KEY (PERSON_ID)
            REFERENCES Person (PERSON_ID)
);