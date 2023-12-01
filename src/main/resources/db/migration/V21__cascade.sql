alter table oppgave
    drop constraint oppgave_meldingid_fkey,
    add foreign key (meldingid) references melding(id) on delete cascade;

alter table oppgave
    drop constraint oppgave_behandlingid_fkey,
    add foreign key (behandlingid) references behandling(id) on delete cascade;