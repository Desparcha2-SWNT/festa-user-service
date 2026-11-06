package com.festa.user.infrastructure.messaging;

import com.festa.user.config.RabbitConfig;
import com.festa.user.domain.port.out.EventPublisherPort;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventPublisherAdapter implements EventPublisherPort {

    private final RabbitTemplate rabbit;

    public RabbitEventPublisherAdapter(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @Override
    public void publish(String eventType, String payloadJson, String messageId) {
        rabbit.convertAndSend(
                RabbitConfig.USER_EXCHANGE,
                eventType,
                payloadJson,
                message -> {
                    var props = message.getMessageProperties();
                    props.setMessageId(messageId);
                    props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                    return message;
                }
        );
    }
}