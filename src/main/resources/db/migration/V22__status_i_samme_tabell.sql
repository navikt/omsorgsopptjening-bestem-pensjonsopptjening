drop table melding_status;
alter table melding add column status jsonb not null;
alter table melding add column statushistorikk jsonb not null;
alter table melding add column karantene_til timestamp with time zone;
alter table melding add column status_type varchar(10) not null;

drop table oppgave_status;
alter table oppgave add column status jsonb not null;
alter table oppgave add column statushistorikk jsonb not null;
alter table oppgave add column karantene_til timestamp with time zone;
alter table oppgave add column status_type varchar(10) not null;

drop table godskriv_opptjening_status;
alter table godskriv_opptjening add column status jsonb not null;
alter table godskriv_opptjening add column statushistorikk jsonb not null;
alter table godskriv_opptjening add column karantene_til timestamp with time zone;
alter table godskriv_opptjening add column status_type varchar(10) not null;

drop table brev_status;
alter table brev add column status jsonb not null;
alter table brev add column statushistorikk jsonb not null;
alter table brev add column karantene_til timestamp with time zone;
alter table brev add column status_type varchar(10) not null;

create index melding_status_id on melding(innlesing_id, status_type, id);
create index melding_status_karantene on melding(innlesing_id, status_type, karantene_til);

create index oppgave_status_id on oppgave(status_type, id);
create index oppgave_status_karantene on oppgave(status_type, karantene_til);

create index godskriv_opptjening_status_id on godskriv_opptjening(status_type, id);
create index godskriv_opptjening_status_karantene on godskriv_opptjening(status_type, karantene_til);

create index brev_status_id on brev(status_type, id);
create index brev_status_karantene on brev(status_type, karantene_til);


-- oppgave_status
-- godskriv_opptjening_status
-- brev_status
