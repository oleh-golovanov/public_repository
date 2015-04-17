package com.adidas.poc.components;

import com.adidas.poc.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by Oleh_Golovanov on 4/13/2015 for ADI-COM-trunk
 */
public class Neo4JHandler implements GenericHandler<UserDTO>{
    private static  final Logger LOG  = LoggerFactory.getLogger(Neo4JHandler.class);
    private final String destinationURL;
    private RestTemplate restTemplate;

    public Neo4JHandler(RestTemplate restTemplate, String destinationURL) {
        this.restTemplate = restTemplate;this.destinationURL = destinationURL;
    }


    @Override
    public Object handle(UserDTO dto, Map<String, Object> headers) {
        writeSingleUser(dto);
        return dto;
    }

    private void writeSingleUser(UserDTO v) {
        UserDTO userDto = v;
        try {
            LOG.info("Making rest request against {} with request body {}", destinationURL, userDto);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UserDTO> entity = new HttpEntity<>(userDto, headers);
            restTemplate.exchange(new URI(destinationURL), HttpMethod.POST, entity, String.class);
        } catch (URISyntaxException e) {
            LOG.error("URI syntax error", e);
        }
    }
}
