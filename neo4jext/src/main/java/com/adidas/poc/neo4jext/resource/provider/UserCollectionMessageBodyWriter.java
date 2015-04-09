package com.adidas.poc.neo4jext.resource.provider;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Created by Oleh_Golovanov on 4/8/2015 for ADI-COM-trunk
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class UserCollectionMessageBodyWriter implements MessageBodyWriter<Collection<?>> {
    private static final Logger LOG = LoggerFactory.getLogger(UserCollectionMessageBodyWriter.class);
    private ObjectMapper objectMapper;

    public UserCollectionMessageBodyWriter() {
        this.objectMapper = new ObjectMapper();
        LOG.debug("New {} has been created", this.getClass().getSimpleName());
    }

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        boolean result = Collection.class.isAssignableFrom(aClass);
        LOG.debug("isWriteAble called for class {}, type {}. Result is {}", aClass, type, result);
        return result;
    }

    @Override
    public long getSize(Collection<?> user, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        LOG.debug("UserMessageBodyWriter getSize called");
        return -1;
    }

    @Override
    public void writeTo(Collection<?> user, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        LOG.debug("Message body writeTo readFrom has been invoked: class {}, type {}, annotations {}, mediaType {}, multyValuedMap {}", aClass, type, annotations, mediaType, multivaluedMap);
        try (JsonGenerator jsonGenerator = objectMapper.getJsonFactory().createJsonGenerator(outputStream, JsonEncoding.UTF8)) {
            jsonGenerator.writeObject(user);
        }
    }
}
