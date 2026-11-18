alter table melding add column endringshistorikk jsonb not null default '[]'::jsonb;
alter table oppgave add column endringshistorikk jsonb not null default '[]'::jsonb;
alter table godskriv_opptjening add column endringshistorikk jsonb not null default '[]'::jsonb;
alter table brev add column endringshistorikk jsonb not null default '[]'::jsonb;
alter table behandling add column endringshistorikk jsonb not null default '[]'::jsonb;
alter table kontrollmelding add column endringshistorikk jsonb not null default '[]'::jsonb;
alter table kontrollbehandling add column endringshistorikk jsonb not null default '[]'::jsonb;
