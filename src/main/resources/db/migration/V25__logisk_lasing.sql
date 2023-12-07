alter table brev add column lockId uuid;
alter table brev add column lockTime timestamptz;

create index brev_lockId_lockTime on melding(lockId,lockTime) where lockId is not null;