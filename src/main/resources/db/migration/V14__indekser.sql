drop index melding_status_type_karantene;
-- create index melding_status_type_karantene on melding_status((status->>'type'),((status->>'karanteneTil')::timestamptz));