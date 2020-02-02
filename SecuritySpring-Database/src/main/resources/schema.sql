CREATE TABLE USER (
    id   INTEGER      NOT NULL AUTO_INCREMENT,
    username VARCHAR(128) NOT NULL,
    password VARCHAR(128) NOT NULL,
    PRIMARY KEY (id)
);


INSERT INTO USER (id,username,password) VALUES (1,'India','$2y$12$RxIUt2ZJZpkeNbbrN5g5UukeLYEcqbFOIIkQkOKN8kaiL11DuTSSa');
INSERT INTO USER (id,username,password) VALUES (2,'Indi1','India');
INSERT INTO USER (id,username,password) VALUES (3,'Indi2','India');
INSERT INTO USER (id,username,password) VALUES (4,'Indi3','India');
