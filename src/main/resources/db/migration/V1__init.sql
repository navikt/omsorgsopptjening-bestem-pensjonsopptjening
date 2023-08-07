create extension if not exists "uuid-ossp";

create table melding
(
    id uuid primary key default uuid_generate_v4(),
    opprettet timestamptz default now() not null,
    melding json not null,
    correlation_id varchar not null
);

create table melding_status
(
    id uuid not null references melding(id),
    status json not null,
    statushistorikk json not null
);

create table behandling
(
    id uuid primary key default uuid_generate_v4(),
    opprettet timestamptz default now() not null,
    omsorgs_ar INTEGER  not null,
    omsorgsyter VARCHAR not null,
    omsorgsmottaker VARCHAR not null,
    omsorgstype VARCHAR not null,
    grunnlag json not null,
    vilkarsvurdering json not null,
    utfall json not null,
    kafkaMeldingId uuid not null references melding(id)
);

create table oppgave
(
    id uuid primary key default uuid_generate_v4(),
    opprettet timestamptz default now() not null,
    behandlingId uuid references behandling(id),
    meldingId uuid not null references melding(id),
    detaljer json not null
);

create table oppgave_status
(
    id uuid not null references oppgave(id),
    status json not null,
    statushistorikk json not null
);