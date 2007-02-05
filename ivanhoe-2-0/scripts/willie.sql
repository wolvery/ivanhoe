-- MySQL dump 9.11
--
-- Host: localhost    Database: ivanhoe
-- ------------------------------------------------------
-- Server version	4.0.24_Debian-2-log

--
-- Current Database: ivanhoe
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `ivanhoe`;

USE ivanhoe;

--
-- Table structure for table `action`
--

DROP TABLE IF EXISTS `action`;
CREATE TABLE `action` (
  `id` int(11) NOT NULL default '0',
  `fk_type` int(11) NOT NULL default '0',
  `tag_id` text,
  `date` datetime default NULL,
  `offset` int(8) default NULL,
  `data` text,
  `fk_adopted_from_id` int(11) default NULL,
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `action`
--


/*!40000 ALTER TABLE `action` DISABLE KEYS */;
LOCK TABLES `action` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `action` ENABLE KEYS */;

--
-- Table structure for table `action_document`
--

DROP TABLE IF EXISTS `action_document`;
CREATE TABLE `action_document` (
  `fk_action_id` int(11) NOT NULL default '0',
  `fk_document_id` int(11) NOT NULL default '0',
  `fk_role_id` int(11) NOT NULL default '0',
  `version_date` datetime default NULL
) TYPE=MyISAM;

--
-- Dumping data for table `action_document`
--


/*!40000 ALTER TABLE `action_document` DISABLE KEYS */;
LOCK TABLES `action_document` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `action_document` ENABLE KEYS */;

--
-- Table structure for table `action_type`
--

DROP TABLE IF EXISTS `action_type`;
CREATE TABLE `action_type` (
  `id` tinyint(4) NOT NULL default '0',
  `name` varchar(20) default NULL,
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `action_type`
--


/*!40000 ALTER TABLE `action_type` DISABLE KEYS */;
LOCK TABLES `action_type` WRITE;
INSERT INTO `action_type` VALUES (1,'add'),(2,'delete'),(3,'link'),(4,'add_document'),(5,'image');
UNLOCK TABLES;
/*!40000 ALTER TABLE `action_type` ENABLE KEYS */;

--
-- Table structure for table `bookmarks`
--

DROP TABLE IF EXISTS `bookmarks`;
CREATE TABLE `bookmarks` (
  `id` int(11) NOT NULL default '0',
  `fk_game_id` int(11) NOT NULL default '0',
  `label` varchar(255) default '',
  `url` varchar(255) default '',
  `summary` text NOT NULL,
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `bookmarks`
--


/*!40000 ALTER TABLE `bookmarks` DISABLE KEYS */;
LOCK TABLES `bookmarks` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `bookmarks` ENABLE KEYS */;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` int(11) NOT NULL default '0',
  `name` varchar(255) default '',
  `description` text NOT NULL,
  `fk_game_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `category`
--


/*!40000 ALTER TABLE `category` DISABLE KEYS */;
LOCK TABLES `category` WRITE;
INSERT INTO `category` VALUES (1,'Inward','Connections within the album material',9),(2,'Outward','Connections between the album and everything else',9),(3,'Pedagogy','',9),(4,'Willie Nelson Studies','Authorship questions',9);
UNLOCK TABLES;
/*!40000 ALTER TABLE `category` ENABLE KEYS */;

--
-- Table structure for table `db_history`
--

DROP TABLE IF EXISTS `db_history`;
CREATE TABLE `db_history` (
  `entry_date` varchar(20) default NULL,
  `entry` varchar(50) default NULL,
  `host_version` varchar(20) default NULL
) TYPE=MyISAM;

--
-- Dumping data for table `db_history`
--


/*!40000 ALTER TABLE `db_history` DISABLE KEYS */;
LOCK TABLES `db_history` WRITE;
INSERT INTO `db_history` VALUES ('2004-12-02 10:41:59','removed document.publication_date, added db_histor','1.1'),('2004-12-02 10:42:44','convert player to role id','1.1'),('2004-12-02 10:43:32','convert link tag string to field','1.1'),('2005-01-05 15:18:51','added the bookmarks table','1.2'),('2005-01-05 15:19:45','convert discussion data from player id to role id','1.2'),('2005-01-12 10:26:48','added summary to bookmarks table','1.3'),('2005-01-28 17:31:32','added move_inspiration and category tables and tit','1.4');
UNLOCK TABLES;
/*!40000 ALTER TABLE `db_history` ENABLE KEYS */;

--
-- Table structure for table `discourse_field`
--

DROP TABLE IF EXISTS `discourse_field`;
CREATE TABLE `discourse_field` (
  `fk_game_id` int(11) NOT NULL default '0',
  `fk_document_id` int(11) NOT NULL default '0',
  `starting_doc` tinyint(1) default '1',
  `published_doc` tinyint(1) default '0',
  PRIMARY KEY  (`fk_game_id`,`fk_document_id`)
) TYPE=MyISAM;

--
-- Dumping data for table `discourse_field`
--


/*!40000 ALTER TABLE `discourse_field` DISABLE KEYS */;
LOCK TABLES `discourse_field` WRITE;
INSERT INTO `discourse_field` VALUES (9,29,1,1),(9,30,1,1),(9,31,1,1),(9,32,1,1),(9,33,1,1),(9,34,1,1),(9,35,1,1),(9,36,1,1),(9,37,1,1),(9,38,1,1),(9,39,1,1),(9,40,1,1),(9,41,1,1),(9,42,1,1),(9,43,1,1),(9,44,1,1),(9,45,1,1),(9,46,1,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `discourse_field` ENABLE KEYS */;

--
-- Table structure for table `discussion`
--

DROP TABLE IF EXISTS `discussion`;
CREATE TABLE `discussion` (
  `id` int(11) NOT NULL default '0',
  `fk_role_id` int(11) NOT NULL default '0',
  `fk_game_id` int(11) NOT NULL default '0',
  `title` varchar(50) NOT NULL default '',
  `message` text NOT NULL,
  `post_date` datetime NOT NULL default '0000-00-00 00:00:00',
  `parent_id` int(11) default '0',
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `discussion`
--


/*!40000 ALTER TABLE `discussion` DISABLE KEYS */;
LOCK TABLES `discussion` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `discussion` ENABLE KEYS */;

--
-- Table structure for table `document`
--

DROP TABLE IF EXISTS `document`;
CREATE TABLE `document` (
  `id` int(11) NOT NULL default '0',
  `file_name` varchar(255) default NULL,
  `title` varchar(50) default NULL,
  `author` varchar(255) default 'Unknown',
  `publication_date` varchar(20) default 'Unknown',
  `provenance` varchar(100) default 'Unknown',
  `length` int(11) default NULL,
  `add_date` datetime NOT NULL default '0000-00-00 00:00:00',
  `fk_contributor_id` int(11) default '0',
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `document`
--


/*!40000 ALTER TABLE `document` DISABLE KEYS */;
LOCK TABLES `document` WRITE;
INSERT INTO `document` VALUES (29,'rhs1.html','Time of the Preacher','Willie Nelson','*deprecate*','Unknown',456,'2005-02-21 11:15:27',-1),(30,'rhs2.html','I Couldn\'t Believe It Was True','Willie Nelson','*deprecate*','Unknown',777,'2005-02-21 11:16:44',-1),(31,'rhs3.html','Time of the Preacher Theme','Willie Nelson','*deprecate*','Unknown',964,'2005-02-21 11:17:14',-1),(32,'rhs4.html','Blue Rock Montana/Red Head Stranger','Willie Nelson','*deprecate*','Unknown',631,'2005-02-21 12:04:50',-1),(33,'rhs5.html','Blue Eyes Crying in the Rain','Willie Nelson','*deprecate*','Unknown',509,'2005-02-21 12:05:35',-1),(34,'rhs6.html','Red Headed Stranger','Willie Nelson','*deprecate*','Unknown',2050,'2005-02-21 12:06:10',-1),(35,'rhs7.html','Just as I Am [instrumental]','Willie Nelson','*deprecate*','Unknown',30,'2005-02-21 12:06:29',-1),(36,'rhs8.html','Denver','Willie Nelson','*deprecate*','Unknown',456,'2005-02-21 12:07:01',-1),(37,'rhs9.html','O\'er the Waves [instrumental]','Willie Nelson','*deprecate*','Unknown',32,'2005-02-21 12:07:44',-1),(38,'rhs10.html','Down Yonder [instrumental]','Willie Nelson','*deprecate*','Unknown',29,'2005-02-21 12:07:58',-1),(39,'rhs11.html','Can I Sleep in Your Arms?','Willie Nelson','*deprecate*','Unknown',696,'2005-02-21 12:08:47',-1),(40,'rhs12.html','Remember Me (When The Candle Lights Are Gleaming)','Willie Nelson','*deprecate*','Unknown',868,'2005-02-21 12:09:13',-1),(41,'rhs13.html','Hands on the Wheel','Willie Nelson','*deprecate*','Unknown',1046,'2005-02-21 12:09:38',-1),(42,'rhs14.html','Bandera [instrumental]','Willie Nelson','*deprecate*','Unknown',25,'2005-02-21 12:09:53',-1),(43,'rhs15.html','Bach Minuet in G [instrumental]','Willie Nelson','*deprecate*','Unknown',34,'2005-02-21 12:10:21',-1),(44,'rhs16.html','I Can\'t Help It (If I\'m Still in Love With You)','Willie Nelson','*deprecate*','Unknown',720,'2005-02-21 12:10:43',-1),(45,'rhs17.html','Maiden\'s Prayer','Willie Nelson','*deprecate*','Unknown',449,'2005-02-21 12:11:09',-1),(46,'rhs18.html','Bonaparte\'s Retreat','Willie Nelson','*deprecate*','Unknown',775,'2005-02-21 12:11:32',-1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `document` ENABLE KEYS */;

--
-- Table structure for table `document_image`
--

DROP TABLE IF EXISTS `document_image`;
CREATE TABLE `document_image` (
  `fk_document_id` int(11) NOT NULL default '0',
  `file_name` varchar(255) default NULL
) TYPE=MyISAM;

--
-- Dumping data for table `document_image`
--


/*!40000 ALTER TABLE `document_image` DISABLE KEYS */;
LOCK TABLES `document_image` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `document_image` ENABLE KEYS */;

--
-- Table structure for table `game`
--

DROP TABLE IF EXISTS `game`;
CREATE TABLE `game` (
  `id` int(11) NOT NULL default '0',
  `fk_creator_id` int(11) NOT NULL default '0',
  `name` varchar(50) default NULL,
  `description` text,
  `objectives` text,
  `restricted` tinyint(1) default '0',
  `retired` tinyint(1) default '0',
  `startDocWeight` int(11) default '1',
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `game`
--


/*!40000 ALTER TABLE `game` DISABLE KEYS */;
LOCK TABLES `game` WRITE;
INSERT INTO `game` VALUES (9,20,'Red Headed Stranger','A game where players experiment with some songs attributed to Willie Nelson on the album \"Red Headed Stranger\"','Enjoy yourself, make fun changes.',1,0,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `game` ENABLE KEYS */;

--
-- Table structure for table `keyspace`
--

DROP TABLE IF EXISTS `keyspace`;
CREATE TABLE `keyspace` (
  `tablename` varchar(40) NOT NULL default '',
  `next_value` int(11) NOT NULL default '0',
  PRIMARY KEY  (`tablename`)
) TYPE=MyISAM;

--
-- Dumping data for table `keyspace`
--


/*!40000 ALTER TABLE `keyspace` DISABLE KEYS */;
LOCK TABLES `keyspace` WRITE;
INSERT INTO `keyspace` VALUES ('action',1),('bookmarks',1),('category',5),('discussion',1),('document',50),('game',10),('link_target',1),('move',1),('player',500),('role',1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `keyspace` ENABLE KEYS */;

--
-- Table structure for table `link_target`
--

DROP TABLE IF EXISTS `link_target`;
CREATE TABLE `link_target` (
  `fk_action_id` int(11) NOT NULL default '0',
  `fk_link_type` int(11) NOT NULL default '0',
  `fk_role_id` int(11) NOT NULL default '0',
  `fk_document_id` int(11) NOT NULL default '0',
  `current_move` tinyint(1) default '0',
  `document_version_date` datetime default NULL,
  `link_id` text,
  `label` varchar(20) default NULL,
  `data` text
) TYPE=MyISAM;

--
-- Dumping data for table `link_target`
--


/*!40000 ALTER TABLE `link_target` DISABLE KEYS */;
LOCK TABLES `link_target` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `link_target` ENABLE KEYS */;

--
-- Table structure for table `link_type`
--

DROP TABLE IF EXISTS `link_type`;
CREATE TABLE `link_type` (
  `id` tinyint(4) NOT NULL default '0',
  `name` varchar(20) default NULL,
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `link_type`
--


/*!40000 ALTER TABLE `link_type` DISABLE KEYS */;
LOCK TABLES `link_type` WRITE;
INSERT INTO `link_type` VALUES (1,'internal'),(2,'url'),(3,'bibliographic'),(4,'commentary');
UNLOCK TABLES;
/*!40000 ALTER TABLE `link_type` ENABLE KEYS */;

--
-- Table structure for table `move`
--

DROP TABLE IF EXISTS `move`;
CREATE TABLE `move` (
  `id` int(11) NOT NULL default '0',
  `fk_game_id` int(11) NOT NULL default '0',
  `fk_role_id` int(11) NOT NULL default '0',
  `description` text,
  `start_date` datetime default NULL,
  `submit_date` datetime default NULL,
  `title` text,
  `fk_category_id` int(11) default NULL
) TYPE=MyISAM;

--
-- Dumping data for table `move`
--


/*!40000 ALTER TABLE `move` DISABLE KEYS */;
LOCK TABLES `move` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `move` ENABLE KEYS */;

--
-- Table structure for table `move_action`
--

DROP TABLE IF EXISTS `move_action`;
CREATE TABLE `move_action` (
  `fk_move_id` int(11) NOT NULL default '0',
  `fk_action_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`fk_move_id`,`fk_action_id`)
) TYPE=MyISAM;

--
-- Dumping data for table `move_action`
--


/*!40000 ALTER TABLE `move_action` DISABLE KEYS */;
LOCK TABLES `move_action` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `move_action` ENABLE KEYS */;

--
-- Table structure for table `move_inspiration`
--

DROP TABLE IF EXISTS `move_inspiration`;
CREATE TABLE `move_inspiration` (
  `inspired_id` int(11) NOT NULL default '0',
  `inspirational_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`inspired_id`,`inspirational_id`)
) TYPE=MyISAM;

--
-- Dumping data for table `move_inspiration`
--


/*!40000 ALTER TABLE `move_inspiration` DISABLE KEYS */;
LOCK TABLES `move_inspiration` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `move_inspiration` ENABLE KEYS */;

--
-- Table structure for table `player`
--

DROP TABLE IF EXISTS `player`;
CREATE TABLE `player` (
  `id` int(11) NOT NULL default '0',
  `playername` varchar(20) NOT NULL default '',
  `password` varchar(32) NOT NULL default '',
  `fname` varchar(20) default NULL,
  `lname` varchar(20) default NULL,
  `email` varchar(100) default NULL,
  `affiliation` text,
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `player`
--


/*!40000 ALTER TABLE `player` DISABLE KEYS */;
LOCK TABLES `player` WRITE;
INSERT INTO `player` VALUES (17,'nick','e2e42a07550863f8b67f5eb252581f6d','Nick','Laiacona','ncl2n@virginia.edu','Developer'),(18,'ben','8432ad4ad0ee9031b093df6460d87693','Ben','Cummings','bjc8r@virginia.edu',''),(20,'Duane','357ddb585594fe6400d3114fc94239c2','Duane','Gran','dmg2n@virginia.edu','ARP'),(30,'Nathan','ca9c54e9a4d913f3777f5427a301ab83','Nathan','Piazza','nfp5e@virginia.edu',''),(31,'Laura','b077f51ff36868f21ea52956adcf7ff4','Laura','Mandell','mandellc@muohio.edu','Miami Univ. of Ohio'),(32,'Girl Poet','c1a34aae7d4bd9eee17160f1f034b756','Johanna','Drucker','jrd8e@virginia.edu',''),(33,'jerome','2bb010060d682fee5ad19d973a9a4d2a','jerome','mcgann','jjm2f@virginia.edu',''),(38,'Beth','f0d78724487b188c0df666f874af6b27','Bethany','Nowviskie','bethany@virginia.edu','ARP'),(39,'Tucker','91a026ab57eac2af3f191985dad8bd7a','Chip','Tucker','tucker@virginia.edu','UVA'),(40,'Rob Pope','d5a57c8f65bc0da0af40bf2e52467ec5','Rob','Pope','rfpope@brookes.ac.uk','Oxford Brookes University'),(41,'Jerome McGann','5f4dcc3b5aa765d61d8327deb882cf99','Jerome','McGann','jmcgann@virginia.edu','ARP @ UVA'),(100,'Jessica Feldman','5f4dcc3b5aa765d61d8327deb882cf99','Jessica','Feldman','jrf2j@virginia.edu','UVA'),(111,'Charles Sligh','81dc9bdb52d04dc20036dbd8313ed055','Charlie','Sligh','cls9k@Virginia.edu',''),(112,'Melissa White','81dc9bdb52d04dc20036dbd8313ed055','Melissa','White','mw2w@virginia.edu',''),(113,'Keicy Tolbert','81dc9bdb52d04dc20036dbd8313ed055','Keicy','Tolbert','knt6r@virginia.edu','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `player` ENABLE KEYS */;

--
-- Table structure for table `player_game`
--

DROP TABLE IF EXISTS `player_game`;
CREATE TABLE `player_game` (
  `fk_player_id` int(11) NOT NULL default '0',
  `fk_game_id` int(11) NOT NULL default '0'
) TYPE=MyISAM;

--
-- Dumping data for table `player_game`
--


/*!40000 ALTER TABLE `player_game` DISABLE KEYS */;
LOCK TABLES `player_game` WRITE;
INSERT INTO `player_game` VALUES (18,9),(38,9),(111,9),(20,9),(41,9),(113,9),(112,9),(17,9);
UNLOCK TABLES;
/*!40000 ALTER TABLE `player_game` ENABLE KEYS */;

--
-- Table structure for table `player_game_role`
--

DROP TABLE IF EXISTS `player_game_role`;
CREATE TABLE `player_game_role` (
  `fk_player_id` int(11) NOT NULL default '0',
  `fk_game_id` int(11) NOT NULL default '0',
  `fk_role_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`fk_role_id`)
) TYPE=MyISAM;

--
-- Dumping data for table `player_game_role`
--


/*!40000 ALTER TABLE `player_game_role` DISABLE KEYS */;
LOCK TABLES `player_game_role` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `player_game_role` ENABLE KEYS */;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` int(11) NOT NULL default '0',
  `name` varchar(50) default NULL,
  `description` text,
  `objectives` text,
  `stroke_rgb` int(11) default '0',
  `fill_rgb` int(11) default '0',
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `role`
--


/*!40000 ALTER TABLE `role` DISABLE KEYS */;
LOCK TABLES `role` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `role` ENABLE KEYS */;

