create table endringslogg
(
    id uuid primary key default uuid_generate_v4(),
    endret_tidspunkt timestamp with time zone not null default now(),
    endret_av varchar not null,
    kilde varchar not null,
    handling varchar(50) not null,
    begrunnelse text,
    input jsonb not null,
    input_id uuid not null,
    resultat_id uuid,
    status varchar not null,
    detalj varchar(200),
    opprettet timestamp with time zone not null default now()
);

create index endringslogg_handling_idx on endringslogg (handling);
create index endringslogg_input_id_idx on endringslogg (input_id);
