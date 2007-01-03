-- MySQL dump 9.11
--
-- Host: localhost    Database: ivanhoe_tucker
-- ------------------------------------------------------
-- Server version	4.0.24_Debian-2-log

--
-- Current Database: ivanhoe_tucker
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `ivanhoe_tucker`;

USE ivanhoe_tucker;

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
INSERT INTO `action` VALUES (1,2,'d6873a7da95bd015:33a222b5:101b6023128:-7fe3','2005-01-27 16:13:26',1330,'forward',NULL),(2,1,'d6873a7da95bd015:33a222b5:101b6023128:-7fe2','2005-01-27 16:13:26',1321,'approving',NULL),(3,1,'907576bcb6437d08:5c9ba9ad:101b6831fc6:-8000','2005-01-27 18:30:42',10,'(My host graciously led me up the stair\nand at the top he bid me stop and stare\nbehind a velvet curtain that he drew\naside for me alone to raptly view.)\n',NULL),(4,1,'907576bcb6437d08:5c9ba9ad:101b6831fc6:-7e4f','2005-01-27 18:36:37',333,'(Here he proffered a rich and costly chair\nopposed to portrait more than passing fair;\nand certainly a wonder one might call, \nthat pleasing ornaments His Grace\'s hall,\nlovely as the Pyramids in sum,\na marvelous proper simulacrum.)\n',NULL),(5,4,'907576bcb6437d08:5c9ba9ad:101b6831fc6:-7d32','2005-01-27 18:58:28',0,'A Copy of the Portrait of the Duchess',NULL),(6,5,'907576bcb6437d08:5c9ba9ad:101b6831fc6:-7d30','2005-01-27 18:59:00',1357,'LadyDecies.jpg',NULL),(7,1,'907576bcb6437d08:5c9ba9ad:101b6831fc6:-7d2f','2005-01-27 18:59:09',1,'My Liege,\n    Enclosed is a copy I have had executed in miniature of the Duke\'s wondrous portrait of his last Duchess, of which I spoke in my first report. Fra Pandolf, the painter, seems a gifted artisan in the modern mode; perhaps Your Grace might find commission for him to execute portraits of Your Grace or the Countess. As I mentioned, the Duke seems something of a jealous man, (felicitous as that will surely be in instructing Your Grace\'s young daughter in the proprieties and duties of matrimonial life) keeping his original hidden behind a velvet curtain in a hall alcove, and only assented to my having a copy made after repeated invocations of Your Grace\'s name, and reminders of the generous, nay, exorbitant, dowry that Your Grace represents. Even after finally relenting, he seemed quite grudging, and I would humbly advise Your Grace to relate to the Duke in any correspondence both your gratitude for his condescension and your intent to keep the copy nearly as cloistered as the original. Further letters and tokens of good will are forthcoming. As always, I remain\n                                                 Your Ob\'d\'t Serv\'t,\n                                                                      Silvio Quarante\n                                                                        Envoy Plenipotentiary\n\n                     ',NULL),(8,3,'907576bcb6437d08:5c9ba9ad:101b6831fc6:-7689','2005-01-27 19:18:10',1321,'approving',NULL),(10,1,'9c9694cd2f312628:8ebb5a:101b6b1ae2f:-8000','2005-01-27 19:21:32',1233,'hearty, spirited, and well-tended ',NULL),(11,1,'9c9694cd2f312628:a2d304:101b6cf42ac:-7ffa','2005-01-27 19:54:05',2441,' Her mule rides also stopped.  The animal was dead soon after its mistress.  ',NULL),(12,1,'9c9694cd2f312628:a2d304:101b6cf42ac:-7fba','2005-01-27 19:54:32',1310,'-- she would ride every day, with the mule breaking out into a lovely canter.  The lady was afraid of horses -- they were large, noisy and violent.  So insteads she was given the white mule -- it was gentle yet strong, and she lavished many caresses upon the dumb beast ',NULL),(13,1,'9c9694cd2f312628:a2d304:101b6cf42ac:-7e2b','2005-01-27 19:56:47',1677,' and even the filthy Stableboy, who would watch her with pathetic devotion',NULL),(14,2,'9c9694cd2f312628:a2d304:101b6cf42ac:-7da6','2005-01-27 19:57:45',2554,' ',NULL),(15,1,'9c9694cd2f312628:a2d304:101b6cf42ac:-7bc4','2005-01-27 20:14:27',2674,'I ordered the foul picture burned.  ',NULL),(16,1,'9c9694cd2f312628:a2d304:101b6cf42ac:-7ba5','2005-01-27 20:14:42',1579,'.  An itinerant artist came by one day, and drew a picture of the mule for my lady -- a cheap, tawdry thing, not worth the time or homage she wasted upon it',NULL),(17,4,'9c9694cd2f312628:a2d304:101b6cf42ac:-7bf3','2005-01-27 20:13:25',0,'A Sketch of My Lady\'s Mule',NULL),(18,3,'ae4829f08456d081:394a8d:101b9a1dc1d:-7ff4','2005-01-28 09:27:20',1706,'forward',NULL),(19,3,'b1c9c29693f28dda:1404c51:101ba889b9c:-7ffd','2005-01-28 13:31:49',189,'a gifted artisan in the modern mode',NULL),(20,1,'91de22f1aa05b4a6:3727fc29:101ba8dc59e:-7fff','2005-01-28 13:35:39',1624,' ',NULL),(21,2,'91de22f1aa05b4a6:3727fc29:101ba8dc59e:-7f4b','2005-01-28 13:37:09',1623,' ',NULL),(22,1,'91de22f1aa05b4a6:3727fc29:101ba8dc59e:-7f4a','2005-01-28 13:37:10',1547,' Honor should not surrender to love.  (Sure, my mother would beg to differ.)',NULL),(23,3,'b1c9c29693f28dda:1404c51:101ba889b9c:-7ff7','2005-01-28 13:43:20',2442,'Her mule rides also stopped.  The animal was dead soon after its mistress.',NULL),(24,3,'b1c9c29693f28dda:1404c51:101ba889b9c:-7ffa','2005-01-28 13:37:18',1321,'approvingforward ',NULL),(26,2,'dd6d42e68f67f0f4:397443ed:101bad4a202:-7fd9','2005-01-28 14:43:11',2560,'a rarity',NULL),(27,1,'dd6d42e68f67f0f4:397443ed:101bad4a202:-7fc5','2005-01-28 14:43:19',2541,'an unpopular piece.',NULL),(28,1,'dd6d42e68f67f0f4:397443ed:101bad4a202:-7fb2','2005-01-28 14:43:44',2617,' I show it to you\nso that it might not go totally unnoticed. Sculpture is a peasant\'s \npleasure; a school for the colorblind.',NULL),(29,2,'dd6d42e68f67f0f4:397443ed:101bad4a202:-7e6b','2005-01-28 14:53:05',114,'\n',NULL),(30,1,'dd6d42e68f67f0f4:397443ed:101bad4a202:-7e6a','2005-01-28 14:53:06',9,'Though we be in a rush, we must stop and see a magnificent painting prominently displayed on yonder wall.',NULL);
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
INSERT INTO `action_document` VALUES (1,7,3,'2005-01-27 14:35:39'),(2,7,3,'2005-01-27 14:35:39'),(3,7,5,'2005-01-27 14:35:39'),(4,7,5,'2005-01-27 14:35:39'),(5,8,5,'2005-01-27 18:58:28'),(6,8,5,'2005-01-27 18:58:28'),(7,8,5,'2005-01-27 18:58:28'),(8,7,3,'2005-01-27 16:33:34'),(10,7,7,'2005-01-27 14:35:39'),(11,7,7,'2005-01-27 14:35:39'),(12,7,7,'2005-01-27 14:35:39'),(13,7,7,'2005-01-27 14:35:39'),(14,7,7,'2005-01-27 14:35:39'),(15,7,7,'2005-01-27 20:03:15'),(16,7,7,'2005-01-27 20:03:15'),(17,10,7,'2005-01-27 20:13:25'),(18,7,5,'2005-01-27 19:24:10'),(19,8,5,'2005-01-27 19:24:10'),(20,7,13,'2005-01-27 14:35:39'),(21,7,13,'2005-01-27 14:35:39'),(22,7,13,'2005-01-27 14:35:39'),(23,7,7,'2005-01-27 20:03:15'),(24,7,3,'2005-01-27 16:33:34'),(26,7,16,'2005-01-27 14:35:39'),(27,7,16,'2005-01-27 14:35:39'),(28,7,16,'2005-01-27 14:35:39'),(29,7,16,'2005-01-27 14:35:39'),(30,7,16,'2005-01-27 14:35:39');
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
INSERT INTO `category` VALUES (1,'MASTER','a move which works to reify the poem\'s implied power structures from the top down',5),(2,'subordinate','a move which resists (analyzes) the poem\'s implied power structures from the bottom up',5);
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
INSERT INTO `db_history` VALUES ('2004-12-02 10:41:59','removed document.publication_date, added db_histor','1.1'),('2004-12-02 10:42:44','convert player to role id','1.1'),('2004-12-02 10:43:32','convert link tag string to field','1.1'),('2005-01-05 15:18:51','added the bookmarks table','1.2'),('2005-01-05 15:19:45','convert discussion data from player id to role id','1.2'),('2005-04-11 11:53:44','Added \'archived\' field to game table','1.5'),('2005-04-11 11:53:44','Added \'private\' field to game table','1.5'),('2005-04-11 11:53:44','Added \'write_permission\' field to role table','1.5'),('2005-04-11 11:53:44','Added \'new_game_permission\' field to player table','1.5'),('2005-04-11 11:53:44','Added \'new_role_permission\' field to player table','1.5'),('2005-04-11 11:53:44','Added \'write_permission\' field to player table','1.5'),('2005-04-11 11:53:44','Added \'admin\' field to player table','1.5');
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
INSERT INTO `discourse_field` VALUES (5,7,1,1),(5,8,0,1),(5,10,0,0),(5,11,0,0);
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
INSERT INTO `discussion` VALUES (1,3,5,'first test post','<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n      just testing the forum...\r\n    </p>\r\n  </body>\r\n</html>\r\n','2005-01-27 15:57:06',-1),(2,3,5,'RE: first test post','<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n      responding...\r\n    </p>\r\n  </body>\r\n</html>\r\n','2005-01-27 15:57:31',-1),(3,5,5,'Some possibly helpful shorthand','<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n      I\'ve done a few online role-playing type activities, so if interested, \r\n      folks might find a few designations helpful.\r\n    </p>\r\n    <p style=\"margin-top: 0\">\r\n      <i>IC: in character </i>(spoken or acted from the perspective of your \r\n      character)\r\n    </p>\r\n    <p style=\"margin-top: 0\">\r\n      <i>OOC: out of character </i>((breaking character; talking as your \r\n      real-world self, sometimes known as a \'mun\'; OOC is often designated \r\n      with the use of double parentheses))\r\n    </p>\r\n  </body>\r\n</html>\r\n','2005-01-27 19:28:06',-1),(4,8,5,'Helpful hint','<html>\n  <head>\n\n  </head>\n  <body>\n    <p>\n      I\'m happy to see all the good Ivanhoe moves happening here already.  I \n      wanted to mention a tip to get you going on understanding the multiple \n      perspectives.  If you pull up <b>The Count\'s Envoy</b> move #1 where \n      s/he added &quot;a copy of the portrait of the duchess&quot; you might be \n      surprised.  If it is blank, this is because you are looking at your \n      version.  Click on the role circle for <b>The Count\'s Envoy</b> and then \n      re-open the document.  You will now see the player\'s perspective, which \n      includes commentary and a graphic portrait.\n    </p>\n  </body>\n</html>\n','2005-01-27 20:48:41',-1),(5,8,5,'Another hint','<html>\n  <head>\n\n  </head>\n  <body>\n    <p>\n      You may find the history log (look for icon of a log on  the left) to be \n      useful when analyzing other player\'s moves.  It can group them by player \n      or by move, in time sequence.\n    </p>\n    <p>\n      Also, when you select a move from the history log it will set the \n      timeline to that particular &quot;tick&quot; on the visualization.\n    </p>\n  </body>\n</html>\n','2005-01-27 20:51:22',-1);
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
INSERT INTO `document` VALUES (7,'1842fixed.html','Italy','Robert Browning','*deprecate*','Bells and Pomegranates, 1842',2494,'2005-01-27 14:35:39',-1),(8,'ACopyofthePortraitoftheDuchess.html','A Copy of the Portrait of the Duchess','The Count\'s Envoy','*deprecate*','',1,'2005-01-27 18:58:28',5),(10,'mulebaby.html','A Sketch of My Lady\'s Mule','Itinerant Artist','*deprecate*','Stableboy',33,'2005-01-27 20:13:25',7),(11,'MemorandumtotheCount.html','Memorandum to the Count','Dottore','*deprecate*','Scientific Truth',1355,'2005-01-28 14:01:00',12);
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
INSERT INTO `document_image` VALUES (8,'LadyDecies.jpg'),(8,'LadyDecies.jpg'),(10,'mulebaby.jpg'),(8,'duchess.jpg');
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
INSERT INTO `game` VALUES (5,38,'My Last Duchess','This is a test game for ENNC 482A (\"Victorian Relativity\").  Our text is Robert Browning\'s \"My Last Duchess,\" and the game will last one week, during which players are expected to familiarize themselves with IVANHOE\'s concepts and features by making a set of experimental moves.','Explore the IVANHOE interface and think critically about perspective and textual intervention.',0,0,0,0,1);
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
INSERT INTO `keyspace` VALUES ('game',6),('action',31),('link_target',1),('move',11),('document',12),('discussion',6),('player',120),('role',17),('bookmarks',1),('category',3);
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
INSERT INTO `link_target` VALUES (8,4,0,0,0,'2005-01-27 19:18:10','','Misspoken?','Certainly we all do not express ourselves ideally, Your Grace...but perhaps one might ask what prompted the use of such a word as \"forward\" even if it was not your precise meaning?'),(18,4,0,0,0,'2005-01-28 09:27:20','','forward and retreat','In answer, I can only say that none puts by the curtain I have drawn for you but I.\nShe put herself too much forward, you see -- but I cannot allow that forwardness to propel the words I use to paint her portrait.  Even had you skill in speech -- (which I have not) -- you\'d find her an impossible object.  I mean, \"subject.\" '),(19,4,0,0,0,'2005-01-28 13:31:49','','anatomical art','Not the least of Fra Pandolf\'s many painterly excellences is that blessed mixture of social tact with anatomical precision which has been the envy of Rafaelle d\'Urbino and the inspiration of (his brother alike in the art and in the church) Fra Lippo Lippi.  I attended personally on the late Duchess during her alas! too brief tenure as ducal consort.  And my medical knowledge permits me to affirm, what the portrait bears out, a very slight (and to many charming) scoliosis or stoop from the waist.  May I venture my hypothesis that it was this charm of her person which motivated the Duke\'s observation -- misspoken, perhaps, but surely not misremembered -- that there was something \"forward\" about her?'),(23,4,0,0,0,'2005-01-28 13:43:20','','no coincidence','This seeming coincidence between the deaths of our late Duchess and her favorite pet the mule gives striking confirmation to the doctrines of Paracelsus (with whom I had the honor to study at Basel across the lofty Alps) concerning the ubiquitous system of \"correspondences,\"  For reasons that I have vouchsafed to a separate document, an occult analogy between the pure lady and her blameless steed explains, to my poor satisfaction at any rate, the convergence in their departures from this life.'),(24,1,12,8,0,'2005-01-28 13:35:19',NULL,'my comment on the po',NULL);
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
  `fk_category_id` int(11) NOT NULL default '0',
  `title` varchar(255) default NULL,
  `description` text,
  `start_date` datetime default NULL,
  `submit_date` datetime default NULL
) TYPE=MyISAM;

