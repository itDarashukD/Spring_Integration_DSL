//package com.example.dsl.example;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.integration.dsl.*;
//import org.springframework.integration.scheduling.PollerMetadata;
//import org.springframework.integration.transformer.PayloadSerializingTransformer;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.stereotype.Component;
//
//@Component
//public class DSLExaples {
//
//
//    //DSL Basics
//
//    @Bean
//    public IntegrationFlow integerFlow() {
//        return IntegrationFlows.from("input")
//                .<String, Integer>transform(Integer::parseInt)
//                .get();
//    }
//
//
//    @Bean
//    public IntegrationFlow integrationFlow() {
//        return IntegrationFlows.from("input")
//                .filter("Wordl"::equals)
//                .transform("Hello"::concat)
//                .handle(System.out::println)
//                .get();
//    }
//
//
////Message Channels
//
//
//    @Bean
//    public MessageChannel messageChannel() {
//        return MessageChannels.priority(this.mongoDBMessageStore, "Priority group")
//                .interceptor(wireTap())
//                .get();
//    }
//
//
//    @Bean
//    public MessageChannel queueChannel() {
//        return MessageChannels.queue().get();
//    }
//
//    @Bean
//    public MessageChannel publishSubscribe() {
//        return MessageChannels.publishSubscribe().get();
//    }
//
//    @Bean
//    public IntegrationFlow wireChannel() {
//        IntegrationFlows.from("input")
//                .fixedSubscriberChannel()
//                .channel("queueChannel")
//                .channel(publishSubscribe())
//                .channel(MessageChannels.executor("executorChanell", this.taskExecutor))
//                .channel("output")
//                .get();
//    }
///*from("input") means: 'find and use the MessageChannel with the "input" id, or create one';
//fixedSubscriberChannel() produces an instance of FixedSubscriberChannel and registers it with name channelFlow.channel#0;
//channel("queueChannel") works the same way but, of course, uses an existing "queueChannel" bean;
//channel(publishSubscribe()) - the bean-method reference;
//channel(MessageChannels.executor("executorChannel", this.taskExecutor)) the IntegrationFlowBuilder unwraps IntegrationComponentSpec to the ExecutorChannel and registers it as "executorChannel";
//channel("output") - registers the DirectChannel bean with "output" name as long as there are no beans with this name.*/
//
//
////    Pollers
//
//    @Bean(name = PollerMetadata.DEFAULT_POLLER)
//    public PollerMetadata poller() {
//        return Pollers.fixedRate(500).get();
//    }
//
////DSL and Endpoint Configuration
//
//    @Bean
//    public IntegrationFlow flow2() {
//        return IntegrationFlows.from(this.inputChannel)
//                .transform(new PayloadSerializingTransformer(),
//                        c -> c.autoStartup(false).id("payloadSerializingTransformer"))
//                .transform((Integer p) -> p * 2, c -> c.advice(this.expressionAdvice()))
//                .get();
//    }
//
//    //Transformers
//
//    @Bean
//    public IntegrationFlow transformFlow() {
//        return IntegrationFlows.from("input")
//                .transform(Transformers.xpath("/root/myJson", XPathEvaluationType.STRING_RESULT))
//                .transform(Transformers.fromJson(MyPojo.class))
//                .transform(Transformers.serializer())
//                .get();
//    }
//
//
//}
//
//
