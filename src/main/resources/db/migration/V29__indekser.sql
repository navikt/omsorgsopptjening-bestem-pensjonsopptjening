create index melding_status_id_nolock on melding(status, id ASC) where lockId is null
