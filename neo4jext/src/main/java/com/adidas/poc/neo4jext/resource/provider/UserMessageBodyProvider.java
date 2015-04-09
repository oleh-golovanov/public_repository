package com.adidas.poc.neo4jext.resource.provider;

import com.adidas.poc.neo4jext.domain.User;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by Oleh_Golovanov on 4/8/2015 for ADI-COM-trunk
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserMessageBodyProvider implements MessageBodyReader<User>, MessageBodyWriter<User> {
    private static final Logger LOG = LoggerFactory.getLogger(UserMessageBodyProvider.class);

    private ObjectMapper objectMapper;

    public UserMessageBodyProvider() {
        LOG.debug("New {} has been created", this.getClass().getSimpleName());
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public User readFrom(Class<User> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
        LOG.debug("readFrom has been invoked: class {}, type {}, annotations {}, mediaType {}, multyValuedMap {}", aClass, type, annotations, mediaType, multivaluedMap);
        User user;
        try {
            user = objectMapper.getJsonFactory().createJsonParser(inputStream).readValueAs(aClass);
        } catch (Exception e) {
            LOG.error("Exception happen", e);
            throw e;
        }
        return user;
    }

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        boolean result = aClass.equals(User.class);
        LOG.debug("isWriteAble called for class {}, type {}. Result is {}", aClass, type,  result);
        return result;
    }

    @Override
    public long getSize(User user, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        LOG.debug("getSize called");
        return -1;
    }

    @Override
    public void writeTo(User user, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        LOG.debug("writeTo readFrom has been invoked: class {}, type {}, annotations {}, mediaType {}, multyValuedMap {}", aClass, type, annotations, mediaType, multivaluedMap);
        try (JsonGenerator jsonGenerator = objectMapper.getJsonFactory().createJsonGenerator(outputStream, JsonEncoding.UTF8)) {
            jsonGenerator.writeObject(user);
        }
    }
}
