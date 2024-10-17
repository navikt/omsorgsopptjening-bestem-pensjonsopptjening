drop table if exists kontrollbehandling; --old iteration

create table kontrollmelding
(
    id uuid primary key default uuid_generate_v4(),
    opprettet timestamptz default now() not null,
    status VARCHAR not null,
    statushistorikk jsonb not null,
    referanse VARCHAR not null,
    karantene_til timestamptz,
    lockId uuid,
    lockTime timestamptz,
    kafkameldingid uuid not null references melding(id),
    omsorgs_ar INTEGER not null
);

create unique index kontrollmelding_kafkameldingid_referanse on kontrollmelding(kafkameldingid, referanse);
create index kontrollmelding_lockId on kontrollmelding(lockId) where lockId is not null;
create index kontrollmelding_status on kontrollmelding(id, status);
create index kontrollmelding_status_retry on kontrollmelding(id, status, karantene_til) where karantene_til is not null;

create table kontrollbehandling
(
    id uuid primary key default uuid_generate_v4(),
    opprettet timestamptz default now() not null,
    omsorgs_ar INTEGER not null,
    omsorgsyter VARCHAR not null,
    omsorgsmottaker VARCHAR not null,
    omsorgstype VARCHAR not null,
    grunnlag jsonb not null,
    vilkarsvurdering jsonb not null,
    utfall jsonb not null,
    kafkaMeldingId uuid references kontrollmelding(id),
    referanse VARCHAR not null,
    godskriv jsonb,
    oppgave jsonb,
    brev jsonb
);

create index kontrollbehandling_omsorgsyter on kontrollbehandling(omsorgsyter);
create index kontrollbehandling_omsorgsmottaker on kontrollbehandling(omsorgsmottaker);
create index kontrollbehandling_referanse on kontrollbehandling(referanse);
