CREATE TABLE USER (
    id   INTEGER      NOT NULL AUTO_INCREMENT,
    username VARCHAR(128) NOT NULL,
    password VARCHAR(128) NOT NULL,
    PRIMARY KEY (id)
);


INSERT INTO USER (id,username,password) VALUES (1,'India','$2y$10$xMtDX7Xfl1S3inKBtUsGqu0KdTCbj0wiJzVE7K7CDPd9HX/D9q0Wq');
INSERT INTO USER (id,username,password) VALUES (2,'Indi1','$2y$10$xMtDX7Xfl1S3inKBtUsGqu0KdTCbj0wiJzVE7K7CDPd9HX/D9q0Wq');
INSERT INTO USER (id,username,password) VALUES (3,'Indi2','India');
INSERT INTO USER (id,username,password) VALUES (4,'Indi3','India');
