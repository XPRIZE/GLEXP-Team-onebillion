create table units
(
    unitid integer primary key,
    masterlistid int not null,
    unitIndex int not null,
    week int not null default 1,
    key text not null,
    icon text,
    params text,
    config text not null,
    target text not null,
    targetDuration real,
    passThreshold real,
    lang text,
    constraint unique_const unique(masterlistid,unitIndex) on conflict fail
);

create table unitinstances
(
    userid integer not null references users(userid) on delete restrict,
    unitid integer not null references units(unitid) on delete restrict,
    type integer not null,
    seqNo int not null,
    sessionid integer not null,
    score real not null default 0,
    elapsedTime int not null default 0,
    startTime big unsigned int not null default 0,
    endTime big unsigned int default 0,
    starColour integer not null default -1,
    foreign key(userid,sessionid) references sessions(userid,sessionid),
    constraint pkey primary key (userid,unitid,type,seqno) on conflict fail
);

create table sessions
(
    userid integer not null references users(userid) on delete restrict,
    sessionid integer not null,
    startTime big unsigned int not null,
    endTime big unsigned int default 0,
    day int not null,
    constraint pkey primary key (userid,sessionid) on conflict fail
);

create table users
(
    userid integer primary key,
    masterlistid integer not null default 1,
    name text not null,
    deleted int not null default 0
);

create table preferences
(
    name text primary key not null,
    val text
);

create table playzoneassets
(
    userid integer not null references users(userid) on delete restrict,
    assetid integer primary key,
    type integer not null default 1,
    thumbnail text,
    params text,
    createTime big unsigned int not null default 0
);