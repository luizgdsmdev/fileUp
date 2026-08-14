CREATE TABLE users_roles (
     user_id BINARY(16) NOT NULL,
     role_id BIGINT NOT NULL,

     PRIMARY KEY(user_id, role_id),

     CONSTRAINT fk_users_roles_users
         FOREIGN KEY(user_id)
             REFERENCES users(user_id),

     CONSTRAINT fk_users_roles_roles
         FOREIGN KEY(role_id)
             REFERENCES role(role_id)
);