CREATE TABLE behandling
(
    id bigserial primary key    not null,
    omsorgs_ar INTEGER          not null,
    omsorgsyter VARCHAR         not null,
    omsorgstype VARCHAR         not null,
    grunnlag json               not null,
    vilkarsvurdering json       not null,
    utfall json                 not null
);