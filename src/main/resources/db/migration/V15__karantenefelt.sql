alter table melding_status add column karantene_til timestamp with time zone;
alter table melding_status add column status_type varchar(10);

create index melding_status_type_karantene on melding_status(status_type, karantene_til);
