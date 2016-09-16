create table units
(
	unitid int primary key not null,
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
    startAudio int default -1
);

create table unitinstances
(
    userid int not null references users(userid) on delete restrict,
	unitid int not null references units(unitid) on delete restrict,
	seqno int not null,
    sessionid int not null,
	score real not null default 0,
	elapsedtime int not null default 0,
    starttime big unsigned int not null default 0,
    endtime big unsigned int default 0,
    constraint pkey primary key (userid,unitid,seqno) on conflict fail,
    foreign key(userid,sessionid) references sessions(userid,sessionid)
);

create table sessions
(
    userid int not null references users(userid) on delete restrict,
    sessionid int not null,
    starttime big unsigned int not null,
    endtime big unsigned int default 0,
    currentunitid int no null default -1,
    currentseqno int no null default -1,
    constraint pkey primary key (userid,sessionid) on conflict fail
);

create table users
(
    userid integer primary key,
    name text not null
);

create table preferences
(
    name text primary key not null,
    val text
);

create table stars
(
    userid int not null references users(userid) on delete restrict,
    level int not null,
    starnum int not null,
    colour text not null,
    constraint pkey primary key (userid,level,starnum) on conflict fail
);