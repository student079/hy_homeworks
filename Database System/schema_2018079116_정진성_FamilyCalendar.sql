CREATE TABLE event (
    event_id serial PRIMARY KEY,
    event_name VARCHAR(50) NOT NULL,
    event_stime Timestamp NOT NULL,
    event_etime Timestamp NOT NULL,
    event_detail VARCHAR(100),
    event_creator_id int NOT NULL);

CREATE TABLE "user" (
    user_id serial PRIMARY KEY,
    id VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    channel INT NOT NULL);

CREATE TABLE eventmember (
    eventmember_id serial PRIMARY KEY,
    user_id int NOT NULL,
    event_id int references event(event_id) on delete cascade,
    foreign key (user_id) references "user"(user_id) on delete cascade);

CREATE TABLE reminder (
    reminder_id serial PRIMARY KEY,
    event_id int references event(event_id) on delete cascade,
    reminder_start_time TIMESTAMP NOT NULL,
    interval int NOT NULL);

CREATE TABLE rsvp (
    rsvp_id serial PRIMARY KEY,
    event_id int references event(event_id) on delete cascade,
    host_user_id int references "user"(user_id) on delete cascade,
    guest_user_id int references "user"(user_id) on delete cascade,
    request_time TIMESTAMP NOT NULL,
    rsvp_status int NOT NULL);