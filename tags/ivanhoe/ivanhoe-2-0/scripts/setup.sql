
-- Database Schema accurate for DB version 1.8

--
-- Current Database: ivanhoe
--

DROP DATABASE ivanhoe;
CREATE DATABASE ivanhoe;
USE ivanhoe;

CREATE TABLE action (
  id int(11) NOT NULL default 0,
  fk_type int(11) NOT NULL default 0,
  tag_id text,
  date datetime default NULL,
  offset int(8) default NULL,
  data text,
  fk_adopted_from_id int(11) default NULL,
  PRIMARY KEY  (id)
);;

CREATE TABLE action_type (
  id tinyint(4) NOT NULL default 0,
  name varchar(20) default NULL,
  PRIMARY KEY  (id)
); 

CREATE TABLE action_version (
  fk_action_id int(11) NOT NULL default 0,
  fk_document_version_id int(11) NOT NULL default 0
); 

CREATE TABLE bookmarks (
  id int(11) NOT NULL default 0,
  fk_game_id int(11) NOT NULL default 0,
  label varchar(255) default '',
  url varchar(255) default '',
  summary text NOT NULL,
  PRIMARY KEY  (id)
); 

CREATE TABLE category (
  id int(11) NOT NULL default 0,
  name varchar(255) default '',
  description text NOT NULL,
  fk_game_id int(11) NOT NULL default 0,
  PRIMARY KEY  (id)
); 

CREATE TABLE db_history (
  entry_date varchar(20) default NULL,
  entry varchar(50) default NULL,
  host_version varchar(20) default NULL
); 

CREATE TABLE discourse_field (
  fk_game_id int(11) NOT NULL default 0,
  fk_document_id int(11) NOT NULL default 0,
  starting_doc tinyint(1) default '1',
  published_doc tinyint(1) default 0,
  PRIMARY KEY  (fk_game_id,fk_document_id)
); 

CREATE TABLE discussion (
  id int(11) NOT NULL default 0,
  fk_role_id int(11) NOT NULL default 0,
  fk_game_id int(11) NOT NULL default 0,
  title varchar(50) NOT NULL default '',
  message text NOT NULL,
  post_date datetime NOT NULL default '0000-00-00 00:00:00',
  parent_id int(11) default 0,
  PRIMARY KEY  (id)
); 

CREATE TABLE document (
  id int(11) NOT NULL default 0,
  file_name varchar(255) default NULL,
  title varchar(50) default NULL,
  author varchar(255) default 'Unknown',
  publication_date varchar(20) default 'Unknown',
  provenance varchar(100) default 'Unknown',
  length int(11) default NULL,
  add_date datetime NOT NULL default '0000-00-00 00:00:00',
  fk_contributor_id int(11) default 0,
  PRIMARY KEY  (id)
); 

CREATE TABLE document_image (
  fk_document_id int(11) NOT NULL default 0,
  file_name varchar(255) default NULL
); 

CREATE TABLE document_version (
  id int(11) NOT NULL default 0,
  fk_document_id int(11) NOT NULL default 0,
  fk_role_id int(11) NOT NULL default 0,
  date datetime NOT NULL default '0000-00-00 00:00:00',
  parent_id int(11) default NULL,
  published tinyint(1) default 0,
  PRIMARY KEY  (id)
); 

CREATE TABLE game (
  id int(11) NOT NULL default 0,
  fk_creator_id int(11) NOT NULL default 0,
  name varchar(50) default NULL,
  description text,
  objectives text,
  archived tinyint(1) default 0,
  restricted tinyint(1) default 0,
  private tinyint(1) default 0,
  retired tinyint(1) default 0,
  startDocWeight int(11) default '1',
  PRIMARY KEY  (id)
); 

CREATE TABLE keyspace (
  tablename varchar(40) NOT NULL default '',
  next_value int(11) NOT NULL default 0,
  PRIMARY KEY  (tablename)
); 

CREATE TABLE link_target (
  fk_action_id int(11) NOT NULL default 0,
  fk_link_type int(11) NOT NULL default 0,
  link_id text,
  label varchar(20) default NULL,
  data text,
  fk_document_version_id int(11) NOT NULL default 0
); 

