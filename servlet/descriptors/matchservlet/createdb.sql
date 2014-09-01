CREATE DATABASE oddlabs;

USE oddlabs;

CREATE TABLE `registrations` (
	  `reg_key` varchar(20) NOT NULL,
	  `disabled` tinyint(1) NOT NULL default 0,
	  `reg_email` varchar(60) NOT NULL default '',
	  `timezone` tinyint(4) NOT NULL default '0',
	  `reg_time` datetime default '0000-00-00 00:00:00',
	  `num_reg` int(10) unsigned NOT NULL default '0',
	  `name` varchar(80) NOT NULL default '',
	  `address1` varchar(40) NOT NULL default '',
	  `address2` varchar(40) NOT NULL default '',
	  `zip` varchar(40) NOT NULL default '',
	  `city` varchar(40) NOT NULL default '',
	  `state` varchar(40) NOT NULL default '',
	  `country` varchar(40) NOT NULL default '',
	  `coupon` varchar(8) NOT NULL default '',
	  `os` char(1) NOT NULL default '',
	  `affiliate` varchar(50) NOT NULL default '',
	  `shop` varchar(20) NOT NULL default 'swreg',
	  `ref` varchar(255) NOT NULL default '',
	  PRIMARY KEY (`reg_key`),
	  INDEX (`disabled`)
) ENGINE=InnoDB;

CREATE TABLE `match_user` (
	  `username` varchar(20) NOT NULL,
	  `reg_key` varchar(20) NOT NULL,
	  `password` varchar(40) NOT NULL,
	  `email` varchar(60) NOT NULL,
	  `last_used_profile` int(10) default NULL,
	  `banned_until` datetime NOT NULL default '0000-00-00 00:00:00',
	  PRIMARY KEY (`username`),
	  UNIQUE KEY (`reg_key`),
	  INDEX (`banned_until`),
	  FOREIGN KEY (`reg_key`) REFERENCES `registrations` (`reg_key`)
) ENGINE=InnoDB;

CREATE TABLE settings (
	property varchar(50) NOT NULL,
	value varchar(255) NOT NULL,
	PRIMARY KEY (property)
) ENGINE=InnoDB;

CREATE VIEW match_valid_key AS
SELECT reg_key FROM registrations R WHERE NOT R.disabled;

CREATE VIEW match_valid_user AS
SELECT username, password FROM match_valid_key K, match_user U WHERE U.reg_key = K.reg_key AND U.banned_until < NOW();

INSERT INTO `settings` VALUES ('max_profiles', '5');
INSERT INTO `settings` VALUES ('min_username_length', '2');
INSERT INTO `settings` VALUES ('max_username_length', '20');
INSERT INTO `settings` VALUES ('min_password_length', '6');
INSERT INTO `settings` VALUES ('max_password_length', '20');
INSERT INTO `settings` VALUES ('allowed_chars', 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789^|_.,:;?+={}[]()/\\&%#!<>*@$');
INSERT INTO `settings` VALUES ('revision', '1116');

GRANT SELECT ON match_valid_user TO matchservlet IDENTIFIED BY 'match';
GRANT SELECT ON match_valid_key TO matchservlet;
GRANT SELECT ON settings TO matchservlet;
GRANT INSERT ON match_user TO matchservlet;
GRANT SELECT (reg_key, username) ON match_user TO matchservlet;
FLUSH PRIVILEGES;
