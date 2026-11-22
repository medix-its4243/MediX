package com.medical.billing.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {
    @Bean NewTopic billingLookupReplies() { return TopicBuilder.name("billing.lookup.replies").partitions(1).replicas(1).build(); }
}
