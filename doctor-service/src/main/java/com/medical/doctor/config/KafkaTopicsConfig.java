package com.medical.doctor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {
    @Bean NewTopic doctorLookupRequests() { return TopicBuilder.name("doctor.lookup.requests").partitions(1).replicas(1).build(); }
}
