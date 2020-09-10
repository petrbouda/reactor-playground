CREATE TABLE person (
    correlationId int,
    personId int,
    lastname varchar(255),
    firstname varchar(255),
    city varchar(255),
    country varchar(255)
);

CREATE TABLE article (
    correlationId int,
    articleId int,
    content varchar(255)
);

INSERT INTO person (correlationId, personId, lastname, firstname, city, country) VALUES (1, 1, 'Erichsen', 'Tom', 'Oslo', 'Norway');
INSERT INTO person (correlationId, personId, lastname, firstname, city, country) VALUES (1, 2, 'Black', 'Bruce', 'New York', 'USA');
INSERT INTO person (correlationId, personId, lastname, firstname, city, country) VALUES (2, 3, 'Smith', 'Lukas', 'Boston', 'USA');

INSERT INTO article (correlationId, articleId, content) VALUES (1, 1, 'This is my nature');
INSERT INTO article (correlationId, articleId, content) VALUES (2, 2, 'This is my Science');

