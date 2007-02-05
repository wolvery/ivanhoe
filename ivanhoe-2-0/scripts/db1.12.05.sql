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
INSERT INTO `action` VALUES (1,4,'b3ef3bc3d121547a:158497c:1014ebcecc6:-8000','2005-01-07 14:52:22',0,'Dream Land',NULL),(2,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fff','2005-01-07 14:53:45',108,'\n',NULL),(3,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ffe','2005-01-07 14:53:47',199,'\n',NULL),(4,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ffd','2005-01-07 14:53:48',300,'\n',NULL),(5,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ffc','2005-01-07 14:53:49',416,'\n',NULL),(6,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ffb','2005-01-07 14:53:53',518,'\n',NULL),(7,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ffa','2005-01-07 14:53:54',612,'\n',NULL),(8,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ff9','2005-01-07 14:53:58',718,' --\n',NULL),(9,2,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ff8','2005-01-07 14:54:05',712,'&mdash',NULL),(10,2,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fef','2005-01-07 14:54:13',329,'&rsquo',NULL),(11,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fe9','2005-01-07 14:54:18',328,'\'',NULL),(12,2,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fe8','2005-01-07 14:54:21',307,'&rsquo',NULL),(13,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fe2','2005-01-07 14:54:23',306,'\'',NULL),(14,2,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fe1','2005-01-07 14:54:29',12,'\n',NULL),(15,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fe0','2005-01-07 14:54:29',11,'\n',NULL),(16,3,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fdf','2005-01-07 14:56:49',1,'Dream Land',NULL),(17,3,'b3ef3bc3d121547a:16c14e7:1014edc9e6c:-7ffe','2005-01-07 15:31:05',66,'She sleeps a charmed sleep;',NULL),(18,4,'263b8754279ca0a4:bb628c:1014ef295f4:-7ffa','2005-01-07 16:13:59',0,'',NULL),(19,4,'27101fef35e777f9:a1c1f5:1015329b845:-7ff4','2005-01-08 11:33:06',0,'Nuptial Sleep',NULL),(20,3,'27101fef35e777f9:a1c1f5:1015329b845:-7ff2','2005-01-08 11:33:52',1,'Nuptial Sleep',NULL),(21,3,'27101fef35e777f9:a1c1f5:1015329b845:-8000','2005-01-08 11:30:38',12,'As dead, they lay, the slumbering souls\nMere girls, hair tossed among the sheets\nAnd as they breathed released from toils\nThey matched their measured breath and beats.\nThe darkest vision, still ahead, bewitched them into dreams\nThat kept them safe from prying eyes in that private bower.',NULL),(22,3,'27101fef35e777f9:a1c1f5:1015329b845:-7ff3','2005-01-08 11:33:52',12,'As dead, they lay, the slumbering souls\nMere girls, hair tossed among the sheets\nAnd as they breathed released from toils\nThey matched their measured breath and beats.\nThe darkest vision, still ahead, bewitched them into dreams\nThat kept them safe from prying eyes in that private bower.',NULL),(23,4,'27101fef35e777f9:a1c1f5:1015329b845:-7fcb','2005-01-08 11:42:46',0,'The Garden of Proserpine (excerpt)',NULL),(24,3,'27101fef35e777f9:a1c1f5:1015329b845:-7fca','2005-01-08 11:46:35',13,'WHERE',NULL),(25,3,'27101fef35e777f9:a1c1f5:1015329b845:-7fc8','2005-01-08 11:48:12',32,'Here',NULL),(26,3,'27101fef35e777f9:a1c1f5:1015329b845:-7fbb','2005-01-08 11:49:24',32,'Here',NULL),(27,3,'27101fef35e777f9:a1c1f5:1015329b845:-7fba','2005-01-08 11:49:24',1,'UnDreaming',NULL),(28,3,'27101fef35e777f9:a1c1f5:1015329b845:-7fb8','2005-01-08 11:51:48',32,'Here',NULL),(29,3,'27101fef35e777f9:a1c1f5:1015329b845:-7fb7','2005-01-08 11:55:03',158,'I watch the green field growing\nFor reaping folk and sowing,\nFor harvest-time and mowing,\nA sleepy world of streams.',NULL),(30,4,'27101fef35e777f9:73d10f:1015349a42d:-7ffa','2005-01-08 12:10:00',0,'from \"Ave atque Vale\"',NULL),(31,3,'27101fef35e777f9:73d10f:1015349a42d:-7ff9','2005-01-08 12:11:08',723,'Sleep that no pain shall wake,\nNight that no morn shall break,\nTill joy shall overtake\nHer perfect peace.',NULL),(32,3,'27101fef35e777f9:73d10f:1015349a42d:-7ff1','2005-01-08 12:13:30',723,'Sleep that no pain shall wake,\nNight that no morn shall break,\nTill joy shall overtake\nHer perfect peace.',NULL),(33,3,'27101fef35e777f9:73d10f:1015349a42d:-7ff0','2005-01-08 12:22:01',1,'Dream Land',NULL),(34,2,'27101fef35e777f9:73d10f:1015349a42d:-7fee','2005-01-08 12:22:06',329,'&rsquo',NULL),(35,1,'27101fef35e777f9:73d10f:1015349a42d:-7fe8','2005-01-08 12:22:08',328,'\'',NULL),(36,2,'27101fef35e777f9:73d10f:1015349a42d:-7fe7','2005-01-08 12:22:11',307,'&rsquo',NULL),(37,1,'27101fef35e777f9:73d10f:1015349a42d:-7fe1','2005-01-08 12:22:12',306,'\'',NULL),(38,2,'27101fef35e777f9:73d10f:1015349a42d:-7fe0','2005-01-08 12:22:16',715,'&mdash',NULL),(39,1,'27101fef35e777f9:73d10f:1015349a42d:-7fda','2005-01-08 12:22:18',712,'-- ',NULL),(40,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd7','2005-01-08 12:22:26',11,'\n',NULL),(41,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd6','2005-01-08 12:22:28',108,'\n',NULL),(42,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd5','2005-01-08 12:22:30',199,'\n',NULL),(43,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd4','2005-01-08 12:22:33',300,'\n',NULL),(44,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd3','2005-01-08 12:22:36',416,'\n',NULL),(45,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd2','2005-01-08 12:22:38',518,'\n',NULL),(46,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd1','2005-01-08 12:22:43',612,'\n',NULL),(47,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd0','2005-01-08 12:22:46',721,'\n',NULL),(48,4,'27101fef35e777f9:501268:10157da8a73:-7ff1','2005-01-09 09:33:15',0,'Insomnia',NULL),(49,3,'27101fef35e777f9:501268:10157da8a73:-7ff0','2005-01-09 09:35:14',1,'Insomnia',NULL),(50,3,'27101fef35e777f9:501268:10157da8a73:-7ff6','2005-01-09 09:29:00',56,'unsleeping',NULL),(51,3,'27101fef35e777f9:501268:10157da8a73:-7ff5','2005-01-09 09:29:00',1,'UnDreaming',NULL),(52,3,'f0ed2a6d7fb07688:b9459c:1015950d40c:-7ffa','2005-01-09 16:26:21',55,'sweet smart',NULL),(53,3,'f0ed2a6d7fb07688:b9459c:1015950d40c:-7ff4','2005-01-09 16:39:02',106,'shed\nFrom sparkling eaves',NULL),(54,3,'27101fef35e777f9:7800e7:1015a24a50d:-7fee','2005-01-09 20:36:19',1,'Dream Land',NULL),(55,4,'27101fef35e777f9:6a21b2:1015a5a69fb:-8000','2005-01-09 21:01:09',0,'The Kraken',NULL),(56,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fff','2005-01-09 21:06:49',1,'The Kraken',NULL),(57,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7ffc','2005-01-09 21:19:49',219,'Huge sponges of millennial growth and height;\nAnd far away into the sickly light,\nFrom many a wondrous grot and secret cell\nUnnumber\'d and enormous polypi\nWinnow with giant arms the slumbering green.\nThere hath he lain for ages and will lie\nBattening upon huge sea-worms in his sleep,',NULL),(58,4,'27101fef35e777f9:6a21b2:1015a5a69fb:-7ffd','2005-01-09 21:19:09',0,'from \"The Octopus\" (1872)',NULL),(59,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7ffb','2005-01-09 21:19:49',1,'from THE OCTOPUS',NULL),(60,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7feb','2005-01-09 21:23:25',1,'from THE OCTOPUS',NULL),(61,2,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fea','2005-01-09 21:23:58',137,'Kraken',NULL),(62,1,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fe4','2005-01-09 21:24:00',131,'Critic',NULL),(63,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fdc','2005-01-09 21:29:43',130,' CriticKraken sleepeth',NULL),(64,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fce','2005-01-09 21:30:28',554,'Indelible ink!',NULL),(65,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fcd','2005-01-09 21:30:28',1,'The Kraken',NULL),(66,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fb7','2005-01-09 21:34:00',15,'AT length their long kiss severed, with sweet smart:\nAnd as the last slow sudden drops are shed\nFrom sparkling eaves when all the storm has fled,',NULL),(67,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fb6','2005-01-09 21:34:00',129,'e CriticKraken sleepeth: fain',NULL),(68,4,'b3ef3bc3d121547a:1ee69d3:1015d2d29ff:-8000','2005-01-10 10:10:35',0,'',NULL),(69,1,'464545650b803684:191394e:1015f31e282:-8000','2005-01-10 19:34:59',46,'sadfsafdsadfasfdsfad\nasdflkjasdflkjasdflkj\nadsflkjasdfkljjasdflkjasdflkj',NULL);
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
INSERT INTO `action_document` VALUES (1,7,2,'2005-01-07 14:52:22'),(2,7,2,'2005-01-07 14:52:22'),(3,7,2,'2005-01-07 14:52:22'),(4,7,2,'2005-01-07 14:52:22'),(5,7,2,'2005-01-07 14:52:22'),(6,7,2,'2005-01-07 14:52:22'),(7,7,2,'2005-01-07 14:52:22'),(8,7,2,'2005-01-07 14:52:22'),(9,7,2,'2005-01-07 14:52:22'),(10,7,2,'2005-01-07 14:52:22'),(11,7,2,'2005-01-07 14:52:22'),(12,7,2,'2005-01-07 14:52:22'),(13,7,2,'2005-01-07 14:52:22'),(14,7,2,'2005-01-07 14:52:22'),(15,7,2,'2005-01-07 14:52:22'),(16,7,2,'2005-01-07 14:52:22'),(17,7,2,'2005-01-07 15:00:46'),(18,8,6,'2005-01-07 16:13:59'),(19,9,2,'2005-01-08 11:33:06'),(20,9,2,'2005-01-08 11:33:06'),(21,8,2,'2005-01-07 16:13:59'),(22,8,2,'2005-01-07 16:13:59'),(23,10,2,'2005-01-08 11:42:46'),(24,7,2,'2005-01-07 15:00:46'),(25,10,2,'2005-01-08 11:42:46'),(26,10,2,'2005-01-08 11:42:46'),(27,8,2,'2005-01-08 11:37:40'),(28,10,2,'2005-01-08 11:42:46'),(29,10,2,'2005-01-08 11:42:46'),(30,11,8,'2005-01-08 12:10:00'),(31,7,2,'2005-01-08 11:57:07'),(32,7,2,'2005-01-08 11:57:07'),(33,7,8,'2005-01-08 12:15:25'),(34,7,8,'2005-01-08 12:15:25'),(35,7,8,'2005-01-08 12:15:25'),(36,7,8,'2005-01-08 12:15:25'),(37,7,8,'2005-01-08 12:15:25'),(38,7,8,'2005-01-08 12:15:25'),(39,7,8,'2005-01-08 12:15:25'),(40,7,8,'2005-01-08 12:15:25'),(41,7,8,'2005-01-08 12:15:25'),(42,7,8,'2005-01-08 12:15:25'),(43,7,8,'2005-01-08 12:15:25'),(44,7,8,'2005-01-08 12:15:25'),(45,7,8,'2005-01-08 12:15:25'),(46,7,8,'2005-01-08 12:15:25'),(47,7,8,'2005-01-08 12:15:25'),(48,12,8,'2005-01-09 09:33:15'),(49,12,8,'2005-01-09 09:33:15'),(50,11,8,'2005-01-08 12:15:25'),(51,8,8,'2005-01-07 16:13:59'),(52,9,12,'2005-01-08 11:33:06'),(53,9,12,'2005-01-09 16:28:06'),(54,7,2,'2005-01-08 11:57:07'),(55,13,2,'2005-01-09 21:01:09'),(56,13,2,'2005-01-09 21:01:09'),(57,13,8,'2005-01-09 21:01:09'),(58,14,8,'2005-01-09 21:19:09'),(59,14,8,'2005-01-09 21:19:09'),(60,14,8,'2005-01-09 21:19:09'),(61,13,8,'2005-01-09 21:01:09'),(62,13,8,'2005-01-09 21:01:09'),(63,13,8,'2005-01-09 21:01:09'),(64,14,8,'2005-01-09 21:19:09'),(65,13,8,'2005-01-09 21:01:09'),(66,9,12,'2005-01-09 16:43:35'),(67,13,8,'2005-01-09 21:31:59'),(68,15,15,'2005-01-10 10:10:35'),(69,10,18,'2005-01-08 11:42:46');
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
INSERT INTO `db_history` VALUES ('2004-12-02 10:41:59','removed document.publication_date, added db_histor','1.1'),('2004-12-02 10:42:44','convert player to role id','1.1'),('2004-12-02 10:43:32','convert link tag string to field','1.1'),('2005-01-05 15:18:51','added the bookmarks table','1.2'),('2005-01-05 15:19:45','convert discussion data from player id to role id','1.2'),('2005-01-12 10:26:48','added summary to bookmarks table','1.3'),('2005-01-14 16:05:53','added move_inspiration and category tables and tit','1.4'),('2005-04-11 11:53:32','Added \'archived\' field to game table','1.5'),('2005-04-11 11:53:32','Added \'private\' field to game table','1.5'),('2005-04-11 11:53:32','Added \'write_permission\' field to role table','1.5'),('2005-04-11 11:53:32','Added \'new_game_permission\' field to player table','1.5'),('2005-04-11 11:53:32','Added \'new_role_permission\' field to player table','1.5'),('2005-04-11 11:53:32','Added \'write_permission\' field to player table','1.5'),('2005-04-11 11:53:32','Added \'admin\' field to player table','1.5');
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
INSERT INTO `discourse_field` VALUES (6,7,0,1),(6,8,0,1),(6,9,0,1),(6,10,0,1),(6,11,0,1),(6,12,0,1),(6,13,0,1),(6,14,0,1),(5,15,0,1);
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
INSERT INTO `document` VALUES (7,'Dream Land.html','Dream Land','Christina Rossetti','*deprecate*','The Germ (1850)',815,'2005-01-07 14:52:22',2),(8,'SleepersAwake.html','Sleepers Awake','Goblina','*deprecate*','her soul, fed by the wellspring of unseen forces',879,'2005-01-07 16:13:59',6),(9,'NuptialSleep.html','Nuptial Sleep','D. G. Rossetti','*deprecate*','Poems (1870)',681,'2005-01-08 11:33:06',2),(10,'TheGardenofProserpineexcerpt.html','The Garden of Proserpine (excerpt)','A. C. Swinburne','*deprecate*','Poems and Ballads (1866)',752,'2005-01-08 11:42:46',2),(11,'fromAveatqueVale.html','from \"Ave atque Vale\"','A. C. Swinburne','*deprecate*','Poems and Ballads, Second Series (1878)',962,'2005-01-08 12:10:00',8),(12,'Insomnia.html','Insomnia','D. G. Rossetti','*deprecate*','Ballads and Sonnets (1881)',852,'2005-01-09 09:33:15',8),(13,'TheKraken.html','The Kraken','Alfred Tennyson','*deprecate*','Poems, Chiefly Lyrical (1830)',629,'2005-01-09 21:01:09',2),(14,'fromTheOctopus1872.html','from \"The Octopus\" (1872)','Arthur Clement Hilton','*deprecate*','RPO',568,'2005-01-09 21:19:09',8),(15,'test1.html','test 1','tester','*deprecate*','',15,'2005-01-10 10:10:35',15);
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
  `archived` tinyint(1) default '0',
  `restricted` tinyint(1) default '0',
  `private` tinyint(1) default '0',
  `retired` tinyint(1) default '0',
  `startDocWeight` int(11) default '1',
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `game`
--


/*!40000 ALTER TABLE `game` DISABLE KEYS */;
LOCK TABLES `game` WRITE;
INSERT INTO `game` VALUES (5,20,'silly test','this is just a system test','',0,0,0,0,1),(6,41,'Dream Land','This game will be played with Christina Rossetti\'s lyric poem \"Dream Land\".','Usual IVANHOE rules.',0,0,0,0,1);
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
INSERT INTO `keyspace` VALUES ('game',7),('action',70),('link_target',1),('move',17),('document',16),('discussion',1),('player',101),('role',19),('bookmarks',1),('category',1);
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
INSERT INTO `link_target` VALUES (16,4,0,0,0,'2005-01-07 14:56:49','','Textual Issues','Some fool put in a text of this poem and pretended it was The Germ text.  This text is simply something LIKE The Germ text.'),(17,4,0,0,0,'2005-01-07 15:31:05','','on sleeping women','I wish that my women would sleep more and speak less.'),(20,1,2,8,0,'2005-01-08 11:37:40','27101fef35e777f9:a1c1f5:1015329b845:-7ff3','To expose the intert',NULL),(21,4,0,0,0,'2005-01-08 11:30:38','','Intertext','Does Goblina mean us to read this poem in relation to Miss Rossetti\'s \"Dream Land\"?  It\'s intertext is surely something else altogether -- surely Miss Rossetti\'s brother\'s immortal sonnet \"Nuptial Sleep\".'),(22,1,2,9,0,'2005-01-08 11:37:40','27101fef35e777f9:a1c1f5:1015329b845:-7ff2','To expose the intert',NULL),(24,4,0,0,0,'2005-01-08 11:46:35','','Explication','\"Where\" is this poet pointing us to?  Surely to \"The purple land\" that is this poem she is writing.  This is the only land for which we care because it is the only land from which we can make relaible judgments about the world of illusion, \"reality\", and its \"dreams of life\" from which we are missioned, as poets, to awake all of the living dead.'),(25,4,0,0,0,'2005-01-08 11:48:12','','Explication','If a poet may be permitted to gloss his own work, this text opens with a word that signals to its readers \"where\" we are now living -- here in this verse.'),(26,1,2,8,0,'2005-01-08 11:37:40','27101fef35e777f9:a1c1f5:1015329b845:-7fba','',NULL),(27,1,2,10,0,'2005-01-08 11:57:07','27101fef35e777f9:a1c1f5:1015329b845:-7fbb','',NULL),(28,4,0,0,0,'2005-01-08 11:51:48','','Goblina\'s precursors','The reader of this text from \"a later day\" will want to negotiate the poem through its precessional verses, as for instance we have been observing.'),(29,4,0,0,0,'2005-01-08 11:55:03','','Intertexts again','See lines 9-12 of Miss Rossetti\'s \"Dream Land\".'),(31,1,8,11,0,'2005-01-08 12:15:25',NULL,'To draw the CONTRAST',NULL),(32,4,0,0,0,'2005-01-08 12:13:30','','Naive commentary','While one hestitates to introduce a reading that would appear to contradict, or at least seriously qualify, the commentary of a genius like Mr. Swinburne, nonetheless these lines seem to have little in common with Mr. Swinburne\'s attitudes toward sleep, death, and poetry as set forth in \"Ave atque Vale\", especially stands 4-5.'),(33,4,0,0,0,'2005-01-08 12:22:01','','The Text','While Mr. Swinburne was entirely correct to revise this text, which is certainly disgraceful, he neglects to observe all of the problems raised by the material.  I here accept (and incorporate) his corrections and will soon point out the other matters that seem pertinent to the issue,'),(49,4,0,0,0,'2005-01-09 09:35:14','','Thematic Query','It strikes me that CR and ACS both have their theories and aesthetics of sleeping and dreaming, but that DGR\'s main preoccupation is with insomniac wakefulness, as if his dreams were waking dreams (and nightmares).'),(50,1,8,8,0,'2005-01-07 16:13:59','27101fef35e777f9:501268:10157da8a73:-7ff5','Is Goblina\'s Undream',NULL),(51,1,8,11,0,'2005-01-09 09:38:45','27101fef35e777f9:501268:10157da8a73:-7ff6','Is Goblina\'s Undream',NULL),(52,4,0,0,0,'2005-01-09 16:26:21','','street smarts?','\"sweet smart\".  Adj + noun, or noun + adj?  Pleasant pain, that is, or intelligent pleasure?  I suspect a typesetter\'s error for our commodity-hip poet\'s likelier reading, \"sweets mart,\" i. e. the candy store to which Yeats patronizingly relegated the po-boy Keats who was Rossetti\'s great original.'),(53,4,0,0,0,'2005-01-09 16:39:02','','the shed of life','The shedding of \"drops\" (rain) joins up with the later \"tides\" and \"streams\" to form a complete if twisted hydrological cycle.  Vide Strangelove on our Precious Bodily Fluids.  But this is child\'s play to the architectural motif that starts up when the \"shed\" acquires \"eaves\" as by bricolage from Coleridge\'s \"Frost @ Midnight\".  The image lumbers on into shipwreck at mid-sonnet, then fresh forestation of \"new woods\" assures us there\'s plenty more where that came from.'),(54,4,0,0,0,'2005-01-09 20:36:19','','Whose poem?','Everyone assumes this poem is by CR.  But are we perhaps dealing with a hoax?  A plagiarism?  I see the poem is attributed to one \"Ellen Allyn\" in a table of contents secreted away at the end of the second issue of The Germ (the T of C in the first issue gives no authorship at all).  \"Ellen Allyn\"?  PLEASE!  (Or why not Bonny Barbara Allen? -- since we\'re clearly at games with words!)  CR published a version of the text in 1862 as her own, -- very close to this 1850 text.  Is that a hoax too?  And what hangeth upon the name?'),(56,4,0,0,0,'2005-01-09 21:06:49','','Pre-Christian dreams','An interesting apocalyptic vision from the laureate\'s pre-laureate days, when he still had a sense of humour.  And before my poor friend Rossetti plunged into his daylight nightmares and insomniac terrors, he was one of the few who understood the comic potential of Alfred\'s early squib (see \"MacCraken\").'),(57,1,8,14,0,'2005-01-09 21:31:59','27101fef35e777f9:6a21b2:1015a5a69fb:-7ffb','',NULL),(59,1,8,13,0,'2005-01-09 21:31:59','27101fef35e777f9:6a21b2:1015a5a69fb:-7ffc','',NULL),(60,4,0,0,0,'2005-01-09 21:23:25','','Essential gloss','Having just read Harold Bloom\'s \"Anxiety of Influence\", I see more clearly than ever the \"hidden roads than go from poem to poem\".  \"The Octopus\" only APPEARS to be a parody of ACS; it\'s real target is . . .the laureate himself!  Sleepers awake!  Sleepers awake, indeed!  I am not dead, I do not sleep, I have awakened from the dream of verse.'),(63,4,0,0,0,'2005-01-09 21:29:43','','Textual error','Well, perhaps not exactly an error, simply a diversion  and coded text.  The Kraken indeed! AT actually meant \"the critic\", in prophetic awareness of the soon-to-appear review of his 1830 book of poems, which would be mauled by anabysmal creature incapable of perceiving the subsurface Tennysonian sunlight.'),(64,1,8,13,0,'2005-01-09 21:31:59','27101fef35e777f9:6a21b2:1015a5a69fb:-7fcd','More hidden roads',NULL),(65,1,8,14,0,'2005-01-09 21:31:59','27101fef35e777f9:6a21b2:1015a5a69fb:-7fce','More hidden roads',NULL),(66,1,8,13,0,'2005-01-09 21:31:59','27101fef35e777f9:6a21b2:1015a5a69fb:-7fb6','Homage to the wit an',NULL),(67,1,12,9,0,'2005-01-09 21:38:58','27101fef35e777f9:6a21b2:1015a5a69fb:-7fb7','Homage to the wit an',NULL);
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
INSERT INTO `move` VALUES (1,6,2,'The text has been corrected by me because I can\'t imagine a poet as fastidious as Miss Rossetti being able to tolerate such a sloppy version of her work being made available to the public.','2005-01-07 14:53:19','2005-01-07 15:00:46',NULL,NULL),(2,6,4,'Can women who appear in language ever sleep?','2005-01-07 15:31:05','2005-01-07 15:32:57',NULL,NULL),(3,6,6,'To create a counter-text to the first text, one that resonates and rhymes with the original, but in another key.','2005-01-07 16:14:02','2005-01-07 16:15:20',NULL,NULL),(4,6,2,'To begin a process of extrapolating the multiple resonances that are generated by any poetical field -- \"the hidden roads that go from poem to poem\".','2005-01-08 11:30:38','2005-01-08 11:37:40',NULL,NULL),(5,6,2,'More relevant texts, more relevant connections.','2005-01-08 11:42:47','2005-01-08 11:57:07',NULL,NULL),(6,6,8,'To dissallow essential authority even to genius.','2005-01-08 12:10:01','2005-01-08 12:15:25',NULL,NULL),(7,6,8,'A deferential comment on genius as well as a modest critical proposal.','2005-01-08 12:22:01','2005-01-08 12:24:28',NULL,NULL),(8,6,8,'More texts to complicate the thematic issues of sleeping, waking, dreaming, and perceiving.','2005-01-09 09:29:00','2005-01-09 09:38:45',NULL,NULL),(9,6,12,'It should provoke by irritating the reader slumbering into sensuality.','2005-01-09 16:26:21','2005-01-09 16:28:06',NULL,NULL),(10,6,12,'to provoke','2005-01-09 16:39:02','2005-01-09 16:43:35',NULL,NULL),(11,6,8,'To provoke some discussion of the textual issues.','2005-01-09 20:36:19','2005-01-09 20:38:06',NULL,NULL),(12,6,2,'A poem to put one to sleep.','2005-01-09 21:01:09','2005-01-09 21:16:40',NULL,NULL),(13,6,8,'A few intertextual markers that may help to waken us from the dream of verse.','2005-01-09 21:19:09','2005-01-09 21:31:59',NULL,NULL),(14,6,8,'Sir William Impson and other intersted wits (to wit, Goblina) may find the commentary to line 4 of \"The Kraken\" a text to batten upon.','2005-01-09 21:34:00','2005-01-09 21:38:58',NULL,NULL),(15,5,15,'Is this private or PUBLIC.  I think it\'s public.','2005-01-10 10:10:36','2005-01-10 10:12:09',NULL,NULL),(16,6,18,'','2005-01-10 19:34:59',NULL,NULL,NULL);
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
INSERT INTO `move_action` VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(2,17),(3,18),(4,19),(4,20),(4,21),(4,22),(5,23),(5,24),(5,25),(5,26),(5,27),(5,28),(5,29),(6,30),(6,31),(6,32),(7,33),(7,34),(7,35),(7,36),(7,37),(7,38),(7,39),(7,40),(7,41),(7,42),(7,43),(7,44),(7,45),(7,46),(7,47),(8,48),(8,49),(8,50),(8,51),(9,52),(10,53),(11,54),(12,55),(12,56),(13,57),(13,58),(13,59),(13,60),(13,61),(13,62),(13,63),(13,64),(13,65),(14,66),(14,67),(15,68),(16,69);
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
  `new_game_permission` tinyint(1) default '0',
  `new_role_permission` tinyint(1) default '0',
  `write_permission` tinyint(1) default '0',
  `admin` tinyint(1) default '0',
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `player`
--


/*!40000 ALTER TABLE `player` DISABLE KEYS */;
LOCK TABLES `player` WRITE;
INSERT INTO `player` VALUES (17,'nick','e2e42a07550863f8b67f5eb252581f6d','Nick','Laiacona','ncl2n@virginia.edu','Developer',0,0,0,0),(18,'ben','8432ad4ad0ee9031b093df6460d87693','Ben','Cummings','bjc8r@virginia.edu','',0,0,0,0),(20,'duane','357ddb585594fe6400d3114fc94239c2','Duane','Gran','dmg2n@virginia.edu','ARP',0,0,0,0),(30,'Nathan','ca9c54e9a4d913f3777f5427a301ab83','Nathan','Piazza','nfp5e@virginia.edu','',0,0,0,0),(31,'Laura','b077f51ff36868f21ea52956adcf7ff4','Laura','Mandell','mandellc@muohio.edu','Miami Univ. of Ohio',0,0,0,0),(32,'Girl Poet','c1a34aae7d4bd9eee17160f1f034b756','Johanna','Drucker','jrd8e@virginia.edu','',0,0,0,0),(33,'jerome','2bb010060d682fee5ad19d973a9a4d2a','jerome','mcgann','jjm2f@virginia.edu','',0,0,0,0),(38,'Beth','f0d78724487b188c0df666f874af6b27','Bethany','Nowviskie','bethany@virginia.edu','ARP',0,0,0,0),(39,'Tucker','91a026ab57eac2af3f191985dad8bd7a','Chip','Tucker','tucker@virginia.edu','UVA',0,0,0,0),(40,'Rob Pope','d5a57c8f65bc0da0af40bf2e52467ec5','Rob','Pope','rfpope@brookes.ac.uk','Oxford Brookes University',0,0,0,0),(41,'Jerome McGann','5f4dcc3b5aa765d61d8327deb882cf99','Jerome','McGann','jmcgann@virginia.edu','ARP @ UVA',0,0,0,0),(100,'Jessica Feldman','5f4dcc3b5aa765d61d8327deb882cf99','Jessica','Feldman','jrf2j@virginia.edu','UVA',0,0,0,0);
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
INSERT INTO `player_game_role` VALUES (41,6,1),(41,6,2),(100,6,3),(100,6,4),(32,6,5),(32,6,6),(17,6,7),(41,6,8),(20,6,9),(39,6,10),(39,6,11),(39,6,12),(38,6,13),(41,5,14),(41,5,15),(38,6,16),(18,6,17),(30,6,18);
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
  `write_permission` tinyint(1) default '0',
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

--
-- Dumping data for table `role`
--


/*!40000 ALTER TABLE `role` DISABLE KEYS */;
LOCK TABLES `role` WRITE;
INSERT INTO `role` VALUES (1,'Jerome McGann','','',-256,-16776961,1),(2,'A. C. Swinburne','The role is the poet and close friend of CR>\n','To play with CR\'s text from a pagan and atheitic point of view.',-6710785,-14237914,1),(3,'Jessica Feldman','','',-256,-16776961,1),(4,'Yeats','The poet W.B. Yeats','to be interested in what I\'m doing and others are doing.',-256,-16776961,1),(5,'Girl Poet','','',-256,-16776961,1),(6,'Goblina','A charming, dark female creature bent on mayhem. ','To play with the one of the great poems of perverse female sexuality. ',-52429,-13159,1),(7,'nick','','',-256,-16776961,1),(8,'Boy Poet','This is a young second year college student who has recently, as a result of reading Swinburne\'s \"Anactoria\", dropped his Statistics Major to a Statistics Minor and decided to major in Abnoral Psychology, a special field in his university.','To dig more deeply into the strange and wonderful dynamics that play about in this field of verse and re-verse.',-14237837,-9191489,1),(9,'duane','','',-256,-16776961,1),(10,'Tucker','','',-256,-16776961,1),(11,'cyaxanth','Jungian Dream Analyst lately certified by Buenos Aires institute, no patients, eager to find and spread the truth about dreams.  Blue for the collective deeps, gold for the dawn of enlightenment','to convert all interpretands into the true system of meaning',-256,-16776961,1),(12,'Sir Wm Impson','mad textualist, buzzsaw against the grain of meaning','highlight alternative readings inconclusively',-4249997,-8368871,1),(13,'Beth','','',-256,-16776961,1),(14,'Jerome McGann','','',-256,-16776961,1),(15,'tester','test role','to test',-256,-16776961,1),(16,'Laureate','I am aging poet laureate Robert Bridges, friend and posthumous editor of Gerard Manley Hopkins, who greatly admired Christina Rossetti.','to inject (and simultaneously resist?) a little Sprung Rhythm into the proceedings.',-4230285,-8368819,1),(17,'ben','','',-14276929,-11698048,1),(18,'Nathan','','',-256,-16776961,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `role` ENABLE KEYS */;

