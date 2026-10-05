-- =========================================================
-- Culture of house: basic schema (multi clubs)
-- =========================================================

create table clubs
(
    id         bigserial primary key,
    name       varchar(255) not null,
    address    varchar(500),
    created_at timestamp    not null default now()
);

create table users
(
    id            bigserial primary key,
    club_id       bigint references clubs (id), -- NULL for SUPER_ADMIN
    login         varchar(100) not null unique,
    password_hash varchar(255) not null,
    role          varchar(20)  not null
        check (role in ('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')),
    created_at    timestamp    not null default now()
);

-- Directory of visitor categories — common for all clubs (without club_id).
create table visitor_categories
(
    id        bigserial primary key,
    name      varchar(150) not null,
    age_group varchar(5)   not null
        check (age_group in ('CHILD', 'TEEN', 'ADULT'))
);

-- Directory of disability categories — common for all clubs (without club_id).
create table disability_categories
(
    id               bigserial primary key,
    name             varchar(150) not null,
    discount_percent numeric(3)   not null default 0
        check (discount_percent >= 0 and discount_percent <= 100)
);

-- Ticket denominations — per club
create table ticket_types
(
    id            bigserial primary key,
    club_id       bigint       not null references clubs (id),
    nominal_value numeric(10, 2), -- NULL - receipt (the amount is entered manually)
    name          varchar(100) not null,
    is_active     boolean      not null default true
);

create table events
(
    id               bigserial primary key,
    club_id          bigint         not null references clubs (id),
    event_date       date           not null,
    event_start_time timestamp,
    event_end_time   timestamp,
    name             varchar(255)   not null,
    type             varchar(10)    not null
        check (type in ('PAID', 'FREE')),
    venue            varchar(255),
    ticket_price     numeric(10, 2),
    revenue          numeric(12, 2) not null default 0,
    comment          text,
    created_at       timestamp      not null default now(),
    updated_at       timestamp      not null default now()
);

-- There may be several responsible employees at the event.
create table event_responsible_users
(
    event_id bigint not null references events (id) on delete cascade,
    user_id  bigint not null references users (id),
    primary key (event_id, user_id)
);

create table event_attendance
(
    id          bigserial primary key,
    event_id    bigint    not null references events (id) on delete cascade,
    category_id bigint    not null references visitor_categories (id),
    count       integer   not null check (count > 0),
    created_at  timestamp not null default now()
);

create table event_disability_attendance
(
    id          bigserial primary key,
    event_id    bigint    not null references events (id) on delete cascade,
    category_id bigint    not null references disability_categories (id),
    count       integer   not null check (count > 0),
    created_at  timestamp not null default now()
);

create table ticket_movements
(
    id              bigserial primary key,
    club_id         bigint      not null references clubs (id),
    ticket_type_id  bigint      not null references ticket_types (id),
    date            date        not null,
    type            varchar(15) not null
        check (type in ('REPLENISHMENT', 'SALE', 'ADJUSTMENT')),
    quantity        integer     not null,
    amount          numeric(12, 2) not null,
    comment         text,
    source_event_id bigint references events (id) on delete cascade,
    created_at      timestamp   not null default now()
);

-- No more than one auto-generated sale of tickets of a given denomination for an event
create unique index uq_ticket_movements_auto_sale
    on ticket_movements (source_event_id, ticket_type_id) where type = 'SALE' and source_event_id is not null;

create table cash_operations
(
    id              bigserial primary key,
    club_id         bigint         not null references clubs (id),
    date            date           not null,
    type            varchar(20)    not null
        check (type in ('INCOME_EVENT', 'INCOME_OTHER', 'EXPENSE', 'WITHDRAWAL', 'ADJUSTMENT')),
    amount          numeric(12, 2) not null,
    source_event_id bigint references events (id) on delete cascade,
    comment         text,
    created_by      bigint references users (id),
    created_at      timestamp      not null default now()
);

-- No more than one auto-generated cash receipt entry per event
create unique index uq_cash_operations_auto_income
    on cash_operations (source_event_id) where type = 'INCOME_EVENT' and source_event_id is not null;
