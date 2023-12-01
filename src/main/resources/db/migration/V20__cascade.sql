alter table behandling
    drop constraint behandling_kafkameldingid_fkey,
    add foreign key (kafkameldingid) references melding(id) on delete cascade;