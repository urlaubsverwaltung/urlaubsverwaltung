CREATE DATABASE IF NOT EXISTS `urlaubsverwaltung` CHARACTER SET UTF8 COLLATE utf8_unicode_ci;
GRANT ALL ON `urlaubsverwaltung`.* TO 'urlaubsverwaltung'@'%';

-- Make privileges active
FLUSH PRIVILEGES;
