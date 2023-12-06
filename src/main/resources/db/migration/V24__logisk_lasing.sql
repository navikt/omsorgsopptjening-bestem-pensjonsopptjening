alter table melding add column lockId uuid;
alter table melding add column lockTime timestamptz;

create index melding_lockId on melding(lockId) where lockId is not null;