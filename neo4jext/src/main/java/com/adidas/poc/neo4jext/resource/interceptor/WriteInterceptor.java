package com.adidas.poc.neo4jext.resource.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;

/**
 * Created by Oleh_Golovanov on 4/9/2015 for ADI-COM-trunk
 */
public class WriteInterceptor implements WriterInterceptor{
    private static final Logger LOG = LoggerFactory.getLogger(WriteInterceptor.class);
    @Override
    public void aroundWriteTo(WriterInterceptorContext writerInterceptorContext) throws IOException, WebApplicationException {

        try {
            LOG.debug("Inside Write interceptor");
            writerInterceptorContext.proceed();
        } catch (Throwable e){
            LOG.error("WriteInterceptor detected exception", e);
            throw  e;
        }
    }
}
