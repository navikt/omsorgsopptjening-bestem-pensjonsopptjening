alter table oppgave_status add column status_type varchar(10);
alter table oppgave_status add column karantene_til timestamptz;
alter table godskriv_opptjening_status add column status_type varchar(10);
alter table godskriv_opptjening_status add column karantene_til timestamptz;
alter table brev_status add column status_type varchar(10);
alter table brev_status add column karantene_til timestamptz;

create index oppgave_status_type_id on oppgave_status (status_type, id);
create index oppgave_status_type_karantene on oppgave_status (status_type, karantene_til);
create index godskriv_opptjening_status_type_id on godskriv_opptjening_status(status_type, id);
create index godskriv_opptjening_status_type_karantene on godskriv_opptjening_status(status_type, karantene_til);
create index brev_status_type_id on brev_status (status_type, id);
create index brev_status_type_karantene on brev_status (status_type, karantene_til);