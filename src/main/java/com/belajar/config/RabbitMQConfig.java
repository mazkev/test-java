package com.belajar.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "order.exchange";
    public static final String QUEUE_NAME = "order.invoice.queue";
    public static final String ROUTING_KEY = "order.created";

    // Fase 15: Dead Letter Exchange (DLX) & Dead Letter Queue (DLQ)
    public static final String DLX_EXCHANGE_NAME = "order.dlx.exchange";
    public static final String DLQ_QUEUE_NAME = "order.invoice.dlq";
    public static final String DLQ_ROUTING_KEY = "order.invoice.dlq.key";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE_NAME);
    }

    // Antrean Utama: Jika gagal 3x, RabbitMQ otomatis melemparkan ke DLX
    @Bean
    public Queue orderInvoiceQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    // Antrean DLQ: Menyimpan pesan gagal secara permanen agar zero data loss
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE_NAME).build();
    }

    @Bean
    public Binding binding(Queue orderInvoiceQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderInvoiceQueue).to(orderExchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding dlqBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ_ROUTING_KEY);
    }

    // Converter otomatis objek Java <-> JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Pasang converter ke RabbitTemplate pengirim
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