CREATE TABLE link_type (
  id tinyint(4) NOT NULL default 0,
  name varchar(20) default NULL,
  PRIMARY KEY  (id)
); 

CREATE TABLE move (
  id int(11) NOT NULL default 0,
  fk_game_id int(11) NOT NULL default 0,
  fk_role_id int(11) NOT NULL default 0,
  description text,
  start_date datetime default NULL,
  submit_date datetime default NULL,
  title text,
  fk_category_id int(11) default NULL
); 

CREATE TABLE move_action (
  fk_move_id int(11) NOT NULL default 0,
  fk_action_id int(11) NOT NULL default 0,
  PRIMARY KEY  (fk_move_id,fk_action_id)
); 

CREATE TABLE move_inspiration (
  inspired_id int(11) NOT NULL default 0,
  inspirational_id int(11) NOT NULL default 0,
  PRIMARY KEY  (inspired_id,inspirational_id)
); 

CREATE TABLE player (
  id int(11) NOT NULL default 0,
  playername varchar(20) NOT NULL default '',
  password varchar(32) NOT NULL default '',
  fname varchar(20) default NULL,
  lname varchar(20) default NULL,
  email varchar(100) default NULL,
  affiliation text,
  new_game_permission tinyint(1) default 0,
  new_role_permission tinyint(1) default 0,
  write_permission tinyint(1) default 0,
  admin tinyint(1) default 0,
  PRIMARY KEY  (id)
); 

CREATE TABLE player_game (
  fk_player_id int(11) NOT NULL default 0,
  fk_game_id int(11) NOT NULL default 0
); 

CREATE TABLE player_game_role (
  fk_player_id int(11) NOT NULL default 0,
  fk_game_id int(11) NOT NULL default 0,
  fk_role_id int(11) NOT NULL default 0,
  PRIMARY KEY  (fk_role_id)
); 

CREATE TABLE role (
  id int(11) NOT NULL default 0,
  name varchar(50) default NULL,
  description text,
  objectives text,
  stroke_rgb int(11) default 0,
  fill_rgb int(11) default 0,
  write_permission tinyint(1) default 0,
  PRIMARY KEY  (id)
); 

LOCK TABLES keyspace WRITE;
INSERT INTO keyspace VALUES ('action',1),('bookmarks',1),('category',1),('discussion',1),('document',1),('game',1),('link_target',1),('move',1),('player',1),('role',1),('document_version',1);
UNLOCK TABLES;


LOCK TABLES db_history WRITE;
INSERT INTO db_history VALUES ('2004-12-02 10:41:59','removed document.publication_date, added db_histor','1.1'),('2004-12-02 10:42:44','convert player to role id','1.1'),('2004-12-02 10:43:32','convert link tag string to field','1.1'),('2005-01-05 15:18:51','added the bookmarks table','1.2'),('2005-01-05 15:19:45','convert discussion data from player id to role id','1.2'),('2005-01-12 10:26:48','added summary to bookmarks table','1.3'),('2005-01-28 17:31:32','added move_inspiration and category tables and tit','1.4'),('2005-05-23 10:43:44','Added \'archived\' field to game table','1.5'),('2005-05-23 10:43:44','Added \'private\' field to game table','1.5'),('2005-05-23 10:43:44','Added \'write_permission\' field to role table','1.5'),('2005-05-23 10:43:44','Added \'new_game_permission\' field to player table','1.5'),('2005-05-23 10:43:44','Added \'new_role_permission\' field to player table','1.5'),('2005-05-23 10:43:44','Added \'write_permission\' field to player table','1.5'),('2005-05-23 10:43:44','Added \'admin\' field to player table','1.5'),('2005-06-28 15:47:38','convert action document version dates','1.6'),('2005-07-18 15:44:05','Add document_version entry in keyspace table','1.7'),('2005-07-25 15:13:14','drop action_document, convert link_target to use v','1.8');
UNLOCK TABLES;

LOCK TABLES action_type WRITE;
INSERT INTO action_type VALUES (1,'add'),(2,'delete'),(3,'link'),(4,'add_document'),(5,'image');
UNLOCK TABLES;

LOCK TABLES link_type WRITE;
INSERT INTO link_type VALUES (1,'internal'),(2,'url'),(3,'bibliographic'),(4,'commentary');
UNLOCK TABLES;
