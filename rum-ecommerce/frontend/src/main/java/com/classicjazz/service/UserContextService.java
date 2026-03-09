package com.classicjazz.service;

import com.classicjazz.model.UserContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists user context to a JSON file (array) for RUM/session tracking (e.g. Datadog RUM SDK).
 * Each signup or signin appends to the array so all users are preserved.
 */
@Service
public class UserContextService {

    private static final Logger log = LoggerFactory.getLogger(UserContextService.class);
    private static final TypeReference<List<UserContext>> LIST_TYPE = new TypeReference<>() {};

    private final Path filePath;
    private final ObjectMapper objectMapper;

    public UserContextService(
            @Value("${classicjazz.user-context.file:user_context.json}") String filePath) {
        this.filePath = Paths.get(filePath).toAbsolutePath().normalize();
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        if (!Files.exists(this.filePath)) {
            try {
                if (this.filePath.getParent() != null) {
                    Files.createDirectories(this.filePath.getParent());
                }
                objectMapper.writeValue(this.filePath.toFile(), new ArrayList<>());
                log.info("Created empty user_context.json at {}", this.filePath);
            } catch (IOException e) {
                log.warn("Failed to create user_context.json at {}: {}", this.filePath, e.getMessage());
            }
        }
    }

    /**
     * Append a user to user_context.json. Skips duplicates by id (if id is non-null).
     */
    public synchronized void writeUserContext(String id, String name, String email) {
        if (id == null && name == null && email == null) return;
        try {
            List<UserContext> users = readExisting();
            if (id != null && users.stream().anyMatch(u -> id.equals(u.id()))) {
                log.debug("User {} already in user_context.json, skipping", id);
                return;
            }
            users.add(new UserContext(id, name, email));
            objectMapper.writeValue(this.filePath.toFile(), users);
            log.debug("Appended user context to {}: id={}, name={}", this.filePath, id, name);
        } catch (IOException e) {
            log.warn("Failed to write user context to {}: {}", this.filePath, e.getMessage());
        }
    }

    private List<UserContext> readExisting() {
        try {
            if (Files.exists(this.filePath) && Files.size(this.filePath) > 0) {
                return new ArrayList<>(objectMapper.readValue(this.filePath.toFile(), LIST_TYPE));
            }
        } catch (IOException e) {
            log.warn("Failed to read existing user_context.json, starting fresh: {}", e.getMessage());
        }
        return new ArrayList<>();
    }
}
