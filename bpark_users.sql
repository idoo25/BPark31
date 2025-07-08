-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: bpark
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `User_ID` int NOT NULL AUTO_INCREMENT,
  `Name` varchar(50) DEFAULT NULL,
  `Phone` varchar(50) DEFAULT NULL,
  `Email` varchar(50) DEFAULT NULL,
  `CarNum` varchar(50) DEFAULT NULL,
  `UserName` varchar(50) DEFAULT NULL,
  `UserTypeEnum` enum('sub','emp','mng') DEFAULT NULL,
  PRIMARY KEY (`User_ID`),
  UNIQUE KEY `UserName_UNIQUE` (`UserName`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (4,'John Subscriber','050-1234567','john@email.com','123-45-678','john_user','sub'),(5,'Alice Attendant','050-2345678','alice@email.com','234-56-789','alice_emp','emp'),(6,'Bob Manager','050-3456789','bob@email.com','345-67-890','bob_mgr','mng'),(7,'ido porat','0509353400','idopo25@googlemail.com','9065745','idoporat_user','sub'),(8,'ido cohen','0509353401','idopo25@gmail.com','2488413','idocohen','sub'),(9,'Ron Subscriber','050-7890123','ron@email.com','789-01-234','ron_sub','sub'),(10,'Tom Attendant','050-8901234','tom@email.com','890-12-345','tom_emp','emp'),(11,'Sara Manager','050-9012345','sara@email.com','901-23-456','sara_mgr','mng'),(12,'Dina Subscriber','050-0123456','dina@email.com','012-34-567','dina_sub','sub'),(13,'Erez Employee','050-1357913','erez@email.com','135-79-246','erez_emp','emp'),(14,'Shira Manager','050-2468135','shira@email.com','246-81-357','shira_mgr','mng'),(15,'Oren Subscriber','050-3579246','oren@email.com','357-92-468','oren_sub','sub'),(18,'Tal Subscriber','050-6803579','liorstein1@gmail.com','680-35-791','tal_sub','sub'),(21,'Gal Subscriber','050-8101111','gal_sub@email.com','123-10-011','gal_sub','sub'),(22,'Roni Subscriber','050-8202222','roni_sub@email.com','223-20-022','roni_sub','sub'),(23,'Noa Subscriber','050-8303333','noa_sub@email.com','323-30-033','noa_sub','sub'),(24,'Tomer Subscriber','050-8404444','tomer_sub@email.com','423-40-044','tomer_sub','sub'),(25,'Dana Subscriber','050-8505555','dana_sub@email.com','523-50-055','dana_sub','sub'),(26,'Liat Subscriber','050-8606666','liat_sub@email.com','623-60-066','liat_sub','sub'),(27,'Avi Subscriber','050-8707777','avi_sub@email.com','723-70-077','avi_sub','sub');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-07 17:50:35
