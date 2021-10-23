create table user_table(
user_id SERIAL PRIMARY KEY,
user_name varchar NOT NULL
);

create table subscriber_table(
subscriber_id SERIAL PRIMARY KEY,
user_id bigint NOT NULL,
topic_name varchar NOT NULL,
CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES user_table(user_id)
);

create table notifications(
notification_id SERIAL PRIMARY KEY,
topic_name varchar NOT NULL,
status varchar NOT NULL
);

create table topic_details(
label_id SERIAL PRIMARY KEY,
label varchar NOT NULL,
india varchar,
egypt varchar,
singapore varchar,
);