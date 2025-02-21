create table if not exists test
(
    id      bigserial primary key,
    name    text        not null,
    created timestamp not null default now()
);