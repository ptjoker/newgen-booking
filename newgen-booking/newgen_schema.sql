-- MySQL dump 10.13  Distrib 9.6.0, for macos15.7 (arm64)
--
-- Host: localhost    Database: NewGenBookingSchemas
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin_actions`
--

DROP TABLE IF EXISTS `admin_actions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_actions` (
  `action_id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int NOT NULL,
  `action_type` enum('create','update','delete','verify','suspend','report_generate') NOT NULL,
  `target_table` varchar(50) DEFAULT NULL,
  `target_record_id` int DEFAULT NULL,
  `target_user` int DEFAULT NULL,
  `target_provider` int DEFAULT NULL,
  `description` text,
  `old_values` json DEFAULT NULL,
  `new_values` json DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `user_agent` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`action_id`),
  KEY `target_user` (`target_user`),
  KEY `target_provider` (`target_provider`),
  KEY `idx_admin_actions` (`admin_id`,`created_at`),
  KEY `idx_target` (`target_table`,`target_record_id`),
  CONSTRAINT `admin_actions_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `admin_actions_ibfk_2` FOREIGN KEY (`target_user`) REFERENCES `users` (`user_id`),
  CONSTRAINT `admin_actions_ibfk_3` FOREIGN KEY (`target_provider`) REFERENCES `providers` (`provider_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `provider_id` int NOT NULL,
  `event_id` int DEFAULT NULL,
  `booking_type` enum('appointment','event_ticket','hotel','trip') NOT NULL,
  `booking_date` date NOT NULL,
  `time_slot` time DEFAULT NULL,
  `number_of_guests` int DEFAULT '1',
  `special_requests` text,
  `total_amount` decimal(10,2) NOT NULL,
  `status` enum('pending','confirmed','cancelled','completed','no_show') DEFAULT 'pending',
  `cancellation_reason` text,
  `checked_in` tinyint(1) DEFAULT '0',
  `checked_in_time` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `booking_reference` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`booking_id`),
  KEY `user_id` (`user_id`),
  KEY `provider_id` (`provider_id`),
  KEY `event_id` (`event_id`),
  KEY `idx_booking_date` (`booking_date`),
  KEY `idx_status` (`status`),
  KEY `idx_booking_type` (`booking_type`),
  CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`provider_id`) ON DELETE CASCADE,
  CONSTRAINT `bookings_ibfk_3` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `events`
--

DROP TABLE IF EXISTS `events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `events` (
  `event_id` int NOT NULL AUTO_INCREMENT,
  `provider_id` int NOT NULL,
  `event_name` varchar(200) NOT NULL,
  `description` text,
  `event_type` enum('concert','conference','workshop','sports','other') DEFAULT 'other',
  `event_date` datetime NOT NULL,
  `end_date` datetime DEFAULT NULL,
  `location` varchar(255) NOT NULL,
  `venue_details` text,
  `total_tickets` int NOT NULL,
  `available_tickets` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `early_bird_price` decimal(10,2) DEFAULT NULL,
  `group_discount` tinyint(1) DEFAULT '0',
  `image_url` varchar(255) DEFAULT NULL,
  `status` enum('draft','published','cancelled','completed') DEFAULT 'draft',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`event_id`),
  KEY `provider_id` (`provider_id`),
  KEY `idx_event_date` (`event_date`),
  KEY `idx_available_tickets` (`available_tickets`),
  KEY `idx_status` (`status`),
  CONSTRAINT `events_ibfk_1` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`provider_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `notif_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `title` varchar(200) NOT NULL,
  `message` text NOT NULL,
  `type` enum('booking','payment','reminder','promotion','system','admin') NOT NULL,
  `reference_id` int DEFAULT NULL,
  `reference_type` varchar(50) DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `read_at` datetime DEFAULT NULL,
  `is_archived` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notif_id`),
  KEY `idx_user_notifications` (`user_id`,`is_read`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `booking_id` int NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `payment_method` enum('credit_card','debit_card','cash') NOT NULL,
  `transaction_id` varchar(100) DEFAULT NULL,
  `status` enum('pending','completed','failed','refunded','partially_refunded') DEFAULT 'pending',
  `refund_amount` decimal(10,2) DEFAULT NULL,
  `refund_reason` text,
  `refunded_by` int DEFAULT NULL,
  `refund_date` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `booking_id` (`booking_id`),
  UNIQUE KEY `transaction_id` (`transaction_id`),
  KEY `refunded_by` (`refunded_by`),
  KEY `idx_payment_date` (`payment_date`),
  KEY `idx_transaction_id` (`transaction_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE,
  CONSTRAINT `payments_ibfk_2` FOREIGN KEY (`refunded_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `providers`
--

DROP TABLE IF EXISTS `providers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `providers` (
  `provider_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `business_name` varchar(100) NOT NULL,
  `service_type` enum('hotel','doctor','trip','event_organizer') NOT NULL,
  `registration_number` varchar(50) DEFAULT NULL,
  `address` text NOT NULL,
  `city` varchar(100) DEFAULT NULL,
  `province` varchar(50) DEFAULT NULL,
  `postal_code` varchar(20) DEFAULT NULL,
  `phone` varchar(20) NOT NULL,
  `website` varchar(255) DEFAULT NULL,
  `description` text,
  `business_hours` text,
  `verified` tinyint(1) DEFAULT '0',
  `verification_date` datetime DEFAULT NULL,
  `verified_by` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`provider_id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `verified_by` (`verified_by`),
  KEY `idx_service_type` (`service_type`),
  KEY `idx_city` (`city`),
  KEY `idx_verified` (`verified`),
  CONSTRAINT `providers_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `providers_ibfk_2` FOREIGN KEY (`verified_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int NOT NULL,
  `report_type` enum('bookings','payments','users','providers','revenue','commission') NOT NULL,
  `report_format` enum('pdf','excel','csv') DEFAULT 'pdf',
  `generated_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `date_range_start` date DEFAULT NULL,
  `date_range_end` date DEFAULT NULL,
  `filters` json DEFAULT NULL,
  `file_path` varchar(255) DEFAULT NULL,
  `file_size` int DEFAULT NULL,
  `status` enum('generating','completed','failed') DEFAULT 'completed',
  `download_count` int DEFAULT '0',
  PRIMARY KEY (`report_id`),
  KEY `admin_id` (`admin_id`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_generated_date` (`generated_date`),
  CONSTRAINT `reports_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `schedules`
--

DROP TABLE IF EXISTS `schedules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedules` (
  `schedule_id` int NOT NULL AUTO_INCREMENT,
  `provider_id` int NOT NULL,
  `day_of_week` tinyint DEFAULT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `break_start` time DEFAULT NULL,
  `break_end` time DEFAULT NULL,
  `max_bookings_per_slot` int DEFAULT '1',
  `is_available` tinyint(1) DEFAULT '1',
  `effective_from` date DEFAULT NULL,
  `effective_to` date DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`schedule_id`),
  KEY `idx_provider_schedule` (`provider_id`,`day_of_week`),
  KEY `idx_effective_dates` (`effective_from`,`effective_to`),
  CONSTRAINT `schedules_ibfk_1` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`provider_id`) ON DELETE CASCADE,
  CONSTRAINT `schedules_chk_1` CHECK ((`day_of_week` between 0 and 6))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` enum('customer','provider','admin') NOT NULL DEFAULT 'customer',
  `profile_picture` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_email` (`email`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-13 15:39:05
