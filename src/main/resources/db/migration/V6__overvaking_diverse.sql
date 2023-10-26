alter table melding_status drop column kort_status;
alter table oppgave_status drop column kort_status;
alter table godskriv_opptjening_status drop column kort_status;

alter table melding alter column opprettet drop default;

create index melding_status_type on melding_status ((status->>'type'));
create index oppgave_status_type on oppgave_status ((status->>'type'));
create index godskriv_opptjening_status_type on godskriv_opptjening_status ((status->>'type'));
create index brev_status_type on brev_status ((status->>'type'));