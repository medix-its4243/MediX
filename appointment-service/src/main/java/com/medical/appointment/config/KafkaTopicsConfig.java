package com.medical.appointment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {
    @Bean NewTopic appointmentLookupRequests() { return TopicBuilder.name("appointment.lookup.requests").partitions(1).replicas(1).build(); }
    @Bean NewTopic appointmentLookupReplies() { return TopicBuilder.name("appointment.lookup.replies").partitions(1).replicas(1).build(); }
}
