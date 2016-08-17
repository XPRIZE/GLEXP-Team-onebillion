create table units
(
	unitid int primary key not null,
    level int,
	key text not null,
	icon text,
	params text,
	config text not null,
	target text not null,
	targetDuration real,
	passThreshold real,
    lang text,
    catAudio int default -1
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
    currentunitid int no null default -1,
    currentseqno int no null default -1,
    constraint pkey primary key (userid,sessionid) on conflict fail
);

create table users
(
    userid integer primary key,
    name text not null
);

create table certificates
(
    userid int not null references users(userid) on delete restrict,
    level int not null,
    file text not null,
    constraint pkey primary key (userid,level) on conflict fail
);
