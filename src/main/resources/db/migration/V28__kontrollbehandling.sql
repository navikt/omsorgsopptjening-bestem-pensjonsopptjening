create table kontrollbehandling
(
    kontrollId uuid primary key default uuid_generate_v4(),
    opprettet timestamptz default now() not null,
    status VARCHAR not null,
    statushistorikk jsonb not null,
    referanse VARCHAR not null,
    karantene_til timestamptz,
    lockId uuid,
    lockTime timestamptz,
    originalId uuid not null references behandling(id),
    id uuid,
    omsorgs_ar INTEGER,
    omsorgsyter VARCHAR,
    omsorgsmottaker VARCHAR,
    omsorgstype VARCHAR,
    grunnlag jsonb,
    vilkarsvurdering jsonb,
    utfall jsonb,
    kafkaMeldingId uuid references melding(id)
);

create index kontrollbehandling_lockId on kontrollbehandling(lockId) where lockId is not null;
create unique index kontrollbehandling_behandlingId_referanse on kontrollbehandling(originalId, referanse);
create index kontrollbehandling_omsorgsyter on kontrollbehandling(omsorgsyter);
create index kontrollbehandling_omsorgsmottaker on kontrollbehandling(omsorgsmottaker);
create index kontrollbehandling_status on kontrollbehandling(kontrollId, status);
create index kontrollbehandling_status_retry on kontrollbehandling(kontrollId, status, karantene_til) where karantene_til is not null;