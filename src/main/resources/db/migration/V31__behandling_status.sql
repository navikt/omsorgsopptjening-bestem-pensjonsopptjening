alter table behandling add column status varchar(50);
alter table behandling add column statushistorikk jsonb;

--Migrate existing rows to 'Vilkårsvurdert' to avoid nullability. No information is lost as the existing functionality
--for status 'stoppet' shows no trace of usage in prod (all behandlinger have stoppet = null).
update behandling
set status = 'Vilkårsvurdert',
    statushistorikk = jsonb_build_array(jsonb_build_object('type', 'Vilkårsvurdert', 'tidspunkt', opprettet));

alter table behandling alter column status set not null;
alter table behandling alter column statushistorikk set not null;
