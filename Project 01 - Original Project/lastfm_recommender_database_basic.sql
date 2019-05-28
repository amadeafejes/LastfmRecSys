
CREATE TABLE `fav_tracks` (
  `user_id` int(10) NOT NULL,
  `tra_id` int(10) NOT NULL,
  `counter` int(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `rec_tracks` (
  `user_id` int(10) NOT NULL,
  `tra_id` int(10) NOT NULL,
  `comp` float NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tracks` (
  `id` int(10) NOT NULL,
  `traid` varchar(255) DEFAULT NULL,
  `traname` varchar(255) DEFAULT NULL,
  `artid` varchar(255) DEFAULT NULL,
  `artname` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `users` (
  `id` int(10) NOT NULL,
  `userid` varchar(255) NOT NULL DEFAULT '',
  `gender` varchar(10) NOT NULL,
  `age` varchar(10) NOT NULL,
  `country` varchar(255) NOT NULL,
  `registered` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

