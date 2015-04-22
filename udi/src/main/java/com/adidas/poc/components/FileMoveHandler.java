package com.adidas.poc.components;

import com.adidas.poc.UDIApplicationConfiguration;
import com.adidas.poc.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.messaging.Message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Oleh_Golovanov on 4/17/2015
 */
public class FileMoveHandler implements GenericHandler<Message<?>> {
    private static final Logger LOG = LoggerFactory.getLogger(FileMoveHandler.class);
    private String processedDirectory;

    public FileMoveHandler(String processedDirectory) {
        this.processedDirectory = processedDirectory;
    }

    @Override
    public Object handle(Message<?> payload, Map<String, Object> headers) {
        Object failHeader = headers.get(UDIApplicationConfiguration.FAIL_HEADER);
        if (failHeader != null) {
            LOG.warn("One or more items failed to be written. Skip moving.");
        } else {
            String fileToMove = headers.get("processed_file").toString();
            LOG.info("Aggregated input {}", payload);
            LOG.info("File to be moved {}", fileToMove);
            Path sourceFilePath = Paths.get(fileToMove);
            String fileName = sourceFilePath.getFileName().toString();
            try {
                Files.move(sourceFilePath, Paths.get(processedDirectory, fileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOG.error("Unable to move processed file", e);
            }
        }
        return payload;
    }
}
