alter table melding_status add column kort_status varchar(10) not null;
alter table oppgave_status add column kort_status varchar(10) not null;
alter table godskriv_opptjening_status add column kort_status varchar(10) not null;
alter table brev add column kort_status varchar(10) not null;