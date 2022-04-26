DROP DATABASE oddlabs;
CREATE DATABASE oddlabs;
USE oddlabs;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
  `mapcode` varchar(15) NOT NULL DEFAULT '',
  `status` enum('created','started','completed','dropped') NOT NULL DEFAULT 'created',
  `winner` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `messages` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `message` text NOT NULL,
  KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `online_profiles` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `nick` varchar(20) NOT NULL DEFAULT '',
  `game_id` int(11) NOT NULL DEFAULT '2137',
  PRIMARY KEY (`id`),
  UNIQUE KEY `nick` (`nick`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `profiles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reg_id` int(11) NOT NULL DEFAULT '0',
  `nick` varchar(20) NOT NULL DEFAULT '',
  `rating` int(11) NOT NULL DEFAULT '0',
  `wins` int(11) NOT NULL DEFAULT '0',
  `losses` int(11) NOT NULL DEFAULT '0',
  `invalid` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `nick` (`nick`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `settings` (
  `property` varchar(50) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`property`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `settings` VALUES ('max_profiles', '5');
INSERT INTO `settings` VALUES ('min_username_length', '2');
INSERT INTO `settings` VALUES ('max_username_length', '20');
INSERT INTO `settings` VALUES ('min_password_length', '6');
INSERT INTO `settings` VALUES ('max_password_length', '20');
INSERT INTO `settings` VALUES ('allowed_chars', 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789^|_.,:;?+={}[]()/\\&%#!<>*@$');
INSERT INTO `settings` VALUES ('revision', '2137');

CREATE VIEW match_valid_key AS
SELECT reg_key FROM registrations R WHERE NOT R.disabled;

GRANT ALL ON oddlabs.* TO 'matchmaker'@'localhost' IDENTIFIED BY 'U46TawOp';

FLUSH PRIVILEGES;
