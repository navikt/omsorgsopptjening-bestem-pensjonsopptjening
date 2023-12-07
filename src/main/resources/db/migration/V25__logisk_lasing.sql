alter table brev add column lockId uuid;
alter table brev add column lockTime timestamptz;

create index brev_lockId_lockTime on brev(lockId,lockTime) where lockId is not null;

alter table godskriv_opptjening add column lockId uuid;
alter table godskriv_opptjening add column lockTime timestamptz;

create index godskriv_opptjening_lockId_lockTime on godskriv_opptjening(lockId,lockTime) where lockId is not null;