create table brev
(
    id uuid primary key default uuid_generate_v4(),
    opprettet timestamptz default now() not null,
    behandlingId uuid not null references behandling(id)
);

create table brev_status
(
    id uuid not null references brev(id),
    status json not null,
    statushistorikk json not null
);