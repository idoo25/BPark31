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
-- Table structure for table `parkinginfo`
--

DROP TABLE IF EXISTS `parkinginfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parkinginfo` (
  `ParkingInfo_ID` int NOT NULL AUTO_INCREMENT,
  `ParkingSpot_ID` int NOT NULL,
  `User_ID` int NOT NULL,
  `Date_Of_Placing_Order` datetime DEFAULT NULL,
  `Actual_start_time` datetime DEFAULT NULL,
  `Actual_end_time` datetime DEFAULT NULL,
  `Estimated_start_time` datetime DEFAULT NULL,
  `Estimated_end_time` datetime DEFAULT NULL,
  `IsOrderedEnum` enum('yes','no') NOT NULL DEFAULT 'no',
  `IsLate` enum('yes','no') NOT NULL DEFAULT 'no',
  `IsExtended` enum('yes','no') NOT NULL DEFAULT 'no',
  `statusEnum` enum('preorder','active','finished','cancelled') NOT NULL,
  PRIMARY KEY (`ParkingInfo_ID`),
  KEY `ParkingSpot_ID` (`ParkingSpot_ID`),
  KEY `idx_user_id` (`User_ID`),
  KEY `idx_status` (`statusEnum`),
  KEY `idx_estimated_start` (`Estimated_start_time`),
  KEY `idx_actual_start` (`Actual_start_time`),
  CONSTRAINT `parkinginfo_ibfk_1` FOREIGN KEY (`User_ID`) REFERENCES `users` (`User_ID`),
  CONSTRAINT `parkinginfo_ibfk_2` FOREIGN KEY (`ParkingSpot_ID`) REFERENCES `parkingspot` (`ParkingSpot_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parkinginfo`
--

LOCK TABLES `parkinginfo` WRITE;
/*!40000 ALTER TABLE `parkinginfo` DISABLE KEYS */;
INSERT INTO `parkinginfo` VALUES (15,1,7,'2025-06-20 12:06:51',NULL,NULL,'2025-06-26 07:45:00','2025-06-26 11:45:00','yes','no','no','cancelled'),(16,2,7,'2025-06-20 12:06:57',NULL,NULL,'2025-06-26 07:45:00','2025-06-26 11:45:00','yes','no','no','cancelled'),(17,3,7,'2025-06-18 12:06:57',NULL,NULL,'2025-06-20 12:00:00','2025-06-20 16:00:00','yes','no','no','cancelled'),(18,1,7,'2025-06-20 12:23:35',NULL,NULL,'2025-06-27 08:00:00','2025-06-27 12:00:00','yes','no','no','cancelled'),(19,2,7,'2025-06-20 12:23:46',NULL,NULL,'2025-06-27 09:00:00','2025-06-27 13:00:00','yes','no','no','cancelled'),(20,3,7,'2025-06-20 12:24:08',NULL,NULL,'2025-06-27 09:15:00','2025-06-27 13:15:00','yes','no','no','cancelled'),(21,1,7,'2025-06-20 12:25:01',NULL,NULL,'2025-06-23 08:00:00','2025-06-23 12:00:00','yes','no','no','cancelled'),(22,1,7,'2025-06-20 12:25:10',NULL,NULL,'2025-06-23 12:15:00','2025-06-23 16:15:00','yes','no','no','cancelled'),(23,1,7,'2025-06-20 13:03:05',NULL,NULL,'2025-06-25 22:15:00','2025-06-26 02:15:00','yes','no','no','cancelled'),(24,3,4,'2025-06-20 13:13:53',NULL,NULL,'2025-06-26 07:15:00','2025-06-26 11:15:00','yes','no','no','cancelled'),(25,4,4,'2025-06-20 13:13:56',NULL,NULL,'2025-06-26 07:15:00','2025-06-26 11:15:00','yes','no','no','cancelled'),(26,5,4,'2025-06-20 13:14:51',NULL,NULL,'2025-06-26 07:45:00','2025-06-26 11:45:00','yes','no','no','cancelled'),(27,6,7,'2025-06-24 10:56:26',NULL,NULL,'2025-06-26 06:30:00','2025-06-26 10:30:00','yes','no','no','cancelled'),(37,1,8,'2025-06-30 21:30:24','2025-06-30 21:30:24',NULL,'2025-06-30 21:30:24','2025-07-01 01:30:24','no','yes','no','active'),(38,2,7,'2025-06-30 21:30:44','2025-06-30 21:30:45','2025-07-06 14:03:45','2025-06-30 21:30:45','2025-07-01 01:30:45','no','yes','no','finished'),(39,1,7,'2025-07-06 15:53:43',NULL,NULL,'2025-07-08 07:00:00','2025-07-08 11:00:00','yes','no','no','cancelled'),(45,1,7,'2025-07-05 15:53:43',NULL,NULL,'2025-07-07 15:45:00','2025-07-07 19:45:00','yes','no','no','cancelled'),(46,1,7,'2025-07-07 10:10:43','2025-07-07 10:10:43',NULL,'2025-07-07 10:10:43','2025-07-07 16:10:43','no','yes','no','active'),(47,4,12,'2025-07-07 10:40:28','2025-07-07 10:40:28',NULL,'2025-07-07 16:40:28','2025-07-07 16:40:28','no','yes','no','active'),(48,5,15,'2025-07-07 10:40:28','2025-07-07 10:40:28',NULL,'2025-07-07 16:40:28','2025-07-07 16:40:28','no','yes','no','active'),(49,6,18,'2025-07-07 10:40:28','2025-07-07 10:40:28',NULL,'2025-07-07 16:40:28','2025-07-07 16:40:28','no','yes','no','active'),(50,1,9,'2025-07-07 17:47:29',NULL,NULL,'2025-07-09 09:00:00','2025-07-09 13:00:00','yes','no','no','preorder');
/*!40000 ALTER TABLE `parkinginfo` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-07 17:50:36
