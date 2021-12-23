create table bgguser (
	id bigint not null generated always as identity (start with 1 increment by 1),
	username varchar(255) not null,
	last_update date not null);
	
alter table bgguser add constraint pk_bgguser primary key (id);

create table play (
	id bigint not null generated always as identity (start with 1 increment by 1),
	bgg_id bigint not null,
	play_date date not null);

alter table play add constraint pk_play primary key (id);

	
create table player (
	id bigint not null generated always as identity (start with 1 increment by 1),
	character varchar(50) not null,
	name varchar(255) not null,
	username varchar(255) null,
	win boolean not null,
	play_id bigint not null);

alter table player add constraint pk_player primary key (id);
alter table player add constraint fk_play foreign key (play_id) references play(id);