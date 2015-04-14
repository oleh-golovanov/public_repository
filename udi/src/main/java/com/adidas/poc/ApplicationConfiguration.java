package com.adidas.poc;

import com.adidas.poc.dto.UserDTO;
import com.adidas.poc.intelements.CSVToCollectionTransformer;
import com.adidas.poc.intelements.Neo4JHandler;
import org.aopalliance.aop.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.store.MessageStore;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Map;

import static org.springframework.integration.dsl.IntegrationFlows.from;

/**
 * Created by Oleh_Golovanov on 4/9/2015 for ADI-COM-trunk
 */
@SpringBootApplication
@EnableIntegration
@IntegrationComponentScan
public class ApplicationConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfiguration.class);
    public static final int RETRY_ATTEMPTS = 3;
    public static final String FAIL_HEADER = "FAIL_HEADER";


    @Value("${data.input.unprocessed.folder}")
    private String unprocessedDirectory;

    @Value("${data.input.processed.folder}")
    private String processedDirectory;

    @Value("${neo4j.url}")
    private String destinationURL;


    @Bean
    public IntegrationFlow mainFlow() {
        return IntegrationFlows.from(fileReadingMessageSource(), c -> c.poller(Pollers.fixedRate(1000)))
                //.transform((file)->{return messageBuilderFactory().withPayload(file).setHeader("destinationURL", destinationURL).build();})
                .transform(csvToListTransformer())
                .split()
                .handle((input, map)->{LOG.info("Splitted item {}", input); return input;})
                .handle(neo4JHandler(), endpSpec -> endpSpec.advice(retryAdvice()))
                .handle((in, m) -> {
                    Object failHeader = m.get(FAIL_HEADER);
                    LOG.info("Processed item {}", in);
                    return in;
                })
                .aggregate()
                .handle((Object input, Map<String, Object> map) -> {
                    Object failHeader = map.get(FAIL_HEADER);
                    if(failHeader != null){
                        LOG.error("One or more items failed to be written. Skip moving.");
                    } else {
                        String fileToMove = map.get("processed_file").toString();
                        LOG.info("Aggregated input {}", input);
                        LOG.info("File to be moved {}", fileToMove);
                        Path sourceFilePath = Paths.get(fileToMove);
                        String fileName = sourceFilePath.getFileName().toString();
                        try {
                            Files.move(sourceFilePath, Paths.get(processedDirectory, fileName), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            LOG.error("Unable to move procesed file", e);
                        }
                    }
                    return input;
                })
                .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME)
                .get();
    }

    @Bean
    public Advice retryAdvice() {
        RequestHandlerRetryAdvice requestHandlerRetryAdvice = new RequestHandlerRetryAdvice();

        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(RETRY_ATTEMPTS);
        RetryTemplate  retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(simpleRetryPolicy);
        requestHandlerRetryAdvice.setRecoveryCallback((retryCtx)->{LOG.info("Invoked from recovery callback {}", retryCtx); return  messageBuilderFactory().withPayload(new UserDTO()).setHeader(FAIL_HEADER, Boolean.TRUE).build();});

        requestHandlerRetryAdvice.setRetryTemplate(retryTemplate);

        return requestHandlerRetryAdvice;
    }

    @Bean
    public MessageBuilderFactory messageBuilderFactory(){
        return  new DefaultMessageBuilderFactory();
    }


    @Bean
    public MessageStore messageStore(){
        return new SimpleMessageStore();
    }

    @Bean
    public GenericTransformer<File, Message<Collection<UserDTO>>> csvToListTransformer() {
        return new CSVToCollectionTransformer(messageBuilderFactory());
    }


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Neo4JHandler neo4JHandler(){
        return  new Neo4JHandler(restTemplate(), destinationURL);
    }

    @Bean
    public MessageChannel outChannel() {
        return new DirectChannel();
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
        return from(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME)
                .handle(m -> System.out.println("ERROR HADLER FLOW\r\n" + m.getPayload()))
                .get();
    }
}
