package com.adidas.poc;

import static org.springframework.integration.dsl.IntegrationFlows.from;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;

import org.aopalliance.aop.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.RouterSpec;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.router.MethodInvokingRouter;
import org.springframework.integration.store.MessageStore;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import com.adidas.poc.components.CSVToCollectionTransformer;
import com.adidas.poc.components.FileMoveHandler;
import com.adidas.poc.components.Neo4JHandler;
import com.adidas.poc.dto.UserDTO;

/**
 * Created by Oleh_Golovanov on 4/9/2015 for ADI-COM-trunk
 */
@SpringBootApplication
@EnableIntegration
@IntegrationComponentScan
public class UDIApplicationConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(UDIApplicationConfiguration.class);
    public static final int RETRY_ATTEMPTS = 5;
    public static final String FAIL_HEADER = "FAIL_HEADER";
    public static final int BACK_OFF_PERIOD = 5000;
    public static final String PROCESSED_CHANNEL = "processedChannel";
    public static final String UNPROCESSED_FAILED_CHANNEL = "unprocessed_FAILED_channel";


    public static final String PROCESSED_FILE_HEADER = "processed_file";


    @Value("${data.input.unprocessed.folder}")
    private String unprocessedDirectory;

    @Value("${data.input.processed.folder}")
    private String processedDirectory;

    @Value("${neo4j.url}")
    private String destinationURL;


    @Bean
    public IntegrationFlow mainFlow() {
        return IntegrationFlows
                .from(fileReadingMessageSource(), c -> c.poller(Pollers.fixedRate(1000)))
                .transform(csvToListTransformer())
                .split()
                .channel(asyncChannel())
                .handle((input, map) -> {
                    LOG.info("Split item {}", input);
                    return input;
                })
                .handle(neo4JHandler(), endpSpec -> endpSpec.advice(retryAdvice()))
                .handle((in, m) -> {
                    Object failHeader = m.get(FAIL_HEADER);
                    LOG.info("Processed item {}, failHeader {}", in, failHeader);
                    return in;
                })
                .aggregate()
                //.handle((p, h)->{Object failHeader = h.get(FAIL_HEADER);})
                .routeToRecipients(
                        (r) -> {
                            r.recipient(processdChannel(),m -> m.getHeaders().get(FAIL_HEADER) == null)
                                    .recipient(unprocessedChannel(), m -> m.getHeaders().get(FAIL_HEADER) != null).get();
                        })
                .get();
    }


    @Bean
    public IntegrationFlow successProcessedFlow() {
        return IntegrationFlows.from(processdChannel())
                .handle(fileMoveHandler())
                .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME)
                .get();
    }

    @Bean
    public IntegrationFlow failedProcessedFlow() {
        return IntegrationFlows.from(unprocessedChannel()).handle((payload, headers) -> {
            LOG.info("The {} file will not be moved due to some exceptions", headers.get(PROCESSED_FILE_HEADER));
            return payload;
        }).channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME).get();
    }

    private GenericHandler<Message<?>> fileMoveHandler() {
        return new FileMoveHandler(processedDirectory);
    }

    @Bean
    public Advice retryAdvice() {
        RequestHandlerRetryAdvice requestHandlerRetryAdvice = new RequestHandlerRetryAdvice();

        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(RETRY_ATTEMPTS);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(BACK_OFF_PERIOD);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(simpleRetryPolicy);
        requestHandlerRetryAdvice.setRecoveryCallback((retryCtx) -> {
            LOG.info("Invoked from recovery callback {}", retryCtx);
            return messageBuilderFactory().withPayload(Boolean.FALSE)
                    .setHeader(FAIL_HEADER, Boolean.TRUE).build();
        });

        requestHandlerRetryAdvice.setRetryTemplate(retryTemplate);

        return requestHandlerRetryAdvice;
    }

    @Bean
    public MessageBuilderFactory messageBuilderFactory() {
        return new DefaultMessageBuilderFactory();
    }


    @Bean
    public MessageStore messageStore() {
        return new SimpleMessageStore();
    }

    @Bean
    public GenericTransformer<File, Message<Collection<UserDTO>>> csvToListTransformer() {
        return new CSVToCollectionTransformer(messageBuilderFactory());
    }


    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }

    @Bean
    public Neo4JHandler neo4JHandler() {
        return new Neo4JHandler(restTemplate(), destinationURL);
    }

    @Bean
    public MessageChannel processdChannel() {
        return MessageChannels.direct(PROCESSED_CHANNEL).get();
    }

    @Bean
    public MessageChannel unprocessedChannel() {
        return MessageChannels.direct(UNPROCESSED_FAILED_CHANNEL).get();
    }

    @Bean
    public MessageChannel asyncChannel() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setConcurrencyLimit(2);
        return MessageChannels.executor(executor).get();
    }

    @Bean
    public MessageSource<File> fileReadingMessageSource() {
        FileReadingMessageSource fileReadingMessageSource = new FileReadingMessageSource();
        File directory = Paths.get(unprocessedDirectory).toFile();
        fileReadingMessageSource.setDirectory(directory);
        return fileReadingMessageSource;
    }


    @Bean
    public MessageSource<File> testMessageSource() {
        return () -> {
            return new Message<File>() {
                @Override
                public File getPayload() {
                    LOG.info("Quering payload from test message");
                    return Paths.get(unprocessedDirectory).toFile();
                }

                @Override
                public MessageHeaders getHeaders() {
                    return new MessageHeaders(null);
                }
            };
        };
    }


    @Bean
    public IntegrationFlow errorHadlerFlow() {
        return from(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME).handle(
                m -> System.out.println("ERROR HADLER FLOW\r\n" + m.getPayload())).get();
    }
}
