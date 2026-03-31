package com.donttaz.donttazapi.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PENALTY_EXCHANGE = "penalty.exchange";
    public static final String PENALTY_QUEUE = "penalty.webhook.queue";
    public static final String PENALTY_ROUTING_KEY = "penalty.routing.key";

    @Bean
    public DirectExchange penaltyExchange() {
        return new DirectExchange(PENALTY_EXCHANGE);
    }

    @Bean
    public Queue penaltyQueue() {
        return new Queue(PENALTY_QUEUE);
    }

    @Bean
    public Binding penaltyBinding(Queue penaltyQueue, DirectExchange penaltyExchange) {
        return BindingBuilder.bind(penaltyQueue).to(penaltyExchange).with(PENALTY_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
