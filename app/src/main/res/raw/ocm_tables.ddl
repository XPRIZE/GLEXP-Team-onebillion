create table units
(
    unitid integer primary key,
    masterlistid int not null,
    unitIndex int not null,
    level int not null default 1,
    key text not null,
    icon text,
    params text,
    config text not null,
    target text not null,
    targetDuration real,
    passThreshold real,
    typeid int not null default 0,
    lang text,
    showBack int not null default 0,
    constraint unique_const unique(masterlistid,unitIndex) on conflict fail,
    foreign key(masterlistid) references masterlists(masterlistid)
);

create table masterlists
(
    masterlistid integer primary key,
    name text not null,
    folder text not null,
    token big unsigned int not null default 0
);

create tablet extraunits
{
    userid integer not null references users(userid) on delete no action,
    level integer not null,
    unitid integer not null,
    constraint pkey primary key (userid,level,unitid) on conflict fail
};

create table unitinstances
(
    userid integer not null references users(userid) on delete no action,
    unitid integer not null,
    typeid integer not null,
    seqNo int not null,
    sessionid integer not null,
    scoreCorrect int not null default 0,
    scoreWrong int not null default 0,
    elapsedTime int not null default 0,
    startTime big unsigned int not null default 0,
    endTime big unsigned int default 0,
    starColour integer not null default -1,
    statusid integer not null default -1,
    extra text,
    assetid integer not null default -1,
    foreign key(userid,sessionid) references sessions(userid,sessionid),
    constraint pkey primary key (userid,sessionid,unitid,typeid,seqno) on conflict fail
);

create index unitid_index ON unitinstances(unitid);

create table sessions
(
    userid integer not null references users(userid) on delete no action,
    sessionid integer not null,
    startTime big unsigned int not null,
    workTime big unsigned int not null,
    endTime big unsigned int default 0,
    day int default -1,
    constraint pkey primary key (userid,sessionid) on conflict fail
);

create table users
(
    userid integer primary key,
    studylistid int not null,
    playzonelistid int not null,
    librarylistid int not null,
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
    assetid integer primary key,
    userid integer not null references users(userid) on delete no action,
    typeid integer not null default 1,
    thumbnail text,
    params text,
    createTime big unsigned int not null default 0,
    deleted int not null default 0
);


create table analytics
(
    timestamp big unsigned int not null default 0,
    event text,
    parameters text
);