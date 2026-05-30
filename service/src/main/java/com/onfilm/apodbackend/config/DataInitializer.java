package com.onfilm.apodbackend.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onfilm.apodbackend.model.Apod;
import com.onfilm.apodbackend.repository.ApodRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Initializes the database with APOD (Astronomy Picture of the Day) data from a JSON file
 * if the database is empty. This class runs once on application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ApodRepository apodRepository;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructs a new DataInitializer with the given ApodRepository and JdbcTemplate.
     *
     * @param apodRepository The repository for APOD data.
     * @param jdbcTemplate The JDBC template for batch updates.
     */
    public DataInitializer(ApodRepository apodRepository, JdbcTemplate jdbcTemplate) {
        this.apodRepository = apodRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Callback used to run the bean. This method is executed once the application context is loaded.
     * It checks if APOD data exists in the database and, if not, loads it from "apod_data.json"
     * using a JDBC batch update for efficiency.
     *
     * @param args Command line arguments (not used in this implementation).
     * @throws Exception if an error occurs during data loading.
     */
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (apodRepository.count() == 0) { // Still use repository to check if data exists
            try (InputStream inputStream = new ClassPathResource("apod_data.json").getInputStream()) {
                List<Apod> apods = objectMapper.readValue(inputStream, new TypeReference<List<Apod>>() {});

                String sql = "INSERT INTO APOD (date, title, credit, explanation, hdurl, service_version, copyright, media_type, url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Apod apod = apods.get(i);
                        ps.setObject(1, apod.getDate());
                        ps.setString(2, apod.getTitle());
                        ps.setString(3, apod.getCredit());
                        ps.setString(4, apod.getExplanation());
                        ps.setString(5, apod.getHdurl());
                        ps.setString(6, apod.getServiceVersion());
                        ps.setString(7, apod.getCopyright());
                        ps.setString(8, apod.getMediaType());
                        ps.setString(9, apod.getUrl());
                    }

                    @Override
                    public int getBatchSize() {
                        return apods.size();
                    }
                });

                System.out.println("Loaded " + apods.size() + " APOD entries from apod_data.json using JdbcTemplate batch update.");
            } catch (Exception e) {
                System.err.println("Error loading APOD data: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for more details
            }
        } else {
            System.out.println("APOD data already exists in the database. Skipping initialization.");
        }
    }
}