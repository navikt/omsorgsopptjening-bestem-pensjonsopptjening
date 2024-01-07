alter table melding drop constraint unique_correlation_innlesing;

create unique index unique_correlation_innlesing on melding(correlation_id, innlesing_id) where status <> 'Stoppet';