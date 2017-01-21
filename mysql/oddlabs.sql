-- MySQL dump 10.15  Distrib 10.0.29-MariaDB, for Linux (i686)
--
-- Host: localhost    Database: localhost
-- ------------------------------------------------------
-- Server version	10.0.28-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `game_reports`
--

DROP TABLE IF EXISTS `game_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_reports` (
  `game_id` int(11) NOT NULL DEFAULT '0',
  `tick` int(11) NOT NULL DEFAULT '0',
  `team1` int(11) NOT NULL DEFAULT '0',
  `team2` int(11) NOT NULL DEFAULT '0',
  `team3` int(11) NOT NULL DEFAULT '0',
  `team4` int(11) NOT NULL DEFAULT '0',
  `team5` int(11) NOT NULL DEFAULT '0',
  `team6` int(11) NOT NULL DEFAULT '0',
  `team7` int(11) NOT NULL DEFAULT '0',
  `team8` int(11) NOT NULL DEFAULT '0',
  KEY `game_id` (`game_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `games`
--

DROP TABLE IF EXISTS `games`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `games` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `player1_name` varchar(20) NOT NULL DEFAULT '',
  `player2_name` varchar(20) NOT NULL DEFAULT '',
  `player3_name` varchar(20) NOT NULL DEFAULT '',
  `player4_name` varchar(20) NOT NULL DEFAULT '',
  `player5_name` varchar(20) NOT NULL DEFAULT '',
  `player6_name` varchar(20) NOT NULL DEFAULT '',
  `player7_name` varchar(20) NOT NULL DEFAULT '',
  `player8_name` varchar(20) NOT NULL DEFAULT '',
  `player1_race` enum('V','N') NOT NULL DEFAULT 'V',
  `player2_race` enum('V','N') NOT NULL DEFAULT 'V',
  `player3_race` enum('V','N') NOT NULL DEFAULT 'V',
  `player4_race` enum('V','N') NOT NULL DEFAULT 'V',
  `player5_race` enum('V','N') NOT NULL DEFAULT 'V',
  `player6_race` enum('V','N') NOT NULL DEFAULT 'V',
  `player7_race` enum('V','N') NOT NULL DEFAULT 'V',
  `player8_race` enum('V','N') NOT NULL DEFAULT 'V',
  `player1_team` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `player2_team` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `player3_team` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `player4_team` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `player5_team` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `player6_team` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `player7_team` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `player8_team` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `time_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `time_start` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `time_stop` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `name` varchar(40) NOT NULL DEFAULT '',
  `rated` enum('N','Y') NOT NULL DEFAULT 'N',
  `speed` enum('1','2','3','4') NOT NULL DEFAULT '1',
  `size` enum('1','2','3','4') NOT NULL DEFAULT '1',
  `hills` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `trees` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `resources` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `mapcode` varchar(16) NOT NULL DEFAULT '',
  `status` enum('created','started','completed','dropped') NOT NULL DEFAULT 'created',
  `winner` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1496872 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `match_user`
--

DROP TABLE IF EXISTS `match_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `match_user` (
  `username` varchar(20) NOT NULL,
  `id` int(11) NOT NULL,
  `reg_key` varchar(20) NOT NULL,
  `password` varchar(40) NOT NULL,
  `email` varchar(60) NOT NULL,
  `last_used_profile` int(10) DEFAULT NULL,
  `banned_until` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`username`),
  UNIQUE KEY `reg_key` (`reg_key`),
  KEY `banned_until` (`banned_until`),
  KEY `id` (`id`,`reg_key`),
  CONSTRAINT `match_user_ibfk_1` FOREIGN KEY (`id`, `reg_key`) REFERENCES `registrations` (`id`, `reg_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `match_valid_key`
--

DROP TABLE IF EXISTS `match_valid_key`;
/*!50001 DROP VIEW IF EXISTS `match_valid_key`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `match_valid_key` (
  `reg_key` tinyint NOT NULL
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

--
-- Temporary table structure for view `match_valid_user`
--

DROP TABLE IF EXISTS `match_valid_user`;
/*!50001 DROP VIEW IF EXISTS `match_valid_user`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `match_valid_user` (
  `username` tinyint NOT NULL,
  `password` tinyint NOT NULL
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `messages` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `message` text NOT NULL,
  KEY `id` (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=43783 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `online_profiles`
--

DROP TABLE IF EXISTS `online_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `online_profiles` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `nick` varchar(20) NOT NULL DEFAULT '',
  `game_id` int(11) NOT NULL DEFAULT '2137',
  PRIMARY KEY (`id`),
  UNIQUE KEY `nick` (`nick`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `profiles`
--

DROP TABLE IF EXISTS `profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profiles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reg_id` int(11) NOT NULL DEFAULT '0',
  `nick` varchar(20) NOT NULL DEFAULT '',
  `rating` int(11) NOT NULL DEFAULT '0',
  `wins` int(10) NOT NULL DEFAULT '0',
  `losses` int(10) NOT NULL DEFAULT '0',
  `invalid` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `nick` (`nick`)
) ENGINE=MyISAM AUTO_INCREMENT=93838 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `registrations`
--

DROP TABLE IF EXISTS `registrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `registrations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `last_used_profile` varchar(20) NOT NULL DEFAULT '',
  `reg_key` varchar(20) NOT NULL,
  `disabled` tinyint(1) NOT NULL DEFAULT '0',
  `banned` tinyint(1) NOT NULL DEFAULT '0',
  `reg_email` varchar(60) NOT NULL DEFAULT '',
  `email` varchar(60) NOT NULL DEFAULT '',
  `timezone` tinyint(4) NOT NULL DEFAULT '0',
  `reg_time` datetime DEFAULT '0000-00-00 00:00:00',
  `num_reg` int(10) unsigned NOT NULL DEFAULT '0',
  `name` varchar(80) NOT NULL DEFAULT '',
  `username` varchar(20) NOT NULL DEFAULT '',
  `password` varchar(40) NOT NULL DEFAULT '',
  `address1` varchar(40) NOT NULL DEFAULT '',
  `address2` varchar(40) NOT NULL DEFAULT '',
  `zip` varchar(40) NOT NULL DEFAULT '',
  `city` varchar(40) NOT NULL DEFAULT '',
  `state` varchar(40) NOT NULL DEFAULT '',
  `country` varchar(40) NOT NULL DEFAULT '',
  `coupon` varchar(8) NOT NULL DEFAULT '',
  `os` char(1) NOT NULL DEFAULT '',
  `affiliate` varchar(50) NOT NULL DEFAULT '',
  `shop` varchar(20) NOT NULL DEFAULT 'swreg',
  `ref` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`,`reg_key`),
  KEY `disabled` (`disabled`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `settings`
--

DROP TABLE IF EXISTS `settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `settings` (
  `property` varchar(50) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`property`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Final view structure for view `match_valid_key`
--

/*!50001 DROP TABLE IF EXISTS `match_valid_key`*/;
/*!50001 DROP VIEW IF EXISTS `match_valid_key`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `match_valid_key` AS select `R`.`reg_key` AS `reg_key` from `registrations` `R` where (`R`.`disabled` = 0) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `match_valid_user`
--

/*!50001 DROP TABLE IF EXISTS `match_valid_user`*/;
/*!50001 DROP VIEW IF EXISTS `match_valid_user`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `match_valid_user` AS select `U`.`username` AS `username`,`U`.`password` AS `password` from (`match_valid_key` `K` join `match_user` `U`) where ((`U`.`reg_key` = `K`.`reg_key`) and (`U`.`banned_until` < now())) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-01-21  7:42:41
