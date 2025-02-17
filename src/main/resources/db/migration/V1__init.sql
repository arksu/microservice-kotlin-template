create table if not exists users
(
    id      bigint auto_increment primary key,
    name    text        not null,
    created timestamp not null default now()
);