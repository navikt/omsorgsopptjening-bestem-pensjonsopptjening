alter table oppgave_status add column status_type varchar(10);
alter table oppgave_status add column karantene_til timestamptz;
alter table godskriv_opptjening_status add column status_type varchar(10);
alter table godskriv_opptjening_status add column karantene_til timestamptz;
alter table brev_status add column status_type varchar(10);
alter table brev_status add column karantene_til timestamptz;
