alter table melding drop column status;
alter table oppgave drop column status;
alter table brev drop column status;
alter table godskriv_opptjening drop column status;

alter table melding rename column status_type to status;
alter table oppgave rename column status_type to status;
alter table brev rename column status_type to status;
alter table godskriv_opptjening rename column status_type to status;
