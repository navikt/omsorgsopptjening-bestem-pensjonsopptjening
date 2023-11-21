create index oppgave_meldingId on oppgave(meldingid);
create index oppgave_behandlingId on oppgave(behandlingId);
create index godskriv_opptjening_behandlingId on godskriv_opptjening(behandlingId);
create index brev_behandlingId on brev(behandlingId);
create index godskriv_opptjening_status_id on godskriv_opptjening_status(id);
