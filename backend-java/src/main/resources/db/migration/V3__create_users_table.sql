CREATE TABLE users (
   user_id BINARY(16) PRIMARY KEY,
   first_name VARCHAR(255) NOT NULL,
   second_name VARCHAR(255) NOT NULL,
   email VARCHAR(255) NOT NULL UNIQUE,
   password VARCHAR(255) NOT NULL
);