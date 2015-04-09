package com.adidas.poc.neo4jext.resource.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.*;
import java.io.IOException;

/**
 * Created by Oleh_Golovanov on 4/9/2015 for ADI-COM-trunk
 */
public class ReadInterceptor implements ReaderInterceptor{
    private static final Logger LOG = LoggerFactory.getLogger(ReadInterceptor.class);

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext readerInterceptorContext) throws IOException, WebApplicationException {
        try {
            LOG.debug("Inside Write interceptor");
            return readerInterceptorContext.proceed();
        } catch (Throwable e){
            LOG.error("ReadInterceptor detected exception", e);
            throw  e;
        }
    }
}
