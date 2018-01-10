create table units
(
    unitid integer primary key,
    masterlistid int not null,
    unitIndex int not null,
    level int not null default 1,
    awardStar int not null default -1,
    key text not null,
    icon text,
    params text,
    config text not null,
    target text not null,
    targetDuration real,
    passThreshold real,
    lang text,
    catAudio int default -1,
    startAudio int default -1,
    constraint unique_const unique(masterlistid,unitIndex) on conflict fail
);

create table unitinstances
(
    userid integer not null references users(userid) on delete restrict,
    unitid integer not null references units(unitid) on delete restrict,
    seqNo int not null,
    sessionid integer not null,
    score real not null default 0,
    elapsedTime int not null default 0,
    startTime big unsigned int not null default 0,
    endTime big unsigned int default 0,
    awardStarColour text,
    foreign key(userid,sessionid) references sessions(userid,sessionid),
    constraint pkey primary key (userid,unitid,seqno) on conflict fail
);

create table sessions
(
    userid integer not null references users(userid) on delete restrict,
    sessionid integer not null,
    startTime big unsigned int not null,
    endTime big unsigned int default 0,
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

create table stars
(
    userid integer not null references users(userid) on delete restrict,
    unitid integer not null references units(unitid) on delete restrict,
    colour text not null,
    constraint pkey primary key (userid,unitid) on conflict fail
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


create table analytics
(
    timestamp big unsigned int not null default 0,
    event text,
    parameters text
);