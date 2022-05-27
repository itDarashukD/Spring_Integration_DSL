package com.example.dsl.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PriorityChannel;
import org.springframework.integration.core.GenericSelector;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageHandlerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.messaging.MessageHandler;

import java.io.File;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;


@Configuration
public class GetSaveFile {

    private final String INPUT_DIRECTORY = "src\\main\\resources";
    private final String OUTPUT_DIRECTORY = "src\\main\\resources\\output";
    private final String OUTPUT_2_DIRECTORY = "src\\main\\resources\\output2";


    @Bean
    public IntegrationFlow upCaseFlow() {                            // получаем из канала что-то(String) и делаем верхний регистр
        return IntegrationFlows.from("input")
                .transform(string -> string.toString().toUpperCase())
                .get();
    }

    @Bean
    public MessageSource<File> sourceDirectory() {                         //read file from source directory
        FileReadingMessageSource messageSource = new FileReadingMessageSource();
        messageSource.setDirectory(new File(INPUT_DIRECTORY));
        return messageSource;
    }

    @Bean
    public GenericSelector<File> onlyJPG() {      //get only JPG files from source directory
        return new GenericSelector<File>() {
            @Override
            public boolean accept(File source) {
                return source.getName().endsWith(".jpg");
            }
        };
    }

    @Bean
    public GenericSelector<File> onlyTIFF() {      //get only JPG files from source directory
        return new GenericSelector<File>() {
            @Override
            public boolean accept(File source) {
                return source.getName().endsWith(".tiff");
            }
        };
    }

    /**
     * Now that we have a filtered list of files, we need to write them to a new location.
     * Service Activators are what we turn to when we're thinking about outputs in Spring Integration.
     */

    @Bean
    public MessageHandler targetDirectory() {
        FileWritingMessageHandler messageHandler = new FileWritingMessageHandler(new File(OUTPUT_DIRECTORY));
        messageHandler.setFileExistsMode(FileExistsMode.REPLACE);
        messageHandler.setExpectReply(false); //Because integration flows can be bidirectional, this invocation indicates that this particular pipe is one way.
        return messageHandler;
    }

    @Bean
    public MessageHandler targetSecondDirectory() {
        FileWritingMessageHandler messageHandler = new FileWritingMessageHandler(new File(OUTPUT_2_DIRECTORY));
        messageHandler.setFileExistsMode(FileExistsMode.REPLACE);
        messageHandler.setExpectReply(false); //Because integration flows can be bidirectional, this invocation indicates that this particular pipe is one way.
        return messageHandler;
    }

    /*create another channel*/
    @Bean
    public PriorityChannel alphabetically() {
        return new PriorityChannel(1000, Comparator.comparing(file -> ((File) file.getPayload()).getName()));
    }


    //pool every 1 second source directory to check for the file
    /*  Activating Our Integration Flow*/
    @Bean
    public IntegrationFlow fileMover() {
        return IntegrationFlows.from(sourceDirectory(), configurer -> configurer.poller((Pollers.fixedRate(1000)))) //from source
                .channel("alphabetically")   // sent to anther channel  alphabetically()
                .filter(onlyJPG())          //filtering
                .handle(targetDirectory())  //to source
                .get();                     //activate
    }

    /*Bridge
    *
    * Now, because we've simply written it to a channel, we can bridge from there to other flows.

Let's create a bridge that polls our holding tank for messages and writes them to a destination:
* we wrote to an intermediate channel, now we can add another flow that takes these same files and writes them at a different rate:
    * */

    @Bean
    public IntegrationFlow fileReader() {
        return IntegrationFlows.from(sourceDirectory()) //from source
                .filter(onlyJPG())
                .channel("holdingTank")  // sent to anther channel  alphabetically()
                .get();                     //activate
    }

    @Bean
    public IntegrationFlow fileWriter() {
        return IntegrationFlows.from("holdingTank")
                .bridge(e -> e.poller(Pollers.fixedRate(1, TimeUnit.SECONDS, 1)))
                .handle(targetDirectory())
                .get();
    }

    @Bean
    public IntegrationFlow anotherFileWriter() {
        return IntegrationFlows.from("holdingTank")
//                .bridge(e -> e.poller(Pollers.fixedRate(1, TimeUnit.SECONDS, 1)))
                .handle(anotherFileWriter())
                .get();
    }


}
