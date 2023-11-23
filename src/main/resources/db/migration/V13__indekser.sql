drop index godskriv_opptjening_status_1 ;
create index melding_status_type_karantene on melding_status((status->>'type'),(status->>'karanteneTil'));