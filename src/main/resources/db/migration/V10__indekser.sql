create index behandling_omsorgsmottaker on behandling (omsorgsmottaker);
create index behandling_omsorgsyter on behandling (omsorgsyter);
create unique index melding_status_id on melding_status(id);
create index melding_opprettet on melding(opprettet);