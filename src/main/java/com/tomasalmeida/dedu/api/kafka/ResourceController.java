package com.tomasalmeida.dedu.api.kafka;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAdminClient.class);

    private final SortedSet<String> topics = new TreeSet<>();

    public boolean hasNoTopicInfo() {
        return topics.isEmpty();
    }

    public void addTopics(@NotNull final Collection<String> topicNames) {
        topics.addAll(topicNames);
    }

    public boolean topicExists(@NotNull final String topicName) {
        final boolean topicExists = topics.contains(topicName);
        LOGGER.debug("Topic [{}] exists in resource controller? [{}]", topicName, topicExists);
        return topicExists;
    }

    public boolean topicPrefixMatches(@NotNull final String topicPrefix) {
        final boolean prefixMatches = topics.stream()
                .parallel()
                .anyMatch(topic -> topic.startsWith(topicPrefix));
        LOGGER.debug("Topic prefix [{}] has a match? [{}]", topicPrefix, prefixMatches);
        return prefixMatches;
    }

}
