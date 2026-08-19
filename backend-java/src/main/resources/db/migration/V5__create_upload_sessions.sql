CREATE TABLE upload_sessions (
     session_id BINARY(16) NOT NULL,
     user_id BINARY(16) NOT NULL,

     session_name VARCHAR(255) NOT NULL,

     file_size_bytes BIGINT NOT NULL,
     chunk_size_bytes BIGINT NOT NULL,

     total_chunks INT NOT NULL,
     uploaded_chunks INT NOT NULL,

     status VARCHAR(30) NOT NULL,
     completed BOOLEAN NOT NULL,

     created_at TIMESTAMP NOT NULL,
     updated_at TIMESTAMP NOT NULL,

     PRIMARY KEY (session_id),

     CONSTRAINT fk_upload_sessions_user
         FOREIGN KEY (user_id)
             REFERENCES users(user_id)
             ON DELETE CASCADE
             ON UPDATE CASCADE
);

CREATE INDEX idx_upload_sessions_user_id
    ON upload_sessions(user_id);

CREATE INDEX idx_upload_sessions_status
    ON upload_sessions(status);