package com.climbx.climbx.common.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.climbx.climbx.common.service.S3Service;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractSchedulerIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("climbx_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    protected JdbcClient jdbcClient;

    @MockitoBean
    protected S3Client s3Client;

    @MockitoBean
    protected S3Presigner s3Presigner;

    @MockitoBean
    protected S3Service s3Service;

    protected int countRankingHistories(Long userId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM user_ranking_histories WHERE user_id = ?")
            .param(userId)
            .query(Integer.class)
            .single();
    }

    protected int countRankingHistoriesByCriteria(Long userId, String criteria) {
        return jdbcClient.sql("SELECT COUNT(*) FROM user_ranking_histories WHERE user_id = ? AND criteria = ?")
            .param(userId)
            .param(criteria)
            .query(Integer.class)
            .single();
    }

    protected int countOutboxEvents() {
        return jdbcClient.sql("SELECT COUNT(*) FROM outbox_events")
            .query(Integer.class)
            .single();
    }

    protected int countProcessedOutboxEvents() {
        return jdbcClient.sql("SELECT COUNT(*) FROM outbox_events WHERE processed = true")
            .query(Integer.class)
            .single();
    }

    protected int countUnprocessedOutboxEvents() {
        return jdbcClient.sql("SELECT COUNT(*) FROM outbox_events WHERE processed = false")
            .query(Integer.class)
            .single();
    }
}