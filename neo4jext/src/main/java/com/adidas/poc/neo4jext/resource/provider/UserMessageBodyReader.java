package com.adidas.poc.neo4jext.resource.provider;

import com.adidas.poc.neo4jext.domain.User;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by Oleh_Golovanov on 4/8/2015 for ADI-COM-trunk
 */
//@Provider
//@Consumes(MediaType.APPLICATION_JSON)
public class UserMessageBodyReader implements MessageBodyReader<User> {
    private static final Logger LOG = LoggerFactory.getLogger(UserMessageBodyReader.class);

    private ObjectMapper objectMapper;

    public UserMessageBodyReader() {
        LOG.debug("New {} has been created", this.getClass().getSimpleName());
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public User readFrom(Class<User> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
        LOG.debug("Message body reader readFrom has been invoked: class {}, type {}, annotations {}, mediaType {}, multyValuedMap {}", aClass, type, annotations, mediaType, multivaluedMap);
        User user = null;
        try {
            user = objectMapper.getJsonFactory().createJsonParser(inputStream).readValueAs(aClass);
        } catch (Exception e) {
            LOG.error("Exception happen", e);
            throw e;
        }
        return user;
    }
}
