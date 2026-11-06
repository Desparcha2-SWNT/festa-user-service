package com.festa.user.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String AUTH_EXCHANGE = "festa.auth";
    public static final String USER_EXCHANGE = "festa.user";
    public static final String MODERATION_EXCHANGE = "festa.moderation";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        template.setMandatory(true);
        return template;
    }

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AUTH_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange moderationExchange() {
        return new TopicExchange(MODERATION_EXCHANGE, true, false);
    }

    @Bean
    public Declarables inboxTopology(AppProperties props) {
        String inbox = props.rabbit().inboxQueue();
        String retry = props.rabbit().retryQueue();
        String dlq = props.rabbit().dlqQueue();

        Queue inboxQueue = QueueBuilder.durable(inbox)
                .deadLetterExchange("")
                .deadLetterRoutingKey(retry)
                .build();

        Queue retryQueue = QueueBuilder.durable(retry)
                .ttl(30_000)
                .deadLetterExchange("")
                .deadLetterRoutingKey(inbox)
                .build();

        Queue dlqQueue = QueueBuilder.durable(dlq).build();

        return new Declarables(
                inboxQueue, retryQueue, dlqQueue,
                BindingBuilder.bind(inboxQueue).to(authExchange()).with("account.*"),
                BindingBuilder.bind(inboxQueue).to(moderationExchange()).with("moderation.*")
        );
    }
}