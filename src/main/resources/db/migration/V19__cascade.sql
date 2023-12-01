alter table melding_status
    drop constraint melding_status_id_fkey,
    add foreign key (id) references melding(id) on delete cascade;