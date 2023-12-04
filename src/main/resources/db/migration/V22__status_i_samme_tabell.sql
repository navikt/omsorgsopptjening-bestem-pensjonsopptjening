drop table melding_status;
alter table melding add column status jsonb not null;
alter table melding add column statushistorikk jsonb not null;
alter table melding add column karantene_til timestamp with time zone;
alter table melding add column status_type varchar(10) not null;

-- oppgave_status
-- godskriv_opptjening_status
-- brev_status
