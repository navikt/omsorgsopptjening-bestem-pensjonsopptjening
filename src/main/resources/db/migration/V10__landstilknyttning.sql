CREATE SEQUENCE landstilknytning_seq INCREMENT 50;
CREATE TABLE LANDSTILKNYTNING
(
    LANDSTILKNYTNING_ID BIGINT primary key not null,
    TYPE     VARCHAR(30) UNIQUE not null
);

INSERT INTO LANDSTILKNYTNING(LANDSTILKNYTNING_ID, TYPE)
VALUES (nextval('landstilknytning_seq'), 'EOS');

INSERT INTO LANDSTILKNYTNING(LANDSTILKNYTNING_ID, TYPE)
VALUES (nextval('landstilknytning_seq'), 'NASJONAL');



ALTER TABLE OMSORGSARBEID_PERIODE ADD LANDSTILKNYTNING VARCHAR (30) not null;
ALTER TABLE OMSORGSARBEID_PERIODE ADD CONSTRAINT fk_kilde FOREIGN KEY (LANDSTILKNYTNING) REFERENCES LANDSTILKNYTNING (TYPE)