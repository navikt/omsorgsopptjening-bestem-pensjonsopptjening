CREATE SEQUENCE omsorgstype_seq INCREMENT 50;
CREATE TABLE OMSORGSTYPE
(
    OMSORGSTYPE_ID BIGINT primary key not null,
    TYPE           VARCHAR(30) UNIQUE not null
);

INSERT INTO OMSORGSTYPE(OMSORGSTYPE_ID, TYPE)
VALUES (nextval('omsorgstype_seq'), 'BARNETRYGD');
INSERT INTO OMSORGSTYPE(OMSORGSTYPE_ID, TYPE)
VALUES (nextval('omsorgstype_seq'), 'HJELPESTONAD_SATS_3');
INSERT INTO OMSORGSTYPE(OMSORGSTYPE_ID, TYPE)
VALUES (nextval('omsorgstype_seq'), 'HJELPESTONAD_SATS_4');



CREATE SEQUENCE kilde_seq INCREMENT 50;
CREATE TABLE KILDE
(
    KILDE_ID BIGINT primary key not null,
    TYPE     VARCHAR(30) UNIQUE not null
);

INSERT INTO KILDE(KILDE_ID, TYPE)
VALUES (nextval('kilde_seq'), 'BA');
INSERT INTO KILDE(KILDE_ID, TYPE)
VALUES (nextval('kilde_seq'), 'INFOTRYGD');



CREATE SEQUENCE omsorgsarbeid_snapshot_seq INCREMENT 50;
CREATE TABLE OMSORGSARBEID_SNAPSHOT
(
    OMSORGSARBEID_SNAPSHOT_ID BIGINT primary key not null,
    OMSORGS_AR                SMALLINT           not null,
    OMSORGSYTER               BIGINT             not null,
    OMSORGSTYPE               VARCHAR(30)        not null,
    KILDE                     VARCHAR(30)        not null,
    KJORE_HASHE               VARCHAR(70)        not null,
    CONSTRAINT fk_omsorgsyter_snapshot FOREIGN KEY (OMSORGSYTER) REFERENCES PERSON (PERSON_ID),
    CONSTRAINT fk_omsorgstype FOREIGN KEY (OMSORGSTYPE) REFERENCES OMSORGSTYPE (TYPE),
    CONSTRAINT fk_kilde FOREIGN KEY (KILDE) REFERENCES KILDE (TYPE)
);



CREATE SEQUENCE omsorgsarbeid_sak_seq INCREMENT 50;
CREATE TABLE OMSORGSARBEID_SAK
(
    OMSORGSARBEID_SAK_ID      BIGINT primary key not null,
    OMSORGSARBEID_SNAPSHOT_ID BIGINT             not null,
    CONSTRAINT fk_snapshot_sak FOREIGN KEY (OMSORGSARBEID_SNAPSHOT_ID) REFERENCES OMSORGSARBEID_SNAPSHOT (OMSORGSARBEID_SNAPSHOT_ID)
);



CREATE SEQUENCE omsorgsarbeid_periode_seq INCREMENT 50;
CREATE TABLE OMSORGSARBEID_PERIODE
(
    OMSORGSARBEID_PERIODE_ID BIGINT primary key not null,
    OMSORGSARBEID_SAK_ID     BIGINT             not null,
    FOM                      DATE               not null,
    TOM                      DATE               not null,
    OMSORGSYTER              BIGINT             not null,
    PROSENT                  SMALLINT           not null,
    CONSTRAINT fk_omsorgsyter_periode FOREIGN KEY (OMSORGSYTER) REFERENCES PERSON (PERSON_ID),
    CONSTRAINT fk_sak_periode FOREIGN KEY (OMSORGSARBEID_SAK_ID) REFERENCES OMSORGSARBEID_SAK (OMSORGSARBEID_SAK_ID)
);



CREATE SEQUENCE omsorgsarbeidsmottaker_seq INCREMENT 50;
CREATE TABLE OMSORGSARBEIDSMOTTAKER
(
    OMSORGSARBEIDSMOTTAKER_ID BIGINT primary key not null,
    OMSORGSARBEID_PERIODE_ID  BIGINT             not null,
    PERSON_ID                 BIGINT             not null,
    CONSTRAINT fk_periode_omsorgsarbeidsmottaker FOREIGN KEY (OMSORGSARBEID_PERIODE_ID) REFERENCES OMSORGSARBEID_PERIODE (OMSORGSARBEID_PERIODE_ID),
    CONSTRAINT fk_person_omsorgsarbeidsmottaker FOREIGN KEY (PERSON_ID) REFERENCES PERSON (PERSON_ID),
    UNIQUE (OMSORGSARBEID_PERIODE_ID, PERSON_ID)
);



CREATE SEQUENCE status_seq INCREMENT 50;
CREATE TABLE STATUS
(
    STATUS_ID BIGINT primary key not null,
    TYPE      VARCHAR(30) UNIQUE not null
);

INSERT INTO STATUS(STATUS_ID, TYPE)
VALUES (nextval('status_seq'), 'FERDIG_BEHANDLET');
INSERT INTO STATUS(STATUS_ID, TYPE)
VALUES (nextval('status_seq'), 'TRENGER_INFORMASJON');



CREATE SEQUENCE omsorgsopptjeningsgrunnlag_seq INCREMENT 50;
CREATE TABLE OMSORGSOPPTJENINGSGRUNNLAG
(
    OMSORGSOPPTJENINGSGRUNNLAG_ID BIGINT primary key not null,
    OMSORGS_AR                    SMALLINT           not null,
    STATUS                        VARCHAR(30)        not null,
    CONSTRAINT fk_status FOREIGN KEY (STATUS) REFERENCES STATUS (TYPE)
);



CREATE SEQUENCE involverte_personer_seq INCREMENT 50;
CREATE TABLE INVOLVERTE_PERSONER
(
    INVOLVERTE_PERSONER_ID        BIGINT primary key not null,
    OMSORGSOPPTJENINGSGRUNNLAG_ID BIGINT             not null,
    PERSON_ID                     BIGINT             not null,
    CONSTRAINT fk_periode_omsorgsarbeidsmottaker FOREIGN KEY (OMSORGSOPPTJENINGSGRUNNLAG_ID) REFERENCES OMSORGSOPPTJENINGSGRUNNLAG (OMSORGSOPPTJENINGSGRUNNLAG_ID),
    CONSTRAINT fk_person_involverte FOREIGN KEY (PERSON_ID) REFERENCES PERSON (PERSON_ID),
    UNIQUE (OMSORGSOPPTJENINGSGRUNNLAG_ID, PERSON_ID)
);







