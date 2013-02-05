# --- First database schema

# --- !Ups

set ignorecase true;

create table users (
  id                        bigint not null,
  email                     varchar(255) not null,
  password                  varchar(255) not null,
  first_name                varchar(255) not null,
  last_name                 varchar(255) not null,
  jersey_number             int not null
);

create table games (
	id                    	bigint not null,
	start_time              date not null,
	address                 varchar(255) not null,
	gym		                varchar(100) not null,
	opponent                varchar(100) not null,
	result                  varchar(100),
    primary key (id)  
);

create table user_games (
	id            			bigint not null,
	user_id					bigint not null,
	game_id 				bigint not null,
	foreign key (user_id) references users(id),
	foreign key (game_id) references games(id),
	primary key (id)
);

create sequence user_seq   start with 1000;
create sequence user_games_seq start with 1000;
create sequence games_seq  start with 1000;


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists users;
drop table if exists user_games;
drop table if exists games;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists user_seq;
drop sequence if exists user_games_seq;
drop sequence if exists games_seq;


