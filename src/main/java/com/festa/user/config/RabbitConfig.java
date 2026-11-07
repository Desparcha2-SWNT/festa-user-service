package com.festa.user.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología de festa-infra/contracts/rabbitmq/definitions.json (festa-infra la carga al
 * arrancar RabbitMQ). Se declara con los MISMOS argumentos: si difieren, RabbitMQ rechaza la
 * declaración (PRECONDITION_FAILED) y el servicio no arranca. Sirve sin festa-infra (tests).
 */
@Configuration
public class RabbitConfig {

    public static final String AUTH_EXCHANGE = "festa.auth";
    public static final String USER_EXCHANGE = "festa.user";
    public static final String DLX = "festa.dlx";
    public static final int RETRY_TTL_MS = 10_000;

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMandatory(true);
        return template;
    }

    @Bean
    public Declarables messagingTopology(AppProperties props) {
        String inbox = props.rabbit().inboxQueue();
        String retry = props.rabbit().retryQueue();
        String dlq = props.rabbit().dlqQueue();

        TopicExchange auth = new TopicExchange(AUTH_EXCHANGE, true, false);
        TopicExchange user = new TopicExchange(USER_EXCHANGE, true, false);
        DirectExchange dlx = new DirectExchange(DLX, true, false);

        Queue inboxQueue = QueueBuilder.durable(inbox)
                .withArgument("x-queue-type", "classic")
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(dlq)
                .build();
        Queue retryQueue = QueueBuilder.durable(retry)
                .withArgument("x-queue-type", "classic")
                .ttl(RETRY_TTL_MS)
                .deadLetterExchange("")
                .deadLetterRoutingKey(inbox)
                .build();
        Queue dlqQueue = QueueBuilder.durable(dlq)
                .withArgument("x-queue-type", "classic")
                .build();

        return new Declarables(
                auth, user, dlx, inboxQueue, retryQueue, dlqQueue,
                BindingBuilder.bind(inboxQueue).to(auth).with("account.registered"),
                BindingBuilder.bind(inboxQueue).to(auth).with("account.deleted"),
                BindingBuilder.bind(dlqQueue).to(dlx).with(dlq)
        );
    }
}
