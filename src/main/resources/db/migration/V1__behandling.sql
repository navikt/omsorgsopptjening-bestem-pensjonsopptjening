create extension if not exists "uuid-ossp";

create TABLE behandling
(
    id uuid primary key default uuid_generate_v4(),
    opprettet timestamptz default now() not null,
    omsorgs_ar INTEGER  not null,
    omsorgsyter VARCHAR not null,
    omsorgsmottaker VARCHAR not null,
    omsorgstype VARCHAR not null,
    grunnlag json not null,
    vilkarsvurdering json not null,
    utfall json not null
);