CREATE TABLE messages
(id VARCHAR(30) PRIMARY KEY,
 msg_type VARCHAR(200),
 exchange VARCHAR(200),
 queue VARCHAR(200),
 content VARCHAR(5000));