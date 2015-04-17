package com.adidas.poc.components;

import com.adidas.poc.dto.UserDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Oleh_Golovanov on 4/9/2015 for ADI-COM-trunk
 */
public class CSVToCollectionTransformer implements GenericTransformer<File, Message<Collection<UserDTO>>> {
    private static final Logger LOG = LoggerFactory.getLogger(CSVToCollectionTransformer.class);
    public static final String FIRST_NAME = "First Name";
    public static final String SECOND_NAME = "Second Name";
    public static final String EMAIL = "Email";
    public static final String NICK = "Nick";
    MessageBuilderFactory messageBuilderFactory;

    public CSVToCollectionTransformer(MessageBuilderFactory messageBuilderFactory) {
         this.messageBuilderFactory = messageBuilderFactory;
    }

    @Override
    public Message<Collection<UserDTO>> transform(File file){
        LOG.info("Starting to transform file {}", file);
        Collection<UserDTO> userDTOs = new ArrayList<>();
        try(Reader in = new FileReader(file)) {
            CSVParser records = CSVFormat.DEFAULT.withHeader(FIRST_NAME, SECOND_NAME, EMAIL, NICK).parse(in);
            boolean isHeader = true;
            for (CSVRecord record : records) {
                if(isHeader){
                    isHeader = false;
                    continue;
                }
                userDTOs.add(mapToUserDTO(record));
            }
        } catch (IOException e) {
            LOG.error("Exception happens during parsing SCV file ", e);
        }
        LOG.info("Transformation of file {} has been finished. Processed {} items", file, userDTOs.size());
        return messageBuilderFactory.withPayload(userDTOs).setHeader("processed_file", file.getAbsolutePath()).build();
    }

    private UserDTO mapToUserDTO(CSVRecord record) {
        String firstName = record.get(FIRST_NAME);
        String secondName = record.get(SECOND_NAME);
        String email = record.get(EMAIL);
        String nick = record.get(NICK);
        return new UserDTO(firstName, secondName,email,nick);
    }
}
