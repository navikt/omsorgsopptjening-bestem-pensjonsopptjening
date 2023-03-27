DROP TABLE INVOLVERTE_PERSONER;
DROP SEQUENCE involverte_personer_seq;


CREATE SEQUENCE involverte_personer_seq INCREMENT 50;
CREATE TABLE INVOLVERTE_PERSONER
(
    OMSORGSOPPTJENINGSGRUNNLAG_ID BIGINT             not null,
    PERSON_ID                     BIGINT             not null,
    PRIMARY KEY(OMSORGSOPPTJENINGSGRUNNLAG_ID, PERSON_ID),
    CONSTRAINT fk_periode_omsorgsarbeidsmottaker FOREIGN KEY (OMSORGSOPPTJENINGSGRUNNLAG_ID) REFERENCES OMSORGSOPPTJENINGSGRUNNLAG (OMSORGSOPPTJENINGSGRUNNLAG_ID),
    CONSTRAINT fk_person_involverte FOREIGN KEY (PERSON_ID) REFERENCES PERSON (PERSON_ID)
);