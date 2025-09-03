-- MySQL dump 10.13  Distrib 9.4.0, for macos15.4 (arm64)
--
-- Host: 54.180.64.110    Database: climbx
-- ------------------------------------------------------
-- Server version	9.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

--
-- Table structure for table `contribution_tag`
--

DROP TABLE IF EXISTS `contribution_tag`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contribution_tag`
(
    `contribution_id` bigint                                                                                                                                                             NOT NULL,
    `created_at`      datetime(6) DEFAULT NULL,
    `deleted_at`      datetime(6) DEFAULT NULL,
    `updated_at`      datetime(6) DEFAULT NULL,
    `tag`             enum ('BALANCE','BATHANG','COORDINATE','CRIMP_HOLD','DROP_KNEE','DYNO','HEEL_HOOK','LUNGE','OVERHANG','PINCH_HOLD','POCKET_HOLD','REACH','SLOPER_HOLD','TOE_HOOK') NOT NULL,
    PRIMARY KEY (`contribution_id`, `tag`),
    CONSTRAINT `FK4p3j6il838rubywbbinhjcey8` FOREIGN KEY (`contribution_id`) REFERENCES `contributions` (`contribution_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contributions`
--

DROP TABLE IF EXISTS `contributions`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contributions`
(
    `contribution_id` bigint      NOT NULL AUTO_INCREMENT,
    `created_at`      datetime(6)  DEFAULT NULL,
    `deleted_at`      datetime(6)  DEFAULT NULL,
    `updated_at`      datetime(6)  DEFAULT NULL,
    `user_id`         bigint       DEFAULT NULL,
    `problem_id`      binary(16)   DEFAULT NULL,
    `comment`         varchar(512) DEFAULT NULL,
    `tier`            varchar(16) NOT NULL,
    PRIMARY KEY (`contribution_id`),
    KEY `FKpma594524o28w7la8ql5ddex6` (`problem_id`),
    KEY `FKpob52gn2004y8vf4nslgkey3n` (`user_id`),
    CONSTRAINT `FKpma594524o28w7la8ql5ddex6` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`problem_id`),
    CONSTRAINT `FKpob52gn2004y8vf4nslgkey3n` FOREIGN KEY (`user_id`) REFERENCES `user_accounts` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gym_areas`
--

DROP TABLE IF EXISTS `gym_areas`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gym_areas`
(
    `created_at`         datetime(6)  DEFAULT NULL,
    `deleted_at`         datetime(6)  DEFAULT NULL,
    `gym_area_id`        bigint      NOT NULL AUTO_INCREMENT,
    `gym_id`             bigint      NOT NULL,
    `updated_at`         datetime(6)  DEFAULT NULL,
    `area_name`          varchar(64) NOT NULL,
    `area_image_cdn_url` varchar(256) DEFAULT NULL,
    PRIMARY KEY (`gym_area_id`),
    KEY `FKl3qfv5btltba6yxean3wsmcbh` (`gym_id`),
    CONSTRAINT `FKl3qfv5btltba6yxean3wsmcbh` FOREIGN KEY (`gym_id`) REFERENCES `gyms` (`gym_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 66
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gyms`
--

DROP TABLE IF EXISTS `gyms`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gyms`
(
    `latitude`             double       DEFAULT NULL,
    `longitude`            double       DEFAULT NULL,
    `created_at`           datetime(6)  DEFAULT NULL,
    `deleted_at`           datetime(6)  DEFAULT NULL,
    `gym_id`               bigint      NOT NULL AUTO_INCREMENT,
    `updated_at`           datetime(6)  DEFAULT NULL,
    `name`                 varchar(64) NOT NULL,
    `phone_number`         varchar(64)  DEFAULT NULL,
    `address`              varchar(128) DEFAULT NULL,
    `map_2d_image_cdn_url` varchar(256) DEFAULT NULL,
    PRIMARY KEY (`gym_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 14
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `problem_tags`
--

DROP TABLE IF EXISTS `problem_tags`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `problem_tags`
(
    `priority`   int                                                                                                                                                                NOT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `deleted_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `problem_id` binary(16)                                                                                                                                                         NOT NULL,
    `tag`        enum ('BALANCE','BATHANG','COORDINATE','CRIMP_HOLD','DROP_KNEE','DYNO','HEEL_HOOK','LUNGE','OVERHANG','PINCH_HOLD','POCKET_HOLD','REACH','SLOPER_HOLD','TOE_HOOK') NOT NULL,
    PRIMARY KEY (`problem_id`, `tag`),
    CONSTRAINT `FK7qkq0yfm0tv41qajcxo05wv03` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`problem_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `problems`
--

DROP TABLE IF EXISTS `problems`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `problems`
(
    `problem_rating`        int         NOT NULL,
    `created_at`            datetime(6)  DEFAULT NULL,
    `deleted_at`            datetime(6)  DEFAULT NULL,
    `gym_area_id`           bigint      NOT NULL,
    `gym_id`                bigint      NOT NULL,
    `updated_at`            datetime(6)  DEFAULT NULL,
    `problem_id`            binary(16)  NOT NULL,
    `problem_image_cdn_url` varchar(512) DEFAULT NULL,
    `active_status`         varchar(16) NOT NULL,
    `hold_color`            varchar(32) NOT NULL,
    `local_level`           varchar(32) NOT NULL,
    `primary_tag`           varchar(16)  DEFAULT NULL,
    `problem_tier`          varchar(16) NOT NULL,
    `secondary_tag`         varchar(16)  DEFAULT NULL,
    PRIMARY KEY (`problem_id`),
    KEY `FK9iia0d74xxd1h821wi8arxjyi` (`gym_area_id`),
    KEY `FKn5tjlxqxhe0ctxw91fujmv51v` (`gym_id`),
    CONSTRAINT `FK9iia0d74xxd1h821wi8arxjyi` FOREIGN KEY (`gym_area_id`) REFERENCES `gym_areas` (`gym_area_id`),
    CONSTRAINT `FKn5tjlxqxhe0ctxw91fujmv51v` FOREIGN KEY (`gym_id`) REFERENCES `gyms` (`gym_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `submissions`
--

DROP TABLE IF EXISTS `submissions`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `submissions`
(
    `created_at`     datetime(6)  DEFAULT NULL,
    `deleted_at`     datetime(6)  DEFAULT NULL,
    `updated_at`     datetime(6)  DEFAULT NULL,
    `problem_id`     binary(16)  NOT NULL,
    `video_id`       binary(16)  NOT NULL,
    `appeal_content` varchar(256) DEFAULT NULL,
    `reject_reason`  varchar(256) DEFAULT NULL,
    `appeal_status`  varchar(32)  DEFAULT NULL,
    `status`         varchar(32) NOT NULL,
    PRIMARY KEY (`video_id`),
    KEY `FKa58pkr3attmu3rm2kydp9i5dm` (`problem_id`),
    CONSTRAINT `FKa58pkr3attmu3rm2kydp9i5dm` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`problem_id`),
    CONSTRAINT `FKt22vwxqrkjasxe4rehu026lg2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_accounts`
--

DROP TABLE IF EXISTS `user_accounts`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_accounts`
(
    `last_login_date`       date        NOT NULL,
    `created_at`            datetime(6)  DEFAULT NULL,
    `deleted_at`            datetime(6)  DEFAULT NULL,
    `updated_at`            datetime(6)  DEFAULT NULL,
    `user_id`               bigint      NOT NULL AUTO_INCREMENT,
    `nickname`              varchar(64) NOT NULL,
    `status_message`        varchar(128) DEFAULT NULL,
    `profile_image_cdn_url` varchar(256) DEFAULT NULL,
    `role`                  varchar(32) NOT NULL,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `UK8id8d2j5q48p721d7vt2pi66p` (`nickname`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 35
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_auths`
--

DROP TABLE IF EXISTS `user_auths`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_auths`
(
    `is_primary`     bit(1)       NOT NULL,
    `auth_id`        bigint       NOT NULL AUTO_INCREMENT,
    `created_at`     datetime(6)  DEFAULT NULL,
    `deleted_at`     datetime(6)  DEFAULT NULL,
    `updated_at`     datetime(6)  DEFAULT NULL,
    `user_id`        bigint       NOT NULL,
    `provider_email` varchar(128) DEFAULT NULL,
    `provider_id`    varchar(128) NOT NULL,
    `provider`       varchar(32)  NOT NULL,
    PRIMARY KEY (`auth_id`),
    UNIQUE KEY `uk_user_auths_provider_id` (`provider`, `provider_id`),
    UNIQUE KEY `uk_user_auths_user_provider` (`user_id`, `provider`),
    CONSTRAINT `FK2q9eek3m3bt69r0xqua1u5i5b` FOREIGN KEY (`user_id`) REFERENCES `user_accounts` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_ranking_histories`
--

DROP TABLE IF EXISTS `user_ranking_histories`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_ranking_histories`
(
    `value`      int         NOT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `deleted_at` datetime(6) DEFAULT NULL,
    `history_id` bigint      NOT NULL AUTO_INCREMENT,
    `updated_at` datetime(6) DEFAULT NULL,
    `user_id`    bigint      NOT NULL,
    `criteria`   varchar(32) NOT NULL,
    PRIMARY KEY (`history_id`),
    KEY `FK4m72vk07uhanfvdwu8s70t5nd` (`user_id`),
    CONSTRAINT `FK4m72vk07uhanfvdwu8s70t5nd` FOREIGN KEY (`user_id`) REFERENCES `user_accounts` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 182
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_stats`
--

DROP TABLE IF EXISTS `user_stats`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_stats`
(
    `contribution_count` int    NOT NULL,
    `current_streak`     int    NOT NULL,
    `longest_streak`     int    NOT NULL,
    `rating`             int    NOT NULL,
    `rival_count`        int    NOT NULL,
    `solved_count`       int    NOT NULL,
    `submission_count`   int    NOT NULL,
    `top_problem_rating` int    NOT NULL,
    `created_at`         datetime(6) DEFAULT NULL,
    `deleted_at`         datetime(6) DEFAULT NULL,
    `updated_at`         datetime(6) DEFAULT NULL,
    `user_id`            bigint NOT NULL,
    PRIMARY KEY (`user_id`),
    CONSTRAINT `FKpq539u2m0wjjyfenqlhbm9qwn` FOREIGN KEY (`user_id`) REFERENCES `user_accounts` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `videos`
--

DROP TABLE IF EXISTS `videos`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `videos`
(
    `duration_seconds`  int          DEFAULT NULL,
    `created_at`        datetime(6)  DEFAULT NULL,
    `deleted_at`        datetime(6)  DEFAULT NULL,
    `file_size`         bigint      NOT NULL,
    `processed_at`      datetime(6)  DEFAULT NULL,
    `updated_at`        datetime(6)  DEFAULT NULL,
    `user_id`           bigint      NOT NULL,
    `video_id`          binary(16)  NOT NULL,
    `job_id`            varchar(256) DEFAULT NULL,
    `hls_cdn_url`       varchar(512) DEFAULT NULL,
    `hls_s3_url`        varchar(512) DEFAULT NULL,
    `original_s3_url`   varchar(512) DEFAULT NULL,
    `thumbnail_cdn_url` varchar(512) DEFAULT NULL,
    `thumbnail_s3_url`  varchar(512) DEFAULT NULL,
    `status`            varchar(16) NOT NULL,
    PRIMARY KEY (`video_id`),
    KEY `FKh8jbxao7gqgk3pj8xi07l1qkw` (`user_id`),
    CONSTRAINT `FKh8jbxao7gqgk3pj8xi07l1qkw` FOREIGN KEY (`user_id`) REFERENCES `user_accounts` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2025-08-13  3:38:26
