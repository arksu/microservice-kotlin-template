create table if not exists test
(
    id      bigint auto_increment primary key,
    name    text        not null,
    created timestamp not null default now()
);