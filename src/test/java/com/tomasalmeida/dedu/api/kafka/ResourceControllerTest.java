package com.tomasalmeida.dedu.api.kafka;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class ResourceControllerTest {

    private static final String TOPIC1 = "topic-name1";
    private static final String TOPIC2 = "topic-name2";
    private static final String TOPIC3 = "topic-name3";

    private ResourceController resourceController;

    @BeforeEach
    void beforeEach() {
        resourceController = new ResourceController();
    }

    @Test
    void shouldExistTopic() {
        givenTopicsAreAdded();

        assertFalse(resourceController.hasNoTopicInfo());
        assertTrue(resourceController.topicExists(TOPIC1));
        assertTrue(resourceController.topicExists(TOPIC2));
        assertFalse(resourceController.topicExists(TOPIC3));
    }

    @Test
    void shouldMatchTopicPrefix() {
        givenTopicsAreAdded();

        assertFalse(resourceController.hasNoTopicInfo());
        assertTrue(resourceController.topicPrefixMatches(TOPIC1));
        assertTrue(resourceController.topicPrefixMatches(TOPIC2));
        assertFalse(resourceController.topicPrefixMatches(TOPIC3));

    }

    private void givenTopicsAreAdded() {
        resourceController.addTopics(Set.of(TOPIC1, TOPIC2));
    }
}