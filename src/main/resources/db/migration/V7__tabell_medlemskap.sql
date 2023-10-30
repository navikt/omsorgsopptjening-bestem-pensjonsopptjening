/*
Tabellen populeres med et uttrekk fra Infotrygd slik at vi får gjort en tilsvarende sjekk på "medlem i folketrygden"
som har blitt gjort tidligere. Tabellen er ment å brukes som en nødløsning inntil en fullverdig løsning for
medlemskap er implementert.
*/

create table medlemskap
(
    fnr varchar(11) primary key,
    pensjonstrygdet varchar(5)
);