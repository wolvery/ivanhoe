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
INSERT INTO `action` VALUES (1,4,'b3ef3bc3d121547a:158497c:1014ebcecc6:-8000','2005-01-07 14:52:22',0,'Dream Land',NULL),(2,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fff','2005-01-07 14:53:45',108,'\n',NULL),(3,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ffe','2005-01-07 14:53:47',199,'\n',NULL),(4,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ffd','2005-01-07 14:53:48',300,'\n',NULL),(5,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ffc','2005-01-07 14:53:49',416,'\n',NULL),(6,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ffb','2005-01-07 14:53:53',518,'\n',NULL),(7,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ffa','2005-01-07 14:53:54',612,'\n',NULL),(8,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ff9','2005-01-07 14:53:58',718,' --\n',NULL),(9,2,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7ff8','2005-01-07 14:54:05',712,'&mdash',NULL),(10,2,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fef','2005-01-07 14:54:13',329,'&rsquo',NULL),(11,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fe9','2005-01-07 14:54:18',328,'\'',NULL),(12,2,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fe8','2005-01-07 14:54:21',307,'&rsquo',NULL),(13,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fe2','2005-01-07 14:54:23',306,'\'',NULL),(14,2,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fe1','2005-01-07 14:54:29',12,'\n',NULL),(15,1,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fe0','2005-01-07 14:54:29',11,'\n',NULL),(16,3,'b3ef3bc3d121547a:158497c:1014ebcecc6:-7fdf','2005-01-07 14:56:49',1,'Dream Land',NULL),(17,3,'b3ef3bc3d121547a:16c14e7:1014edc9e6c:-7ffe','2005-01-07 15:31:05',66,'She sleeps a charmed sleep;',NULL),(18,4,'263b8754279ca0a4:bb628c:1014ef295f4:-7ffa','2005-01-07 16:13:59',0,'',NULL),(19,4,'27101fef35e777f9:a1c1f5:1015329b845:-7ff4','2005-01-08 11:33:06',0,'Nuptial Sleep',NULL),(20,3,'27101fef35e777f9:a1c1f5:1015329b845:-7ff2','2005-01-08 11:33:52',1,'Nuptial Sleep',NULL),(21,3,'27101fef35e777f9:a1c1f5:1015329b845:-8000','2005-01-08 11:30:38',12,'As dead, they lay, the slumbering souls\nMere girls, hair tossed among the sheets\nAnd as they breathed released from toils\nThey matched their measured breath and beats.\nThe darkest vision, still ahead, bewitched them into dreams\nThat kept them safe from prying eyes in that private bower.',NULL),(22,3,'27101fef35e777f9:a1c1f5:1015329b845:-7ff3','2005-01-08 11:33:52',12,'As dead, they lay, the slumbering souls\nMere girls, hair tossed among the sheets\nAnd as they breathed released from toils\nThey matched their measured breath and beats.\nThe darkest vision, still ahead, bewitched them into dreams\nThat kept them safe from prying eyes in that private bower.',NULL),(23,4,'27101fef35e777f9:a1c1f5:1015329b845:-7fcb','2005-01-08 11:42:46',0,'The Garden of Proserpine (excerpt)',NULL),(24,3,'27101fef35e777f9:a1c1f5:1015329b845:-7fca','2005-01-08 11:46:35',13,'WHERE',NULL),(25,3,'27101fef35e777f9:a1c1f5:1015329b845:-7fc8','2005-01-08 11:48:12',32,'Here',NULL),(26,3,'27101fef35e777f9:a1c1f5:1015329b845:-7fbb','2005-01-08 11:49:24',32,'Here',NULL),(27,3,'27101fef35e777f9:a1c1f5:1015329b845:-7fba','2005-01-08 11:49:24',1,'UnDreaming',NULL),(28,3,'27101fef35e777f9:a1c1f5:1015329b845:-7fb8','2005-01-08 11:51:48',32,'Here',NULL),(29,3,'27101fef35e777f9:a1c1f5:1015329b845:-7fb7','2005-01-08 11:55:03',158,'I watch the green field growing\nFor reaping folk and sowing,\nFor harvest-time and mowing,\nA sleepy world of streams.',NULL),(30,4,'27101fef35e777f9:73d10f:1015349a42d:-7ffa','2005-01-08 12:10:00',0,'from \"Ave atque Vale\"',NULL),(31,3,'27101fef35e777f9:73d10f:1015349a42d:-7ff9','2005-01-08 12:11:08',723,'Sleep that no pain shall wake,\nNight that no morn shall break,\nTill joy shall overtake\nHer perfect peace.',NULL),(32,3,'27101fef35e777f9:73d10f:1015349a42d:-7ff1','2005-01-08 12:13:30',723,'Sleep that no pain shall wake,\nNight that no morn shall break,\nTill joy shall overtake\nHer perfect peace.',NULL),(33,3,'27101fef35e777f9:73d10f:1015349a42d:-7ff0','2005-01-08 12:22:01',1,'Dream Land',NULL),(34,2,'27101fef35e777f9:73d10f:1015349a42d:-7fee','2005-01-08 12:22:06',329,'&rsquo',NULL),(35,1,'27101fef35e777f9:73d10f:1015349a42d:-7fe8','2005-01-08 12:22:08',328,'\'',NULL),(36,2,'27101fef35e777f9:73d10f:1015349a42d:-7fe7','2005-01-08 12:22:11',307,'&rsquo',NULL),(37,1,'27101fef35e777f9:73d10f:1015349a42d:-7fe1','2005-01-08 12:22:12',306,'\'',NULL),(38,2,'27101fef35e777f9:73d10f:1015349a42d:-7fe0','2005-01-08 12:22:16',715,'&mdash',NULL),(39,1,'27101fef35e777f9:73d10f:1015349a42d:-7fda','2005-01-08 12:22:18',712,'-- ',NULL),(40,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd7','2005-01-08 12:22:26',11,'\n',NULL),(41,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd6','2005-01-08 12:22:28',108,'\n',NULL),(42,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd5','2005-01-08 12:22:30',199,'\n',NULL),(43,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd4','2005-01-08 12:22:33',300,'\n',NULL),(44,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd3','2005-01-08 12:22:36',416,'\n',NULL),(45,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd2','2005-01-08 12:22:38',518,'\n',NULL),(46,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd1','2005-01-08 12:22:43',612,'\n',NULL),(47,1,'27101fef35e777f9:73d10f:1015349a42d:-7fd0','2005-01-08 12:22:46',721,'\n',NULL),(48,4,'27101fef35e777f9:501268:10157da8a73:-7ff1','2005-01-09 09:33:15',0,'Insomnia',NULL),(49,3,'27101fef35e777f9:501268:10157da8a73:-7ff0','2005-01-09 09:35:14',1,'Insomnia',NULL),(50,3,'27101fef35e777f9:501268:10157da8a73:-7ff6','2005-01-09 09:29:00',56,'unsleeping',NULL),(51,3,'27101fef35e777f9:501268:10157da8a73:-7ff5','2005-01-09 09:29:00',1,'UnDreaming',NULL),(52,3,'f0ed2a6d7fb07688:b9459c:1015950d40c:-7ffa','2005-01-09 16:26:21',55,'sweet smart',NULL),(53,3,'f0ed2a6d7fb07688:b9459c:1015950d40c:-7ff4','2005-01-09 16:39:02',106,'shed\nFrom sparkling eaves',NULL),(54,3,'27101fef35e777f9:7800e7:1015a24a50d:-7fee','2005-01-09 20:36:19',1,'Dream Land',NULL),(55,4,'27101fef35e777f9:6a21b2:1015a5a69fb:-8000','2005-01-09 21:01:09',0,'The Kraken',NULL),(56,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fff','2005-01-09 21:06:49',1,'The Kraken',NULL),(57,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7ffc','2005-01-09 21:19:49',219,'Huge sponges of millennial growth and height;\nAnd far away into the sickly light,\nFrom many a wondrous grot and secret cell\nUnnumber\'d and enormous polypi\nWinnow with giant arms the slumbering green.\nThere hath he lain for ages and will lie\nBattening upon huge sea-worms in his sleep,',NULL),(58,4,'27101fef35e777f9:6a21b2:1015a5a69fb:-7ffd','2005-01-09 21:19:09',0,'from \"The Octopus\" (1872)',NULL),(59,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7ffb','2005-01-09 21:19:49',1,'from THE OCTOPUS',NULL),(60,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7feb','2005-01-09 21:23:25',1,'from THE OCTOPUS',NULL),(61,2,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fea','2005-01-09 21:23:58',137,'Kraken',NULL),(62,1,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fe4','2005-01-09 21:24:00',131,'Critic',NULL),(63,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fdc','2005-01-09 21:29:43',130,' CriticKraken sleepeth',NULL),(64,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fce','2005-01-09 21:30:28',554,'Indelible ink!',NULL),(65,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fcd','2005-01-09 21:30:28',1,'The Kraken',NULL),(66,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fb7','2005-01-09 21:34:00',15,'AT length their long kiss severed, with sweet smart:\nAnd as the last slow sudden drops are shed\nFrom sparkling eaves when all the storm has fled,',NULL),(67,3,'27101fef35e777f9:6a21b2:1015a5a69fb:-7fb6','2005-01-09 21:34:00',129,'e CriticKraken sleepeth: fain',NULL),(68,4,'b3ef3bc3d121547a:1ee69d3:1015d2d29ff:-8000','2005-01-10 10:10:35',0,'',NULL),(69,1,'464545650b803684:191394e:1015f31e282:-8000','2005-01-10 19:34:59',46,'sadfsafdsadfasfdsfad\nasdflkjasdflkjasdflkj\nadsflkjasdfkljjasdflkjasdflkj',NULL),(70,3,'b1c9c29693f28dda:6cffbb:10168be5432:-7feb','2005-01-12 17:36:26',128,'he CriticKraken sleepeth: fain',NULL),(71,4,'f0ed2a6d7fb07688:12bbe6b:10169d8fe6c:-7fef','2005-01-12 21:23:09',0,'',NULL),(72,1,'f0ed2a6d7fb07688:12bbe6b:10169d8fe6c:-7fee','2005-01-12 21:32:30',108,'granny k',NULL),(73,1,'f0ed2a6d7fb07688:12bbe6b:10169d8fe6c:-7fe6','2005-01-12 21:32:39',205,' acre',NULL),(74,1,'f0ed2a6d7fb07688:12bbe6b:10169d8fe6c:-7fe1','2005-01-12 21:32:58',307,'bed box-',NULL),(75,1,'f0ed2a6d7fb07688:12bbe6b:10169d8fe6c:-7fd9','2005-01-12 21:33:13',423,',',NULL),(76,1,'f0ed2a6d7fb07688:12bbe6b:10169d8fe6c:-7fd8','2005-01-12 21:33:15',430,' off-key,',NULL),(77,1,'f0ed2a6d7fb07688:12bbe6b:10169d8fe6c:-7fbb','2005-01-12 21:34:03',635,' ',NULL),(78,2,'f0ed2a6d7fb07688:12bbe6b:10169d8fe6c:-7fa6','2005-01-12 21:35:13',634,' ',NULL),(79,2,'f0ed2a6d7fb07688:12bbe6b:10169d8fe6c:-7fa4','2005-01-12 21:36:05',93,'Awake',NULL),(80,1,'f0ed2a6d7fb07688:12bbe6b:10169d8fe6c:-7fa3','2005-01-12 21:36:06',98,'Untie',NULL),(81,3,'f0ed2a6d7fb07688:12bbe6b:10169d8fe6c:-7f9e','2005-01-12 21:47:14',98,'Untie her granny knot',NULL),(82,3,'27101fef35e777f9:cfd02:10169e784a6:-7f9f','2005-01-12 22:15:44',372,'And hears the nightingale,\n',NULL),(83,3,'27101fef35e777f9:cfd02:10169e784a6:-7fa8','2005-01-12 22:12:38',129,' single star',NULL),(84,3,'27101fef35e777f9:cfd02:10169e784a6:-7fa0','2005-01-12 22:15:44',130,'single star',NULL),(85,3,'27101fef35e777f9:cfd02:10169e784a6:-7f95','2005-01-12 22:26:56',216,'She left the rosy morn',NULL),(86,3,'27101fef35e777f9:cfd02:10169e784a6:-7f93','2005-01-12 22:29:33',240,'She left the fields of corn',NULL),(87,3,'f0ed2a6d7fb07688:caea19:1016d941d20:-7f84','2005-01-13 14:51:18',240,'She left the fields of corn,',NULL),(88,4,'27101fef35e777f9:71b856:101716ee99c:-8000','2005-01-14 08:34:48',0,'Letter from ACS to CR',NULL),(89,3,'27101fef35e777f9:71b856:101716ee99c:-7ff7','2005-01-14 08:36:15',929,'The Garden of Proserpine',NULL),(90,3,'27101fef35e777f9:71b856:101716ee99c:-7fe7','2005-01-14 08:36:53',1006,'&ldquoDream-Land&rdquo',NULL),(91,4,'27101fef35e777f9:71b856:101716ee99c:-7fe4','2005-01-14 08:38:39',0,'Letter from CR to ACS',NULL),(92,3,'27101fef35e777f9:71b856:101716ee99c:-7fe6','2005-01-14 08:36:53',1,'Dream Land',NULL),(93,3,'27101fef35e777f9:71b856:101716ee99c:-7fd7','2005-01-14 08:40:35',394,'He weaves, and is clothed with derision;\nSows, and he shall not reap;\nHis life is a watch or a vision\nBetween a sleep and a sleep.',NULL),(94,3,'27101fef35e777f9:71b856:101716ee99c:-7fd6','2005-01-14 08:40:35',13,'WHERE sunless rivers weep\nTheir waves into the deep,\nShe sleeps a charmed sleep;\nAwake her not.\n',NULL),(95,3,'27101fef35e777f9:71b856:101716ee99c:-7ff6','2005-01-14 08:36:15',1,'from THE GARDEN OF PROSERPIN',NULL),(96,3,'27101fef35e777f9:71b856:101716ee99c:-7f99','2005-01-14 08:53:15',1115,'a complete poetical mappa mundi.',NULL),(97,3,'27101fef35e777f9:71b856:101716ee99c:-7f97','2005-01-14 08:54:17',1114,' a complete poetical mappa mundi.',NULL),(98,3,'27101fef35e777f9:71b856:101716ee99c:-7f93','2005-01-14 08:55:32',1114,' a complete poetical mappa mundi.',NULL),(99,3,'27101fef35e777f9:71b856:101716ee99c:-7f92','2005-01-14 08:56:08',1114,' a complete poetical mappa mundi.',NULL),(100,3,'27101fef35e777f9:71b856:101716ee99c:-7f8f','2005-01-14 08:56:21',1114,' a complete poetical mappa mundi.',NULL),(101,3,'27101fef35e777f9:71b856:101716ee99c:-7f8c','2005-01-14 08:57:54',1,'Nuptial Slee',NULL),(102,3,'27101fef35e777f9:71b856:101716ee99c:-7f79','2005-01-14 08:58:30',1114,' a complete poetical mappa mundi',NULL),(103,3,'27101fef35e777f9:71b856:101716ee99c:-7f72','2005-01-14 09:00:26',541,'Then once by man and angels to be seen,\nIn roaring he shall rise and on the surface die.',NULL),(104,3,'27101fef35e777f9:71b856:101716ee99c:-7f96','2005-01-14 08:54:17',110,'such as are wholly incommunicable by words',NULL),(105,3,'27101fef35e777f9:71b856:101716ee99c:-7f91','2005-01-14 08:56:08',1,'Nuptial Sleep',NULL),(106,3,'27101fef35e777f9:71b856:101716ee99c:-7f8e','2005-01-14 08:56:21',1,'Nuptial Sleep',NULL),(107,3,'27101fef35e777f9:71b856:101716ee99c:-7f78','2005-01-14 08:58:30',547,'Then once by man and angels to be seen,\nIn roaring he shall rise and on the surface die.',NULL),(108,3,'f0ed2a6d7fb07688:15b6116:1018365dce2:-7f5a','2005-01-17 20:43:22',1115,'a complete poetical mappa mundi',NULL),(109,3,'f0ed2a6d7fb07688:15b6116:1018365dce2:-7f59','2005-01-17 20:43:22',778,'joy shall overtake\nHer perfect peace.',NULL),(110,3,'f0ed2a6d7fb07688:15b6116:1018365dce2:-7f47','2005-01-17 20:46:00',1115,'a complete poetical mappa mundi',NULL),(111,3,'f0ed2a6d7fb07688:15b6116:1018365dce2:-7f46','2005-01-17 20:46:00',778,'joy shall overtake\nHer perfect peace',NULL),(112,3,'f0ed2a6d7fb07688:15b6116:1018365dce2:-7fb1','2005-01-17 20:33:53',495,'\nBetween a sleep and a sleep.',NULL),(113,3,'f0ed2a6d7fb07688:15b6116:1018365dce2:-7f3b','2005-01-17 20:53:24',778,'joy shall overtake\nHer perfect peace.',NULL),(114,3,'b3ef3bc3d121547a:1ce9f9d:1018615c68e:-8000','2005-01-18 08:49:29',1,'this is a test',NULL),(115,4,'ae4829f08456d081:be0018:1018b51aa29:-8000','2005-01-19 09:12:18',0,'Measure Me',NULL),(116,4,'aeef85f31dca93ba:cc8a48:1018b86ca88:-8000','2005-01-19 10:10:50',0,'',NULL),(117,1,'ae4829f08456d081:be0018:1018b51aa29:-7fff','2005-01-19 10:38:56',90,'\nTemperament for the most part nervous. Brain large; the anterior and superior parts remarkably salient. In her domestic relations this lady will be warm and affectionate. In the care of children she will evince judicious kindness, but she is not pleased at seeing them spoiled by over-indulgence. Her fondness for any particular locality would chiefly rest upon the associations connected with it. Her attachments are strong and enduring; indeed, this is a leading element of her character. She is rather circumspect, however, in the choice of her friends, and it is well that she is so, for she will seldom meet with persons whose dispositions approach the standard of excellence with which she can entirely sympathise. Her sense of truth and justice would be offended by any dereliction of duty, and she would in such cases express her disapprobation with warmth and energy. She would not, however, be precipitate in acting thus, and rather than live in a state of hostility with those she could wish to love she would depart from them, although the breaking off of friendship would be to her a source of great unhappiness. The careless and unreflecting whom she would labour to amend might deem her punctilious and perhaps exacting, not considering that their amendment and not her own gratification prompted her to admonish. She is sensitive, and is very anxious to succeed in her undertakings, but is not so sanguine as to the probability of success. She is occasionally inclined to take a gloomier view of things than perhaps the facts of the case justify. She should guard against the effect of this where her affection is engaged, for her sense of her own impatience is moderate and not strong enough to steel her against disappointment. She has more firmness than self-reliance, and her sense of justice is of a very high order. She is deferential to the aged and those she deems worthy of her respect, and possesses much devotional feeling, but dislikes fanaticism, and is not given to a belief in supernatural things without questioning the probability of their existence.\n\nMoney is not her idol; she values it merely for its uses. She would be liberal to the poor and compassionate to the afflicted, and when friendship calls for aid she would struggle even against her own interest to impart the required assistance; indeed, sympathy is a marked characteristic of this organisation.\n\nIs fond of symmetry and proportion, and possesses a good perception of form, and is a good judge of colour. She is endowed with a keen perception of melody and rhythm. Her imitative powers are good, and the faculty which gives small dexterity is well developed. These powers might have been cultivated with advantage. Is a fair calculator, and her sense of order and arrangement is remarkably good. Whatever this lady has to settle or arrange will be done with precision and taste.\n\nShe is endowed with an exalted sense of the beautiful and ideal, and longs for perfection. If not a poet her sentiments are poetical, or at least imbued with that enthusiastic grace which is characteristic of poetical feeling. She is fond of dramatic literature and the drama, especially if it be combined with music.\n\nIn its intellectual development this head is very remarkable. The forehead is at once very large and well formed. It bears the stamp of deep thoughtfulness and comprehensive understanding. It is highly philosophical. It exhibits the resence of an intellect at once perspicacious and perspicuous. There is much critical sagacity and fertility in devising resources in situations of difficulty; much originality, with a tendency to speculate and generalise. Possibly this speculative bias may sometimes interfere with the practical efficiency of some of her projects. Yet, since she has scarcely an adequate share of self-reliance, and is not sanguine as to the success of her plans, there is reason to suppose that she would attend more closely to particulars, and thereby prevent the unsatisfactory results of hasty generalisation. The lady possesses a fine organ of language, and can, if she has done her talents justice by exercise, express her sentiments with clearness, precision, and force--sufficiently eloquent but not verbose. In learning a language she would investigate its spirit and structure. The character of the German language would be well adapted to such an organisation. In analysing the motives of human conduct this lady would display originality and power, but in her mode of investigating mental science she would naturally be imbued with a metaphysical bias. She would perhaps be sceptical as to the truth of Gale\'s doctrine; but the study of this doctrine, this new system of mental philosophy, would give additional strength to her excellent understanding by rendering it more practical, more attentive to particulars, and contribute to her happiness by imparting to her more correct notions of the dispositions of those whose acquaintance she may wish to cultivate.\n\n     J. P. Browne, M.D.\n     367 Strand:\n     June 29, 1851.',NULL),(118,1,'ae4829f08456d081:be0018:1018b51aa29:-7feb','2005-01-19 10:39:58',13,':\n    A Phrenological Estimate of the Talents and Dispositions of a Lady.',NULL),(119,1,'ae4829f08456d081:be0018:1018b51aa29:-7fd5','2005-01-19 10:41:43',7,'\n\"Giants exist as a state of mind. They are defined not as an absolute measurement but as a proportionality. . . . So giants can be real, even if adults do not choose to classify them as such.\"\n-- sociobiologist E. O. Wilson\n',NULL),(120,5,'ae4829f08456d081:be0018:1018b51aa29:-7fd4','2005-01-19 10:42:49',232,'fig2.gif',NULL),(121,1,'1b1e034b0478ef15:9ca1fb:1019195db46:-8000','2005-01-20 14:25:12',15,'.  more testing',NULL),(122,4,'1b1e034b0478ef15:1945a5a:101919c5250:-8000','2005-01-20 14:32:13',0,'',NULL),(123,4,'1b1e034b0478ef15:1945a5a:101919c5250:-7fff','2005-01-20 14:44:18',0,'',NULL),(124,4,'aeef85f31dca93ba:e14ebc:10195dd7281:-8000','2005-01-21 10:21:52',0,'',NULL),(125,3,'aeef85f31dca93ba:1747e0f:101960efae3:-8000','2005-01-21 11:15:59',8,'\"Giants exist as a state of mind. They are defined not as an absolute measurement but as a proportionality. . . . So giants can be real, even if adults do not choose to classify them as such.\"\n-- sociobiologist E. O. Wilson\n \n',NULL),(126,2,'ae4829f08456d081:9c94c1:10196fd86d4:-7ffc','2005-01-21 16:00:57',350,'---Isaac Babel, \"Bagrat-Ogly and the Eyes of His Bull\"',NULL),(127,1,'ae4829f08456d081:9c94c1:10196fd86d4:-7ffb','2005-01-21 16:00:57',264,'No iron can stab the heart with such force as a full stop put just at the right place.',NULL),(128,2,'ae4829f08456d081:9c94c1:10196fd86d4:-7ff8','2005-01-21 16:02:17',1,'I don\'t have the quotation with me now, but will when I get home.',NULL),(129,2,'ae4829f08456d081:9c94c1:10196fd86d4:-7ff7','2005-01-21 16:02:25',1,'&#65279;Clifton Snider\nEnglish Department\nCalifornia State University, Long Beach\n\n\"On the Loom of Sorrow\": Eros and Logos in Oscar\nWilde\'s Fairy Tales\nI When a young child listens to a fairy tale, he or she listens with what Owen Barfield calls\n\"original participation,\" a termBarfield derives from Lucien Lévy-Bruhl\'s concept of\n\"participation mystique\" (Barfield 30-31 and 40-45), a concept alsoadopted by Jung and others.\nLike aboriginal peoples, young children perceive differently from older children and adults\nwhoseegos have been differentiated: \"in the act of perception, they are not detached, as we are,\nfrom the representations\" (Barfield31). What is perceived is of the \"same nature\" as the\nperceiver (Barfield 42). In other words, ego consciousness has not yetbeen fully developed for\nthe original participator. As Erich Neumann puts it, \"in every individual life,\nconsciousnessre-experiences&#65279;Clifton Snider\nEnglish Department\nCalifornia State University, Long Beach\n\n\"On the Loom of Sorrow\": Eros and Logos in Oscar\nWilde\'s Fairy Tales\nI When a young child listens to a fairy tale, he or she listens with what Owen Barfield calls\n\"original participation,\" a termBarfield derives from Lucien Lévy-Bruhl\'s concept of\n\"participation mystique\" (Barfield 30-31 and 40-45), a concept alsoadopted by Jung and others.\nLike aboriginal peoples, young children perceive differently from older children and adults\nwhoseegos have been differentiated: \"in the act of perception, they are not detached, as we are,\nfrom the representations\" (Barfield31). What is perceived is of the \"same nature\" as the\nperceiver (Barfield 42). In other words, ego consciousness has not yetbeen fully developed for\nthe original participator. As Erich Neumann puts it, \"in every individual life,\nconsciousnessre-experiences',NULL),(130,3,'ae4829f08456d081:9c94c1:10196fd86d4:-7ff6','2005-01-21 16:03:16',262,'.',NULL),(131,3,'aeef85f31dca93ba:1031310:1019b0ba3b3:-7ffd','2005-01-22 10:34:29',2,' saw a bull of unparalleled beauty lying by the side of the road.\nA boy was bending over it, crying.\n\"This boy is Bagrat-Ogly,\" said a snake charmer who was eating his scanty meal nearby. \"Bagrat-Ogly, son of Kazim.\"\nHe is as exquisite as twelve moons,\" I said.',NULL),(132,1,'ae4829f08456d081:e9ebe1:101a573ed60:-8000','2005-01-24 11:00:33',1,'A work of art is the one mystery, the one extreme magic; everything else is either arithmetic or biology.\n',NULL),(133,3,'ae4829f08456d081:e9ebe1:101a573ed60:-7ff1','2005-01-24 11:01:54',17,'unparalleled',NULL),(134,1,'ae4829f08456d081:e9ebe1:101a573ed60:-7faa','2005-01-24 11:22:28',209,'\"A work of art is the one mystery, the one extreme magic; everything else is either arithmetic or biology!\"',NULL),(135,2,'ae4829f08456d081:e9ebe1:101a573ed60:-7fa9','2005-01-24 11:22:35',208,'.',NULL),(136,1,'ae4829f08456d081:e9ebe1:101a573ed60:-7fa8','2005-01-24 11:22:36',207,',',NULL),(137,3,'ae4829f08456d081:e9ebe1:101a573ed60:-7f74','2005-01-24 11:50:31',95,'crying',NULL),(138,2,'ae4829f08456d081:e9ebe1:101a573ed60:-7f85','2005-01-24 11:48:27',400,'A work of art is the one mystery, the one extreme magic; everything else is either arithmetic or biology',NULL),(139,1,'ae4829f08456d081:e9ebe1:101a573ed60:-7f84','2005-01-24 11:48:27',210,'\"Giants exist as a state of mind. They are defined not as an absolute measurement but as a proportionality. . . . So giants can be real, even if adults do not choose to classify them as such',NULL),(140,3,'ae4829f08456d081:e9ebe1:101a573ed60:-7f83','2005-01-24 11:48:27',400,'A work of art is',NULL),(141,2,'ae4829f08456d081:e9ebe1:101a573ed60:-7f7f','2005-01-24 11:48:41',209,'\"',NULL),(142,4,'a0f04a90f70ac256:b0f6ef:101a63bd78f:-8000','2005-01-24 14:38:54',0,'something to ignore',NULL),(143,1,'a0f04a90f70ac256:b0f6ef:101a63bd78f:-7fff','2005-01-24 14:39:23',106,' (is it really him?)',NULL),(144,2,'a0f04a90f70ac256:b0f6ef:101a63bd78f:-7feb','2005-01-24 14:39:32',55,'.',NULL),(145,1,'a0f04a90f70ac256:b0f6ef:101a63bd78f:-7fea','2005-01-24 14:39:40',56,' for Dino and Maria.',NULL),(146,3,'a0f04a90f70ac256:b0f6ef:101a63bd78f:-7fca','2005-01-24 14:40:25',1,'Jessica',NULL),(147,2,'a0f04a90f70ac256:b0f6ef:101a63bd78f:-7fc8','2005-01-24 14:40:53',29,'document',NULL),(148,4,'11d1def534ea1be0:3c86e9:101afb49a19:-7ffa','2005-01-26 10:50:23',0,'not a real jpeg',NULL),(149,4,'11d1def534ea1be0:3c86e9:101afb49a19:-7ffb','2005-01-26 10:49:45',0,'It\'s a Trap!',NULL),(150,1,'11d1def534ea1be0:3c86e9:101afb49a19:-7ff9','2005-01-26 10:50:55',15,'\nHere is some text that also gets the formatting of the above text.\nWhile i do like sans-serif fonts for screen reading, the centered text is a bit much.',NULL),(151,1,'11d1def534ea1be0:3c86e9:101afb49a19:-7f44','2005-01-26 10:51:42',172,'In contrast, this is the font and typeset properties that additions should have.',NULL),(152,2,'11d1def534ea1be0:3c86e9:101afb49a19:-7ef2','2005-01-26 10:52:02',171,' ',NULL),(153,2,'11d1def534ea1be0:3c86e9:101afb49a19:-7ee5','2005-01-26 10:52:09',168,'\n',NULL),(154,3,'11d1def534ea1be0:3c86e9:101afb49a19:-7ede','2005-01-26 10:52:57',250,'e.',NULL),(155,4,'37081a7bcc479e68:c1dfa9:101b0d39cf9:-8000','2005-01-26 16:00:28',0,'Hills are empty',NULL),(156,2,'37081a7bcc479e68:c1dfa9:101b0d39cf9:-7ffb','2005-01-26 16:06:46',0,'\nEmpty hill not see person Hills are empty, no man is seen,',NULL),(157,1,'37081a7bcc479e68:c1dfa9:101b0d39cf9:-7ff9','2005-01-26 16:07:02',1,'Empty hill not see person ',NULL),(158,3,'f5176ab005432bb2:e8e8f9:101b0d903af:-8000','2005-01-26 16:06:47',11,'empty',NULL),(159,3,'f5176ab005432bb2:e8e8f9:101b0d903af:-7ffe','2005-01-26 16:08:34',100,'deep forest',NULL),(160,3,'f5176ab005432bb2:e8e8f9:101b0d903af:-7ffc','2005-01-26 16:09:30',38,'the sound of people\'s voices',NULL),(161,4,'f5176ab005432bb2:119087d:101b0e9f8df:-8000','2005-01-26 16:24:57',0,'',NULL),(162,3,'f5176ab005432bb2:119087d:101b0e9f8df:-7fff','2005-01-26 16:29:31',26,' Hills are empty, no man is seen',NULL),(163,1,'37081a7bcc479e68:c1dfa9:101b0d39cf9:-7fe8','2005-01-26 16:31:41',2,'Can I add? Yes, I can',NULL),(164,3,'37081a7bcc479e68:c1dfa9:101b0d39cf9:-7fad','2005-01-26 16:33:41',27,'Hills are empty, no man is seen,',NULL),(165,3,'37081a7bcc479e68:c1dfa9:101b0d39cf9:-7fa2','2005-01-26 16:38:46',1,' Can I add? Yes, I can',NULL),(166,3,'37081a7bcc479e68:c1dfa9:101b0d39cf9:-7fa1','2005-01-26 16:38:46',86,'voices ',NULL),(167,3,'f5176ab005432bb2:119087d:101b0e9f8df:-7ffa','2005-01-26 16:37:35',1,'Empty hill not see person',NULL);
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
INSERT INTO `action_document` VALUES (1,7,2,'2005-01-07 14:52:22'),(2,7,2,'2005-01-07 14:52:22'),(3,7,2,'2005-01-07 14:52:22'),(4,7,2,'2005-01-07 14:52:22'),(5,7,2,'2005-01-07 14:52:22'),(6,7,2,'2005-01-07 14:52:22'),(7,7,2,'2005-01-07 14:52:22'),(8,7,2,'2005-01-07 14:52:22'),(9,7,2,'2005-01-07 14:52:22'),(10,7,2,'2005-01-07 14:52:22'),(11,7,2,'2005-01-07 14:52:22'),(12,7,2,'2005-01-07 14:52:22'),(13,7,2,'2005-01-07 14:52:22'),(14,7,2,'2005-01-07 14:52:22'),(15,7,2,'2005-01-07 14:52:22'),(16,7,2,'2005-01-07 14:52:22'),(17,7,2,'2005-01-07 15:00:46'),(18,8,6,'2005-01-07 16:13:59'),(19,9,2,'2005-01-08 11:33:06'),(20,9,2,'2005-01-08 11:33:06'),(21,8,2,'2005-01-07 16:13:59'),(22,8,2,'2005-01-07 16:13:59'),(23,10,2,'2005-01-08 11:42:46'),(24,7,2,'2005-01-07 15:00:46'),(25,10,2,'2005-01-08 11:42:46'),(26,10,2,'2005-01-08 11:42:46'),(27,8,2,'2005-01-08 11:37:40'),(28,10,2,'2005-01-08 11:42:46'),(29,10,2,'2005-01-08 11:42:46'),(30,11,8,'2005-01-08 12:10:00'),(31,7,2,'2005-01-08 11:57:07'),(32,7,2,'2005-01-08 11:57:07'),(33,7,8,'2005-01-08 12:15:25'),(34,7,8,'2005-01-08 12:15:25'),(35,7,8,'2005-01-08 12:15:25'),(36,7,8,'2005-01-08 12:15:25'),(37,7,8,'2005-01-08 12:15:25'),(38,7,8,'2005-01-08 12:15:25'),(39,7,8,'2005-01-08 12:15:25'),(40,7,8,'2005-01-08 12:15:25'),(41,7,8,'2005-01-08 12:15:25'),(42,7,8,'2005-01-08 12:15:25'),(43,7,8,'2005-01-08 12:15:25'),(44,7,8,'2005-01-08 12:15:25'),(45,7,8,'2005-01-08 12:15:25'),(46,7,8,'2005-01-08 12:15:25'),(47,7,8,'2005-01-08 12:15:25'),(48,12,8,'2005-01-09 09:33:15'),(49,12,8,'2005-01-09 09:33:15'),(50,11,8,'2005-01-08 12:15:25'),(51,8,8,'2005-01-07 16:13:59'),(52,9,12,'2005-01-08 11:33:06'),(53,9,12,'2005-01-09 16:28:06'),(54,7,2,'2005-01-08 11:57:07'),(55,13,2,'2005-01-09 21:01:09'),(56,13,2,'2005-01-09 21:01:09'),(57,13,8,'2005-01-09 21:01:09'),(58,14,8,'2005-01-09 21:19:09'),(59,14,8,'2005-01-09 21:19:09'),(60,14,8,'2005-01-09 21:19:09'),(61,13,8,'2005-01-09 21:01:09'),(62,13,8,'2005-01-09 21:01:09'),(63,13,8,'2005-01-09 21:01:09'),(64,14,8,'2005-01-09 21:19:09'),(65,13,8,'2005-01-09 21:01:09'),(66,9,12,'2005-01-09 16:43:35'),(67,13,8,'2005-01-09 21:31:59'),(68,15,15,'2005-01-10 10:10:35'),(69,10,18,'2005-01-08 11:42:46'),(70,13,8,'2005-01-09 21:38:58'),(71,16,11,'2005-01-12 21:23:09'),(72,7,11,'2005-01-07 14:52:22'),(73,7,11,'2005-01-07 14:52:22'),(74,7,11,'2005-01-07 14:52:22'),(75,7,11,'2005-01-07 14:52:22'),(76,7,11,'2005-01-07 14:52:22'),(77,7,11,'2005-01-07 14:52:22'),(78,7,11,'2005-01-07 14:52:22'),(79,7,11,'2005-01-07 14:52:22'),(80,7,11,'2005-01-07 14:52:22'),(81,7,11,'2005-01-12 21:38:48'),(82,7,2,'2005-01-08 11:57:07'),(83,7,11,'2005-01-12 21:38:48'),(84,7,11,'2005-01-12 21:38:48'),(85,7,11,'2005-01-12 21:38:48'),(86,7,11,'2005-01-12 21:38:48'),(87,7,11,'2005-01-12 21:38:48'),(88,17,2,'2005-01-14 08:34:48'),(89,17,2,'2005-01-14 08:34:48'),(90,17,2,'2005-01-14 08:34:48'),(91,18,2,'2005-01-14 08:38:39'),(92,7,2,'2005-01-12 22:31:28'),(93,18,2,'2005-01-14 08:38:39'),(94,7,2,'2005-01-12 22:31:28'),(95,10,2,'2005-01-08 11:57:07'),(96,17,2,'2005-01-14 08:42:19'),(97,17,2,'2005-01-14 08:42:19'),(98,17,2,'2005-01-14 08:42:19'),(99,17,2,'2005-01-14 08:42:19'),(100,17,2,'2005-01-14 08:42:19'),(101,9,2,'2005-01-08 11:37:40'),(102,17,2,'2005-01-14 08:42:19'),(103,13,2,'2005-01-09 21:16:40'),(104,16,8,'2005-01-12 21:23:09'),(105,9,8,'2005-01-09 21:38:58'),(106,9,8,'2005-01-09 21:38:58'),(107,13,8,'2005-01-09 21:38:58'),(108,17,2,'2005-01-14 08:42:19'),(109,7,12,'2005-01-12 21:48:38'),(110,17,2,'2005-01-14 08:42:19'),(111,7,12,'2005-01-12 21:48:38'),(112,18,2,'2005-01-14 08:42:19'),(113,7,12,'2005-01-12 21:48:38'),(114,15,15,'2005-01-10 10:12:09'),(115,19,20,'2005-01-19 09:12:18'),(116,20,22,'2005-01-19 10:10:50'),(117,19,20,'2005-01-19 09:21:31'),(118,19,20,'2005-01-19 09:21:31'),(119,20,20,'2005-01-19 10:10:50'),(120,20,20,'2005-01-19 10:10:50'),(121,15,23,'2005-01-10 10:10:35'),(122,21,22,'2005-01-20 14:32:13'),(123,22,22,'2005-01-20 14:44:18'),(124,23,22,'2005-01-21 10:21:52'),(125,20,20,'2005-01-19 11:00:57'),(126,23,20,'2005-01-21 10:21:52'),(127,23,20,'2005-01-21 10:21:52'),(128,21,20,'2005-01-20 14:32:13'),(129,22,20,'2005-01-20 14:44:18'),(130,23,22,'2005-01-21 10:22:40'),(131,23,22,'2005-01-21 10:22:40'),(132,23,20,'2005-01-21 16:19:40'),(133,23,22,'2005-01-22 10:35:11'),(134,23,20,'2005-01-24 11:12:09'),(135,23,20,'2005-01-24 11:12:09'),(136,23,20,'2005-01-24 11:12:09'),(137,23,22,'2005-01-22 10:35:11'),(138,23,20,'2005-01-24 11:24:16'),(139,23,20,'2005-01-24 11:24:16'),(140,23,20,'2005-01-24 11:24:16'),(141,23,20,'2005-01-24 11:24:16'),(142,24,27,'2005-01-24 14:38:54'),(143,24,27,'2005-01-24 14:38:54'),(144,24,27,'2005-01-24 14:38:54'),(145,24,27,'2005-01-24 14:38:54'),(146,24,27,'2005-01-24 14:38:54'),(147,24,27,'2005-01-24 14:38:54'),(148,26,29,'2005-01-26 10:50:23'),(149,25,29,'2005-01-26 10:49:45'),(150,25,29,'2005-01-26 10:49:45'),(151,25,29,'2005-01-26 10:49:45'),(152,25,29,'2005-01-26 10:49:45'),(153,25,29,'2005-01-26 10:49:45'),(154,25,29,'2005-01-26 10:49:45'),(155,27,33,'2005-01-26 16:00:28'),(156,27,33,'2005-01-26 16:03:00'),(157,27,33,'2005-01-26 16:03:00'),(158,27,32,'2005-01-26 16:00:28'),(159,27,32,'2005-01-26 16:00:28'),(160,27,32,'2005-01-26 16:00:28'),(161,28,32,'2005-01-26 16:24:57'),(162,27,33,'2005-01-26 16:09:07'),(163,28,33,'2005-01-26 16:24:57'),(164,27,33,'2005-01-26 16:09:07'),(165,28,33,'2005-01-26 16:34:13'),(166,27,33,'2005-01-26 16:34:13'),(167,27,33,'2005-01-26 16:09:07');
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
INSERT INTO `bookmarks` VALUES (1,7,'The Brontes and Phrenology','http://faculty.plattsburgh.edu/peter.friesen/default.asp?go=217','includes a phrenological assessment of Charlotte Bronte and the full text of Combe\'s Elements of Phrenology'),(2,7,'Edward Osborne Wilson','http://en.wikipedia.org/wiki/Edward_O._Wilson','Wikipedia entry on E. O. Wilson');
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
INSERT INTO `db_history` VALUES ('2004-12-02 10:41:59','removed document.publication_date, added db_histor','1.1'),('2004-12-02 10:42:44','convert player to role id','1.1'),('2004-12-02 10:43:32','convert link tag string to field','1.1'),('2005-01-05 15:18:51','added the bookmarks table','1.2'),('2005-01-05 15:19:45','convert discussion data from player id to role id','1.2'),('2005-01-12 10:26:48','added summary to bookmarks table','1.3'),('2005-01-28 17:31:32','added move_inspiration and category tables and tit','1.4'),('2005-04-11 11:53:35','Added \'archived\' field to game table','1.5'),('2005-04-11 11:53:35','Added \'private\' field to game table','1.5'),('2005-04-11 11:53:35','Added \'write_permission\' field to role table','1.5'),('2005-04-11 11:53:35','Added \'new_game_permission\' field to player table','1.5'),('2005-04-11 11:53:35','Added \'new_role_permission\' field to player table','1.5'),('2005-04-11 11:53:35','Added \'write_permission\' field to player table','1.5'),('2005-04-11 11:53:35','Added \'admin\' field to player table','1.5');
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
INSERT INTO `discourse_field` VALUES (6,7,0,1),(6,8,0,1),(6,9,0,1),(6,10,0,1),(6,11,0,1),(6,12,0,1),(6,13,0,1),(6,14,0,1),(5,15,0,1),(6,16,0,1),(6,17,0,1),(6,18,0,1),(7,19,0,1),(7,20,0,1),(7,21,0,1),(7,22,0,1),(7,23,0,1),(7,24,0,1),(5,25,0,1),(5,26,0,1),(8,27,0,1),(8,28,0,1);
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
INSERT INTO `discussion` VALUES (1,20,7,'augurs','<html>\n  <head>\n\n  </head>\n  <body>\n    <p>\n      Inaugurals being much in the news, this message comes only to inaugurate \n      the discussion forum -- and to say that, while I continue to pizzle (um, \n      *puzzle*) over the riddle embedded in your first move, that doesn\'t stop \n      my semi-planned silliness.  See visual rhyme in my Move #2.\n    </p>\n  </body>\n</html>\n','2005-01-19 11:06:19',-1),(2,22,7,'RE: augurs','<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <p>\r\n      I do enjoy the visual pun.  It seems to me that bull fighting and \r\n      craniometry share an activity of measurement, but that bull-fighting \r\n      (not an activity I admire) measures on a cosmic scale.  And one may read \r\n      a text by measuring it or by killing it, but how do we wish to read the \r\n      text of Babel\'s story.  Would  you like to hear more of it?  A summary \r\n      perhaps?  By the way, is such a verbose answer out of keeping with the \r\n      style of this game as I have seen it played in the archive--short, \r\n      zen-like statements? \r\n    </p>\r\n  </body>\r\n</html>\r\n','2005-01-21 10:36:06',1),(3,20,7,'RE: augurs','<html>\n  <head>\n    \n  </head>\n  <body>\n    <p>\n      I\'d love to read or hear more about Babel and this particular story -- \n      so please don\'t take my &quot;full stop&quot; move as a shutting-down act.  It was \n      more of an acted-upon impulse. \n    </p>\n    <p>\n      Also: no, a prosy answer is not at all out of place here.  (I think \n      there\'s something about this version of the Ivanhoe interface -- or \n      about the realtime, mediated illusion in general -- that places a \n      premium on the soundbite.  But clearly that\'s not a desired effect.) \n    </p>\n    <p>\n      I\'m happily computerless on the weekends, so won\'t be making any more \n      moves until Monday.  See you then, obliquely.\n    </p>\n  </body>\n</html>\n','2005-01-21 16:45:11',2);
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
INSERT INTO `document` VALUES (7,'Dream Land.html','Dream Land','Christina Rossetti','*deprecate*','The Germ (1850)',815,'2005-01-07 14:52:22',2),(8,'SleepersAwake.html','Sleepers Awake','Goblina','*deprecate*','her soul, fed by the wellspring of unseen forces',879,'2005-01-07 16:13:59',6),(9,'NuptialSleep.html','Nuptial Sleep','D. G. Rossetti','*deprecate*','Poems (1870)',681,'2005-01-08 11:33:06',2),(10,'TheGardenofProserpineexcerpt.html','The Garden of Proserpine (excerpt)','A. C. Swinburne','*deprecate*','Poems and Ballads (1866)',752,'2005-01-08 11:42:46',2),(11,'fromAveatqueVale.html','from \"Ave atque Vale\"','A. C. Swinburne','*deprecate*','Poems and Ballads, Second Series (1878)',962,'2005-01-08 12:10:00',8),(12,'Insomnia.html','Insomnia','D. G. Rossetti','*deprecate*','Ballads and Sonnets (1881)',852,'2005-01-09 09:33:15',8),(13,'TheKraken.html','The Kraken','Alfred Tennyson','*deprecate*','Poems, Chiefly Lyrical (1830)',629,'2005-01-09 21:01:09',2),(14,'fromTheOctopus1872.html','from \"The Octopus\" (1872)','Arthur Clement Hilton','*deprecate*','RPO',568,'2005-01-09 21:19:09',8),(15,'test1.html','test 1','tester','*deprecate*','',15,'2005-01-10 10:10:35',15),(16,'ThePainsofOpium.html','The Pains of Opium','Thomas De Quincey','*deprecate*','Norton Anthology of Course, 6th edn 2:452',593,'2005-01-12 21:23:09',11),(17,'LetterfromACStoCR.html','Letter from ACS to CR','ACS','*deprecate*','Unpublished Correspondence',1187,'2005-01-14 08:34:48',2),(18,'LetterfromCRtoACS.html','Letter from CR to ACS','CR','*deprecate*','Unpublished Correspondence',668,'2005-01-14 08:38:39',2),(19,'fig3.html','Measure Me','George Combe, Late President of the Phrenological Society','*deprecate*','Elements of Phrenology, 2nd American Edn. 1834',17,'2005-01-19 09:12:18',20),(20,'bull.html','bull','Unknown','*deprecate*','Unknown',11,'2005-01-19 10:10:50',22),(21,'Isaac.html','Isaac','Babel','*deprecate*','',66,'2005-01-20 14:32:13',22),(22,'wilde.html','wilde','some professor somewhere','*deprecate*','',1813,'2005-01-20 14:44:18',22),(23,'BagratOgly.html','Bagrat-Ogly','Isaac Babel','*deprecate*','',318,'2005-01-21 10:21:52',22),(24,'somethingtoignore.html','something to ignore','a group of concerned persons','*deprecate*','IATH round table',86,'2005-01-24 14:38:54',27),(25,'akbar1.html','It\'s a Trap!','','*deprecate*','teh intarweb',19,'2005-01-26 10:49:45',29),(26,'bad_image_file.html','not a real jpeg','1337 hax0r','*deprecate*','cat > bad_image_file.jpg',22,'2005-01-26 10:50:23',29),(27,'Hills are empty.html','Hills are empty','Wang Wei','*deprecate*','www',144,'2005-01-26 16:00:28',33),(28,'clock.html','clock','John Cayley','*deprecate*','web',12,'2005-01-26 16:24:57',32);
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
INSERT INTO `document_image` VALUES (19,'fig3.gif'),(20,'bull.jpg'),(20,'fig2.gif'),(25,'akbar1.jpg'),(26,'bad_image_file.jpg'),(28,'clock.gif');
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
INSERT INTO `game` VALUES (5,20,'silly test','this is just a system test','',0,0,0,0,1),(6,41,'Dream Land','This game will be played with Christina Rossetti\'s lyric poem \"Dream Land\".','Usual IVANHOE rules.',0,0,0,0,1),(7,38,'No Brainer','test game for Jessica Feldman','to try out Ivanhoe interface and higher-level concepts',0,1,0,0,1),(8,32,'Dino & Maria','Pleasure and instruction','fun & games',0,0,0,0,1);
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
INSERT INTO `keyspace` VALUES ('game',9),('action',168),('link_target',1),('move',51),('document',29),('discussion',4),('player',101),('role',35),('bookmarks',3),('category',1);
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
INSERT INTO `link_target` VALUES (16,4,0,0,0,'2005-01-07 14:56:49','','Textual Issues','Some fool put in a text of this poem and pretended it was The Germ text.  This text is simply something LIKE The Germ text.'),(17,4,0,0,0,'2005-01-07 15:31:05','','on sleeping women','I wish that my women would sleep more and speak less.'),(20,1,2,8,0,'2005-01-08 11:37:40','27101fef35e777f9:a1c1f5:1015329b845:-7ff3','To expose the intert',NULL),(21,4,0,0,0,'2005-01-08 11:30:38','','Intertext','Does Goblina mean us to read this poem in relation to Miss Rossetti\'s \"Dream Land\"?  It\'s intertext is surely something else altogether -- surely Miss Rossetti\'s brother\'s immortal sonnet \"Nuptial Sleep\".'),(22,1,2,9,0,'2005-01-08 11:37:40','27101fef35e777f9:a1c1f5:1015329b845:-7ff2','To expose the intert',NULL),(24,4,0,0,0,'2005-01-08 11:46:35','','Explication','\"Where\" is this poet pointing us to?  Surely to \"The purple land\" that is this poem she is writing.  This is the only land for which we care because it is the only land from which we can make relaible judgments about the world of illusion, \"reality\", and its \"dreams of life\" from which we are missioned, as poets, to awake all of the living dead.'),(25,4,0,0,0,'2005-01-08 11:48:12','','Explication','If a poet may be permitted to gloss his own work, this text opens with a word that signals to its readers \"where\" we are now living -- here in this verse.'),(26,1,2,8,0,'2005-01-08 11:37:40','27101fef35e777f9:a1c1f5:1015329b845:-7fba','',NULL),(27,1,2,10,0,'2005-01-08 11:57:07','27101fef35e777f9:a1c1f5:1015329b845:-7fbb','',NULL),(28,4,0,0,0,'2005-01-08 11:51:48','','Goblina\'s precursors','The reader of this text from \"a later day\" will want to negotiate the poem through its precessional verses, as for instance we have been observing.'),(29,4,0,0,0,'2005-01-08 11:55:03','','Intertexts again','See lines 9-12 of Miss Rossetti\'s \"Dream Land\".'),(31,1,8,11,0,'2005-01-08 12:15:25',NULL,'To draw the CONTRAST',NULL),(32,4,0,0,0,'2005-01-08 12:13:30','','Naive commentary','While one hestitates to introduce a reading that would appear to contradict, or at least seriously qualify, the commentary of a genius like Mr. Swinburne, nonetheless these lines seem to have little in common with Mr. Swinburne\'s attitudes toward sleep, death, and poetry as set forth in \"Ave atque Vale\", especially stands 4-5.'),(33,4,0,0,0,'2005-01-08 12:22:01','','The Text','While Mr. Swinburne was entirely correct to revise this text, which is certainly disgraceful, he neglects to observe all of the problems raised by the material.  I here accept (and incorporate) his corrections and will soon point out the other matters that seem pertinent to the issue,'),(49,4,0,0,0,'2005-01-09 09:35:14','','Thematic Query','It strikes me that CR and ACS both have their theories and aesthetics of sleeping and dreaming, but that DGR\'s main preoccupation is with insomniac wakefulness, as if his dreams were waking dreams (and nightmares).'),(50,1,8,8,0,'2005-01-07 16:13:59','27101fef35e777f9:501268:10157da8a73:-7ff5','Is Goblina\'s Undream',NULL),(51,1,8,11,0,'2005-01-09 09:38:45','27101fef35e777f9:501268:10157da8a73:-7ff6','Is Goblina\'s Undream',NULL),(52,4,0,0,0,'2005-01-09 16:26:21','','street smarts?','\"sweet smart\".  Adj + noun, or noun + adj?  Pleasant pain, that is, or intelligent pleasure?  I suspect a typesetter\'s error for our commodity-hip poet\'s likelier reading, \"sweets mart,\" i. e. the candy store to which Yeats patronizingly relegated the po-boy Keats who was Rossetti\'s great original.'),(53,4,0,0,0,'2005-01-09 16:39:02','','the shed of life','The shedding of \"drops\" (rain) joins up with the later \"tides\" and \"streams\" to form a complete if twisted hydrological cycle.  Vide Strangelove on our Precious Bodily Fluids.  But this is child\'s play to the architectural motif that starts up when the \"shed\" acquires \"eaves\" as by bricolage from Coleridge\'s \"Frost @ Midnight\".  The image lumbers on into shipwreck at mid-sonnet, then fresh forestation of \"new woods\" assures us there\'s plenty more where that came from.'),(54,4,0,0,0,'2005-01-09 20:36:19','','Whose poem?','Everyone assumes this poem is by CR.  But are we perhaps dealing with a hoax?  A plagiarism?  I see the poem is attributed to one \"Ellen Allyn\" in a table of contents secreted away at the end of the second issue of The Germ (the T of C in the first issue gives no authorship at all).  \"Ellen Allyn\"?  PLEASE!  (Or why not Bonny Barbara Allen? -- since we\'re clearly at games with words!)  CR published a version of the text in 1862 as her own, -- very close to this 1850 text.  Is that a hoax too?  And what hangeth upon the name?'),(56,4,0,0,0,'2005-01-09 21:06:49','','Pre-Christian dreams','An interesting apocalyptic vision from the laureate\'s pre-laureate days, when he still had a sense of humour.  And before my poor friend Rossetti plunged into his daylight nightmares and insomniac terrors, he was one of the few who understood the comic potential of Alfred\'s early squib (see \"MacCraken\").'),(57,1,8,14,0,'2005-01-09 21:31:59','27101fef35e777f9:6a21b2:1015a5a69fb:-7ffb','',NULL),(59,1,8,13,0,'2005-01-09 21:31:59','27101fef35e777f9:6a21b2:1015a5a69fb:-7ffc','',NULL),(60,4,0,0,0,'2005-01-09 21:23:25','','Essential gloss','Having just read Harold Bloom\'s \"Anxiety of Influence\", I see more clearly than ever the \"hidden roads than go from poem to poem\".  \"The Octopus\" only APPEARS to be a parody of ACS; it\'s real target is . . .the laureate himself!  Sleepers awake!  Sleepers awake, indeed!  I am not dead, I do not sleep, I have awakened from the dream of verse.'),(63,4,0,0,0,'2005-01-09 21:29:43','','Textual error','Well, perhaps not exactly an error, simply a diversion  and coded text.  The Kraken indeed! AT actually meant \"the critic\", in prophetic awareness of the soon-to-appear review of his 1830 book of poems, which would be mauled by anabysmal creature incapable of perceiving the subsurface Tennysonian sunlight.'),(64,1,8,13,0,'2005-01-09 21:31:59','27101fef35e777f9:6a21b2:1015a5a69fb:-7fcd','More hidden roads',NULL),(65,1,8,14,0,'2005-01-09 21:31:59','27101fef35e777f9:6a21b2:1015a5a69fb:-7fce','More hidden roads',NULL),(66,1,8,13,0,'2005-01-09 21:31:59','27101fef35e777f9:6a21b2:1015a5a69fb:-7fb6','Homage to the wit an',NULL),(67,1,12,9,0,'2005-01-09 21:38:58','27101fef35e777f9:6a21b2:1015a5a69fb:-7fb7','Homage to the wit an',NULL),(70,4,0,0,0,'2005-01-12 17:36:26','','you wish','O would that the critic would!  Sleep, that is! O lay to rest the murderous intellect and relapse into the arms of Morpheus, who, even in his guise as Metamorpheus, is, through all transforms, he who knows all unaware that which the poet calls that which is.  Therefore is the light of the conscious mind denominated \"sickly.\"  Therefore, too, the primal \"dreamless\" state desiderated in this vision of the deep.  Battening \"huge sea-worms\" (an aural/oral metamorph of \"dreams\"?) down his abysmal hatch, the dreamless Kraken-Critic nothing affirmeth, and therefore never lieth, and therefore, as that sportive marlin Merlin will later have it, lieth forever.'),(81,4,0,0,0,'2005-01-12 21:47:14','','rule and misrule','Cyaxanth\'s meddling with this exquisite lyric is in itself deplorable, but it does have the virtue of demonstrating what a mug\'s game CGR liked to play.  The counters here are prosodic: 3 threes to a 2 per quatrain.  The exquisite fragility of \"Dream Land\" depends, as it were, on your not thinking metrically of Chaucer\'s Sir Topas, on your cordoning Nick Bottom on the raging rocks etc in *Midsummer Night\'s Dream* clean out of mind.  Inasmuch as this allusive repression, or willing suspension of reverberation, parallels the act of chosen oblivion which the lyric retails, it drafts the reader into a collaboration that greatly empowers the verse (collaboration which must itself remain unconscious for the reinforcing trick to come off).'),(82,1,11,7,0,'2005-01-12 22:31:28','27101fef35e777f9:cfd02:10169e784a6:-7fa0','',NULL),(83,4,0,0,0,'2005-01-12 22:12:38','','Poetical secrets','From this place where \"death lies dead\" one can see that Sir Wm may be overlooking the import of this \"single star\" by which the poet and the verse are led.  We perceive only one starry singularity in this poem, \"the nightingale\" (one star, one nightingale, amid the 2s and 3s that pluralize amid the phonic pfields).  Not the muscular poets but the song source of all poetry, the great lesbian.'),(84,1,2,7,0,'2005-01-12 22:31:28','27101fef35e777f9:cfd02:10169e784a6:-7f9f','',NULL),(85,4,0,0,0,'2005-01-12 22:26:56','','More exegesis','This is of course Homer who is being left behind, another of CR\'s Sapphic expulsions (see line 5 and accompanying commentaries).'),(86,4,0,0,0,'2005-01-12 22:29:33','','Expulsion 4','Chaucer, Shakespeare, Homer, and -- here -- also expelled, is Wordsworth.'),(87,4,0,0,0,'2005-01-13 14:51:17','','fiery cherub begone!','Oh no no no, more than enough about expulsion already!  The deep matrix of the poetical spirit absorbs, accommodates, casts not out but admits!  While the dreaming poet cannot know all things, she is open to the awareness that all things are.  She came, note, \"to seek\"; \"her face is toward,\" not turned away from, the realities.'),(89,1,2,10,0,'2005-01-08 11:57:07','27101fef35e777f9:71b856:101716ee99c:-7ff6','',NULL),(90,1,2,7,0,'2005-01-12 22:31:28','27101fef35e777f9:71b856:101716ee99c:-7fe6','',NULL),(92,1,2,17,0,'2005-01-14 08:42:19','27101fef35e777f9:71b856:101716ee99c:-7fe7','',NULL),(93,1,2,7,0,'2005-01-14 08:42:19','27101fef35e777f9:71b856:101716ee99c:-7fd6','',NULL),(94,1,2,18,0,'2005-01-14 08:42:19','27101fef35e777f9:71b856:101716ee99c:-7fd7','',NULL),(95,1,2,17,0,'2005-01-14 08:42:19','27101fef35e777f9:71b856:101716ee99c:-7ff7','',NULL),(96,4,0,0,0,'2005-01-14 08:53:15','','G. Stein!!','\"A complete poetical mappa mundi\".  The phrase exposes what is happening in both the CR ballad and the derivative ACS poems.  The former is a perfect example of what Gertrude Stein called her \"landscape\" writing -- poems as \"geography and plays\".  Lang-scapes, as one critic called them.  The line from CR and ACS to Stevens and Stein et al. is completely realized here -- something my professors never told me about!  (But in CR the map is cast according the what ACS called \"the Christian mythology\" whereas in his case the map is cast in a syncretic code, basically an Enlightenment code.'),(97,1,8,16,0,'2005-01-12 21:23:09','27101fef35e777f9:71b856:101716ee99c:-7f96','',NULL),(98,4,0,0,0,'2005-01-14 08:55:32','','Gloss','Communicable only by the entirety of the map, which this tragic writer did not have -- hence his melancholic nightmares.'),(99,1,8,9,0,'2005-01-09 21:38:58','27101fef35e777f9:71b856:101716ee99c:-7f91','',NULL),(100,1,8,9,0,'2005-01-14 09:01:53','27101fef35e777f9:71b856:101716ee99c:-7f8e','',NULL),(101,4,0,0,0,'2005-01-14 08:57:54','','Gloss','DGR\'s map is comprehensive, joining sleep and waking, but haunted by a kind of meta-state of awareness that enshadows both at once. '),(102,1,8,13,0,'2005-01-09 21:38:58','27101fef35e777f9:71b856:101716ee99c:-7f78','',NULL),(103,4,0,0,0,'2005-01-14 09:00:26','','Gloss','These lines exemplify the mappa mundi theory as well -- and in this case express a differenr flavor of the \"Christian mythology\", one committed to what AT elsewhere called (and searched after as) \"Armageddon\".'),(104,1,2,17,0,'2005-01-14 09:01:53','27101fef35e777f9:71b856:101716ee99c:-7f97','',NULL),(105,1,2,17,0,'2005-01-14 09:01:53','27101fef35e777f9:71b856:101716ee99c:-7f92','',NULL),(106,1,2,17,0,'2005-01-14 09:01:53','27101fef35e777f9:71b856:101716ee99c:-7f8f','',NULL),(107,1,2,17,0,'2005-01-14 09:01:53','27101fef35e777f9:71b856:101716ee99c:-7f79','',NULL),(108,1,12,7,0,'2005-01-12 21:48:38','f0ed2a6d7fb07688:15b6116:1018365dce2:-7f59','stein way via seamus',NULL),(109,1,2,17,0,'2005-01-17 20:46:42','f0ed2a6d7fb07688:15b6116:1018365dce2:-7f5a','stein way via seamus',NULL),(110,1,12,7,0,'2005-01-17 20:46:42','f0ed2a6d7fb07688:15b6116:1018365dce2:-7f46','stein way via seamus',NULL),(111,1,2,17,0,'2005-01-17 20:46:42','f0ed2a6d7fb07688:15b6116:1018365dce2:-7f47','stein way via seamus',NULL),(112,4,0,0,0,'2005-01-17 20:33:53','','sound encryption','CR\'s citation of this line from *Atalanta* shows her awareness of what ACS had done in order to register his awareness of what she had done.  \"Between a sleep and a sleep\" refers back to what falls between one \"sleep\" and another in the CR line referred to, \"She sleeps a charmed sleep,\" viz: \"s a charmed,\" or in other words, but not significantly other sounds, \"such armed\".  Armed like a Britomart on vigil, CR\'s she lives indeed a life of \"watch\" or \"vision\".  (We are in Mr Swinburne\'s debt for this generously vouchsafed glimpse into the unpublished correspondence, yet suspect he has been disappointed in us for not finding it out without so gross a hint.) '),(113,4,0,0,0,'2005-01-17 20:53:24','','CR foretells JJ','The road from the CR/ACS mappa mundi to the terra incognita of a \"dream land\" that Freud was to call the unconscious is pre-paved by CR\'s uncanny lyric valediction.  Her elegant poem or \"perfect piece\" will dwell in the suspended animation of the later 19th century\'s vaporous obtuseness until she, and ACS, and all their misprized tradition, are made safe for modernism (and Stein\'s serene reception, every Tuesday afternoon) by the author of *Portrait of the Artist as a Young Man*, which is to say \"till Joyce shall overtake\" her and make her new in better resurrection.'),(114,4,0,0,0,'2005-01-18 08:49:29','','comment','comment'),(125,4,0,0,0,'2005-01-21 11:15:59','',' letter to Dr. B','Why do you slay the woman by measuring her head?'),(130,4,0,0,0,'2005-01-21 16:03:16','','full stop','\"No iron can stab the heart with such force as a full stop put just at the right place.\" -- Isaac Babel'),(131,4,0,0,0,'2005-01-22 10:34:29','','HI MOM','Clean up and quiet down:  says Mom\nAfter a bull-less visit to Spain with children, I find a story about a dead bull.  \nThe story is about magic.  More to follow.'),(133,4,0,0,0,'2005-01-24 11:01:54','','measure me','\"A work of art is the one mystery, the one extreme magic; everything else is either arithmetic or biology.\" --Truman Capote'),(137,4,0,0,0,'2005-01-24 11:50:31','','his cry','\"Giants exist as a state of mind. They are defined not as an absolute measurement but as a proportionality. . . . So giants can be real, even if adults do not choose to classify them as such!\"'),(140,4,0,0,0,'2005-01-24 11:24:55','',' letter to Dr. B','Why do you slay the woman by measuring her head?'),(146,4,0,0,0,'2005-01-24 14:40:25','','special instructions','Really, this is a demo, in spite of our silly behavior.'),(154,4,0,0,0,'2005-01-26 10:52:57','','demo','??????Ã¼\0Ã¼\0'),(158,4,0,0,0,'2005-01-26 16:06:47','','central theme','I\'d like to see how this notion has a centaral role in all the series of poems'),(159,4,0,0,0,'2005-01-26 16:08:34','','deep forest','is the untreaded space where you led through your quest of emptiness'),(160,4,0,0,0,'2005-01-26 16:09:30','','people voices','its \"echoes\" in other poems '),(162,4,0,0,0,'2005-01-26 16:29:31','','request','why was that deleted ! because otherwise you do not perceive the fading away of the human'),(164,4,0,0,0,'2005-01-26 16:33:41','','Deletion','I wanted to try the deletion function, but also there is '),(165,1,33,27,0,'2005-01-26 16:34:13','37081a7bcc479e68:c1dfa9:101b0d39cf9:-7fa1','is there a connectio',NULL),(166,1,33,28,0,'2005-01-26 16:39:17','37081a7bcc479e68:c1dfa9:101b0d39cf9:-7fa2','is there a connectio',NULL),(167,4,0,0,0,'2005-01-26 16:37:35','','impression','It sounds like the verbalization of ideogrammes; in other words, a kind of approaching the archaic form   ');
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
INSERT INTO `move` VALUES (1,6,2,'The text has been corrected by me because I can\'t imagine a poet as fastidious as Miss Rossetti being able to tolerate such a sloppy version of her work being made available to the public.','2005-01-07 14:53:19','2005-01-07 15:00:46',NULL,NULL),(2,6,4,'Can women who appear in language ever sleep?','2005-01-07 15:31:05','2005-01-07 15:32:57',NULL,NULL),(3,6,6,'To create a counter-text to the first text, one that resonates and rhymes with the original, but in another key.','2005-01-07 16:14:02','2005-01-07 16:15:20',NULL,NULL),(4,6,2,'To begin a process of extrapolating the multiple resonances that are generated by any poetical field -- \"the hidden roads that go from poem to poem\".','2005-01-08 11:30:38','2005-01-08 11:37:40',NULL,NULL),(5,6,2,'More relevant texts, more relevant connections.','2005-01-08 11:42:47','2005-01-08 11:57:07',NULL,NULL),(6,6,8,'To dissallow essential authority even to genius.','2005-01-08 12:10:01','2005-01-08 12:15:25',NULL,NULL),(7,6,8,'A deferential comment on genius as well as a modest critical proposal.','2005-01-08 12:22:01','2005-01-08 12:24:28',NULL,NULL),(8,6,8,'More texts to complicate the thematic issues of sleeping, waking, dreaming, and perceiving.','2005-01-09 09:29:00','2005-01-09 09:38:45',NULL,NULL),(9,6,12,'It should provoke by irritating the reader slumbering into sensuality.','2005-01-09 16:26:21','2005-01-09 16:28:06',NULL,NULL),(10,6,12,'to provoke','2005-01-09 16:39:02','2005-01-09 16:43:35',NULL,NULL),(11,6,8,'To provoke some discussion of the textual issues.','2005-01-09 20:36:19','2005-01-09 20:38:06',NULL,NULL),(12,6,2,'A poem to put one to sleep.','2005-01-09 21:01:09','2005-01-09 21:16:40',NULL,NULL),(13,6,8,'A few intertextual markers that may help to waken us from the dream of verse.','2005-01-09 21:19:09','2005-01-09 21:31:59',NULL,NULL),(14,6,8,'Sir William Impson and other intersted wits (to wit, Goblina) may find the commentary to line 4 of \"The Kraken\" a text to batten upon.','2005-01-09 21:34:00','2005-01-09 21:38:58',NULL,NULL),(15,5,15,'Is this private or PUBLIC.  I think it\'s public.','2005-01-10 10:10:36','2005-01-10 10:12:09',NULL,NULL),(16,6,18,'','2005-01-10 19:34:59',NULL,NULL,NULL),(17,6,11,'plumbing deep structures here','2005-01-12 17:36:26','2005-01-12 17:42:25',NULL,NULL),(18,6,11,'Multiple: To suggest the grave verticality of dreams that attends rather than thwarts the verticality of dreamers; to carry the game into prose; to show how poetic an oneiric prose is like to be.','2005-01-12 21:23:10','2005-01-12 21:26:13',NULL,NULL),(19,6,11,'demented regularity, metrically expressed','2005-01-12 21:32:30','2005-01-12 21:38:48',NULL,NULL),(20,6,12,'strictly emendacious','2005-01-12 21:47:14','2005-01-12 21:48:38',NULL,NULL),(21,6,2,'As the exegetes are now abroad among us, seeking whom they may devour, it is imperative that the sheep not be led astray.','2005-01-12 22:02:28','2005-01-12 22:31:28',NULL,NULL),(22,6,11,'Cyaxanth hates to think that any option has been declined.  The more, the truer.','2005-01-13 14:51:18','2005-01-13 14:52:01',NULL,NULL),(23,6,2,'It is necessary to get all of the documents (known to me) that bear upon these matters into the public eye.','2005-01-14 08:34:49','2005-01-14 08:42:19',NULL,NULL),(24,6,8,'The critical significance of the new ACS documents begins to reveal itself through the naive, Steinian speculations of Boy Poet.','2005-01-14 08:53:15','2005-01-14 09:01:53',NULL,NULL),(25,6,12,'to advance literary history','2005-01-17 20:33:53','2005-01-17 20:46:42',NULL,NULL),(26,6,12,'in order to form a more perfect union','2005-01-17 20:53:24','2005-01-17 20:54:12',NULL,NULL),(27,5,15,'ok','2005-01-18 08:49:29','2005-01-18 08:50:58',NULL,NULL),(28,7,20,'A (no-brainer) self-representation: today, I play an Ivanhoe Game.  The device is alarmingly present.','2005-01-19 09:13:00','2005-01-19 09:21:31',NULL,NULL),(29,7,22,'Textual metaphor.  Who am I?','2005-01-19 10:11:01','2005-01-19 10:12:10',NULL,NULL),(30,7,20,'Know then thyself, presume not God to scan; \nThe proper study of mankind is man.','2005-01-19 10:38:56','2005-01-19 11:00:57',NULL,NULL),(31,5,23,'public statement','2005-01-20 14:25:12','2005-01-20 14:27:13',NULL,NULL),(32,7,22,'let\'s get some literature into this game','2005-01-20 14:32:15','2005-01-20 14:32:49',NULL,NULL),(33,7,22,'Bethany, I\'m just practising moves','2005-01-20 14:44:24','2005-01-20 14:44:55',NULL,NULL),(34,7,22,'Here\'s the text I\'ve been trying to paste in.  Now I know how to do it.  So let\'s play with some literary content now.','2005-01-21 10:21:53','2005-01-21 10:22:40',NULL,NULL),(35,7,22,'Let\'s understand measurement and interpretation','2005-01-21 11:15:59','2005-01-21 11:17:47',NULL,NULL),(36,7,20,'Clean up and quiet down!  (This move only appears anti-social. It is in fact a crystallization of my frustration with the interface -- a sentiment which dovetailed nicely with Isaac Babel\'s Strunk&Whiteism.)','2005-01-21 16:00:57','2005-01-21 16:19:40',NULL,NULL),(37,7,22,'let\'s get to the magic','2005-01-22 10:34:29','2005-01-22 10:35:11',NULL,NULL),(38,7,20,'How do we measure magic? (a move in which Craniometry settles on Bartlett\'s as better begetter of bumps, and experiments with the relative merits of comments and additions)','2005-01-24 11:00:33','2005-01-24 11:12:09',NULL,NULL),(39,7,20,'integration/narrative','2005-01-24 11:22:28','2005-01-24 11:24:16',NULL,NULL),(40,7,20,'Rationale: repair.  Please disregard last move, with its cut-and-paste accident. THIS is the real text of Bagrat-Ogly\'s cry.  (And I\'ve also remembered to keep Dead Bull\'s version in sync with commentary.)','2005-01-24 11:48:27','2005-01-24 11:54:16',NULL,NULL),(41,7,27,'This is a demo of Ivanhoe functionality.  Ignore me.','2005-01-24 14:38:54','2005-01-24 14:42:26',NULL,NULL),(42,5,29,'This move shows off a couple of bugs in Ivanhoe.','2005-01-26 10:50:00','2005-01-26 10:53:29',NULL,NULL),(43,8,33,'to add a document to the game','2005-01-26 16:00:53','2005-01-26 16:03:00',NULL,NULL),(44,8,33,'moving','2005-01-26 16:06:30','2005-01-26 16:09:07',NULL,NULL),(45,8,32,'do we know the principal themes of this poem?','2005-01-26 16:06:47','2005-01-26 16:12:11',NULL,NULL),(46,8,32,'thicken the soup','2005-01-26 16:25:18','2005-01-26 16:26:55',NULL,NULL),(47,8,32,'Try to get the point of  DigPo\'s move','2005-01-26 16:29:31','2005-01-26 16:31:22',NULL,NULL),(48,8,33,'ww','2005-01-26 16:31:41','2005-01-26 16:34:13',NULL,NULL),(49,8,33,'ww','2005-01-26 16:38:46','2005-01-26 16:39:17',NULL,NULL),(50,8,32,'ok','2005-01-26 16:37:35','2005-01-26 16:49:42',NULL,NULL);
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
INSERT INTO `move_action` VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(2,17),(3,18),(4,19),(4,20),(4,21),(4,22),(5,23),(5,24),(5,25),(5,26),(5,27),(5,28),(5,29),(6,30),(6,31),(6,32),(7,33),(7,34),(7,35),(7,36),(7,37),(7,38),(7,39),(7,40),(7,41),(7,42),(7,43),(7,44),(7,45),(7,46),(7,47),(8,48),(8,49),(8,50),(8,51),(9,52),(10,53),(11,54),(12,55),(12,56),(13,57),(13,58),(13,59),(13,60),(13,61),(13,62),(13,63),(13,64),(13,65),(14,66),(14,67),(15,68),(16,69),(17,70),(18,71),(19,72),(19,73),(19,74),(19,75),(19,76),(19,77),(19,78),(19,79),(19,80),(20,81),(21,82),(21,83),(21,84),(21,85),(21,86),(22,87),(23,88),(23,89),(23,90),(23,91),(23,92),(23,93),(23,94),(23,95),(24,96),(24,97),(24,98),(24,99),(24,100),(24,101),(24,102),(24,103),(24,104),(24,105),(24,106),(24,107),(25,108),(25,109),(25,110),(25,111),(25,112),(26,113),(27,114),(28,115),(29,116),(30,117),(30,118),(30,119),(30,120),(31,121),(32,122),(33,123),(34,124),(35,125),(36,126),(36,127),(36,128),(36,129),(36,130),(37,131),(38,132),(38,133),(39,134),(39,135),(39,136),(40,137),(40,138),(40,139),(40,140),(40,141),(41,142),(41,143),(41,144),(41,145),(41,146),(41,147),(42,148),(42,149),(42,150),(42,151),(42,152),(42,153),(42,154),(43,155),(44,156),(44,157),(45,158),(45,159),(45,160),(46,161),(47,162),(48,163),(48,164),(49,165),(49,166),(50,167);
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
INSERT INTO `player_game` VALUES (38,7),(100,7),(18,7),(17,7);
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
INSERT INTO `player_game_role` VALUES (41,6,1),(41,6,2),(100,6,3),(100,6,4),(32,6,5),(32,6,6),(17,6,7),(41,6,8),(20,6,9),(39,6,10),(39,6,11),(39,6,12),(38,6,13),(41,5,14),(41,5,15),(38,6,16),(18,6,17),(30,6,18),(38,7,19),(38,7,20),(100,7,21),(100,7,22),(20,5,23),(18,7,24),(18,7,25),(17,7,26),(38,7,27),(18,5,28),(18,5,29),(32,8,30),(20,8,31),(20,8,32),(32,8,33),(39,8,34);
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
INSERT INTO `role` VALUES (1,'Jerome McGann','','',-256,-16776961,1),(2,'A. C. Swinburne','The role is the poet and close friend of CR>\n','To play with CR\'s text from a pagan and atheitic point of view.',-6710785,-14237914,1),(3,'Jessica Feldman','','',-256,-16776961,1),(4,'Yeats','The poet W.B. Yeats','to be interested in what I\'m doing and others are doing.',-256,-16776961,1),(5,'Girl Poet','','',-256,-16776961,1),(6,'Goblina','A charming, dark female creature bent on mayhem. ','To play with the one of the great poems of perverse female sexuality. ',-52429,-13159,1),(7,'nick','','',-256,-16776961,1),(8,'Boy Poet','This is a young second year college student who has recently, as a result of reading Swinburne\'s \"Anactoria\", dropped his Statistics Major to a Statistics Minor and decided to major in Abnoral Psychology, a special field in his university.','To dig more deeply into the strange and wonderful dynamics that play about in this field of verse and re-verse.',-14237837,-9191489,1),(9,'duane','','',-256,-16776961,1),(10,'Tucker','','',-256,-16776961,1),(11,'cyaxanth','Jungian Dream Analyst lately certified by Buenos Aires institute, no patients, eager to find and spread the truth about dreams.  Blue for the collective deeps, gold for the dawn of enlightenment','to convert all interpretands into the true system of meaning',-256,-16776961,1),(12,'Sir Wm Impson','mad textualist, buzzsaw against the grain of meaning','highlight alternative readings inconclusively',-4249997,-8368871,1),(13,'Beth','','',-256,-16776961,1),(14,'Jerome McGann','','',-256,-16776961,1),(15,'tester','test role','to test',-256,-16776961,1),(16,'Laureate','I am aging poet laureate Robert Bridges, friend and posthumous editor of Gerard Manley Hopkins, who greatly admired Christina Rossetti.','to inject (and simultaneously resist?) a little Sprung Rhythm into the proceedings.',-4230285,-8368819,1),(17,'ben','','',-14276929,-11698048,1),(18,'Nathan','','',-256,-16776961,1),(19,'Beth','','',-256,-16776961,1),(20,'Craniometry','Evolving self-conception...','At the moment, I\'m just playing on the title of this test game and hoping to bring in a 19th-century flavor through physiognomy.',-11698048,-11704704,1),(21,'Jessica Feldman','','',-256,-16776961,1),(22,'dead bull','I am the bull that has been killed by a soldier','to play the game with at least a modicum of sophistication.',-52225,-52429,1),(23,'duane','','',-256,-16776961,1),(24,'ben','','',-256,-16776961,1),(25,'Ben the Developer','IVANHOE developer.','Fix bugs.',-8382183,-8382183,1),(26,'nick','','',-256,-16776961,1),(27,'Demo','Demo for our guests','have fun!',-52225,-13369345,1),(28,'ben','','',-256,-16776961,1),(29,'1337 hax0r','I break ivanhoe by introducing bugs','Break Ivanhoe.',-11724416,-15132288,1),(30,'Girl Poet','','',-256,-16776961,1),(31,'duane','','',-256,-16776961,1),(32,'interpreter','trying to grasp what the poem is at		','understand the text',-256,-16776961,1),(33,'DigPo','Swedish Digital Poetess','exploring wang wei\'s text thinking of Cayley\'s work based on it',-256,-16776961,1),(34,'Tucker','','',-256,-16776961,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `role` ENABLE KEYS */;

