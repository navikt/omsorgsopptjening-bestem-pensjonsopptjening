-- Bound lock wait so startup doesn't hang if another replica keeps table locks.
SET LOCAL lock_timeout = '5s';

alter table melding add column endringshistorikk jsonb not null default '[]'::jsonb;
alter table oppgave add column endringshistorikk jsonb not null default '[]'::jsonb;
alter table godskriv_opptjening add column endringshistorikk jsonb not null default '[]'::jsonb;
alter table brev add column endringshistorikk jsonb not null default '[]'::jsonb;
alter table behandling add column endringshistorikk jsonb not null default '[]'::jsonb;
alter table kontrollmelding add column endringshistorikk jsonb not null default '[]'::jsonb;
alter table kontrollbehandling add column endringshistorikk jsonb not null default '[]'::jsonb;
