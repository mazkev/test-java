package com.belajar.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "order.exchange";
    public static final String QUEUE_NAME = "order.invoice.queue";
    public static final String ROUTING_KEY = "order.created";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue orderInvoiceQueue() {
        // durable = true agar pesan tidak hilang walau RabbitMQ restart
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding binding(Queue orderInvoiceQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderInvoiceQueue).to(orderExchange).with(ROUTING_KEY);
    }

    // Mengubah payload objek Java otomatis menjadi JSON saat dikirim ke RabbitMQ
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}