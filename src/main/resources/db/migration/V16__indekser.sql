drop index melding_status_type_karantene;
create index melding_status_type_karantene on melding_status(status_type, karantene_til ASC) where karantene_til is not null;
create index melding_Status_type_id on melding_status(status_type, id ASC);
