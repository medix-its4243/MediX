package com.medical.record.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {
    @Bean NewTopic recordLookupReplies() { return TopicBuilder.name("record.lookup.replies").partitions(1).replicas(1).build(); }
}
