create index godskriv_opptjening_status_1 on godskriv_opptjening_status ((status->>'type'),(status->>'karanteneTil'));