--
-- Dumping data for table `move`
--


/*!40000 ALTER TABLE `move` DISABLE KEYS */;
LOCK TABLES `move` WRITE;
INSERT INTO `move` VALUES (1,5,3,2,NULL,'I misspoke. To clarify my meaning, I have changed one word of the text.  I ALWAYS intended to keep the Duchess in her place.','2005-01-27 16:13:26','2005-01-27 16:33:34'),(2,5,5,2,NULL,'His Grace was very kind in allowing me to have a copy of his portrait of his former Duchess sent to my master, the Count, who is much interested in his daughter\'s likely predecessor, particularly regarding her abrupt exit from the scene. \nI have begun composing a report ((My \'Italy\')) to the Count regarding my conversations with the Duke, with his words nearly as I could recall them, and my own actions and speech. \nHis calling of his wife\'s looks to everyone as \"forward\" and the quick change to \"approving\" which followed seemed a bit hasty and odd for a mere misstatement, as the Duke claims.','2005-01-27 18:30:42','2005-01-27 19:24:10'),(3,5,7,1,NULL,'The sad fate of the mule has been hitherto neglected.  I want to correct this, and emphasize how much the animal meant to the last Duchess -- and to me.','2005-01-27 19:21:32','2005-01-27 20:03:15'),(4,5,7,1,NULL,'','2005-01-27 20:13:51',NULL),(5,5,3,1,NULL,'An answer to the Envoy\'s question.  (And how dare he question ME?)','2005-01-28 09:27:20','2005-01-28 09:30:04'),(6,5,12,1,NULL,'Science interprets art in the service of aristocracy','2005-01-28 13:31:49','2005-01-28 13:35:19'),(7,5,13,1,NULL,'The duke\'s mindset on love versus honor is a little backwards.','2005-01-28 13:35:39','2005-01-28 13:42:39'),(8,5,12,1,NULL,'coincidence? hardly!','2005-01-28 13:37:18','2005-01-28 13:45:19'),(10,5,16,2,NULL,'Shall my art be equal to a Clown of Innsbruck\'s sculpture of a mythological character? All of my paintings are hung in the foyer. Why else visit than to look and loiter?','2005-01-28 14:43:11','2005-01-28 14:58:56');
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
INSERT INTO `move_action` VALUES (1,1),(1,2),(2,3),(2,4),(2,5),(2,6),(2,7),(2,8),(3,10),(3,11),(3,12),(3,13),(3,14),(4,15),(4,16),(4,17),(5,18),(6,19),(7,20),(7,21),(7,22),(8,23),(8,24),(10,26),(10,27),(10,28),(10,29),(10,30);
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
INSERT INTO `move_inspiration` VALUES (2,1),(5,1),(5,2),(6,1),(6,2),(6,5),(8,3);
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
INSERT INTO `player` VALUES (17,'nick','e2e42a07550863f8b67f5eb252581f6d','Nick','Laiacona','ncl2n@virginia.edu','Developer',0,0,0,0),(18,'ben','8432ad4ad0ee9031b093df6460d87693','Ben','Cummings','bjc8r@virginia.edu','',0,0,0,0),(20,'Duane','357ddb585594fe6400d3114fc94239c2','Duane','Gran','dmg2n@virginia.edu','ARP',0,0,0,0),(38,'Bethany','f0d78724487b188c0df666f874af6b27','Bethany','Nowviskie','bethany@virginia.edu','ARP',0,0,0,0),(101,'Susan Anspach','1a07bcc79f21590b3ed2622d5807bdd0','Susan','Anspach','sca3d@Virginia.EDU','Student',0,0,0,0),(102,'Christopher Barbatti','48c8c3963853fff20bd9e8bee9bd4c07','Christopher','Barbatti','cvb3r@Virginia.EDU','Student',0,0,0,0),(103,'Jason Bowman','544a4f59f691574154a60b8539ebf914','Jason','Bowman','jab3ht@Virginia.EDU','Student',0,0,0,0),(104,'Zachary Brown','95b09698fda1f64af16708ffb859eab9','Zachary','Brown','zpb6d@Virginia.EDU','Student',0,0,0,0),(105,'William Daughtrey','eb9fc349601c69352c859c1faa287874','William','Daughtrey','whd6f@Virginia.EDU','Student',0,0,0,0),(106,'Eleanor Donlon','30de24287a6d8f07b37c716ad51623a7','Eleanor','Donlon','ebd4j@Virginia.EDU','Student',0,0,0,0),(107,'Jaime Knauf','3483e5ec0489e5c394b028ec4e81f3e1','Jaime','Knauf','jnk4a@Virginia.EDU','Student',0,0,0,0),(108,'Anne Lee','6d3a2d24eb109dddf78374fe5d0ee067','Anne','Lee','ael8z@Virginia.EDU','Student',0,0,0,0),(109,'Jessica Stallings','eab0141b79354969d1edd234fbc07422','Jessica','Stallings','jas5rz@Virginia.EDU','Student',0,0,0,0),(110,'Tucker','2f2ec1296695a9fb3251bbc94a2e0cef','Chip','Tucker','ht2t@virginia.edu','Professor',0,0,0,0),(33,'jerome','2bb010060d682fee5ad19d973a9a4d2a','jerome','mcgann','jjm2f@virginia.edu','',0,0,0,0),(41,'Jerome McGann','5f4dcc3b5aa765d61d8327deb882cf99','Jerome','McGann','jmcgann@virginia.edu','ARP @ UVA',0,0,0,0),(50,'Jessica Feldman','5f4dcc3b5aa765d61d8327deb882cf99','Jessica','Feldman','jrf2j@virginia.edu','UVA',0,0,0,0);
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
INSERT INTO `player_game_role` VALUES (17,5,1),(38,5,2),(38,5,3),(102,5,4),(102,5,5),(106,5,6),(106,5,7),(20,5,8),(105,5,9),(110,5,10),(107,5,11),(110,5,12),(107,5,13),(50,5,14),(41,5,15),(105,5,16);
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
INSERT INTO `role` VALUES (1,'nick','','',-256,-16776961,1),(2,'Bethany','','',-256,-16776961,1),(3,'Duke','I am a noble of the house of....','I want to control everything that happens in this game!',-15105920,-4250074,1),(4,'Christopher Barbatti','','',-256,-16776961,1),(5,'The Count\'s Envoy','A diplomatic and legal functionary dispatched by the Count to the Duke of Ferrara to negotiate the final stages of a betrothal between said Duke and the Count\'s daughter. Polite. Curious. Instructed to devise ways to decrease the dowry, as well as to ascertain how the Duke stands disposed to the Count\'s daughter, and what happened to the former Duchess who has mysteriously vanished. Also representative at the engagement social events being held. ','Negotiate with the Duke to:\n   make the dowry arrangement more advantageous for the Count;\n   see what kind of man the Duke is in regards to people and marriage;\n   inquire about former Duchess\'s end.',-8355815,-9230657,1),(6,'Eleanor Donlon','','',-256,-16776961,1),(7,'Stableboy','Caretaker of white mule.','To keep my position in the house of the Duke.',-4210906,-14237914,1),(8,'Duane','','',-256,-16776961,1),(9,'William Daughtrey','','',-256,-16776961,1),(10,'Tucker','','',-256,-16776961,1),(11,'Jaime Knauf','','',-256,-16776961,1),(12,'Dottore','Physician for many years to the ducal court of Ferrara','to promote the lineage interests of my master the Duke while advancing, in this age of discoveries, the frontiers of medical science',-4250074,-8368794,1),(13,'Duke\'s mother','I am understanding but opinionated.		','I want to help set things straight and end all confusion of love.  ',-52327,-26164,1),(14,'Jessica Feldman','','',-256,-16776961,1),(15,'Jerome McGann','','',-256,-16776961,1),(16,'Fra Pandolf','I am a great painter. My work should hang in the most prominent spot of every venue. I am greater than the rest of these Italian Painters!','To reveal the greatness of my art; specifically, my ability to capture human emotion. ',-9191565,-205,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `role` ENABLE KEYS */;

