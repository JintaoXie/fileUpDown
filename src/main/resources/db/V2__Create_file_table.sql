CREATE TABLE file (
  id int NOT NULL,
  oldFileName varchar(200),
  newFileName varchar(300),
  ext varchar(20),
  path varchar(300),
  size varchar(200),
  type varchar(120),
  isImg varchar(8),
  downcounts int,
  uploadTime datetime,
  PRIMARY KEY (id)
);