alter table events
    add column status varchar(20) not null default 'PLANNED'
        check (status in ('PLANNED', 'COMPLETED'));
