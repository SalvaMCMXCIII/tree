create table `tree`
(
    `id`    bigint auto_increment,
    `left`  int          not null,
    `right` int          not null,
    `level` tinyint      not null,
    `name`  varchar(120) not null,
    primary key (`id`)

);