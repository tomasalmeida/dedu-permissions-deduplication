package com.tomasalmeida.dedu.api.kafka;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomasalmeida.dedu.configuration.MainConfiguration;

/**
 * Kafka Admin Client Wrapper
 */
public class KafkaAdminClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAdminClient.class);

    private final AdminClient adminClient;
    private final ResourceController resourceController;

    private Boolean closed = false;

    private KafkaAdminClient(final AdminClient adminClient) {
        this.adminClient = adminClient;
        this.resourceController = new ResourceController();
    }

    @NotNull
    public static KafkaAdminClient build(@NotNull final MainConfiguration mainConfiguration) throws IOException {
        final Properties kafkaProperties = mainConfiguration.getKafkaConfigProperties();
        final AdminClient adminClient = AdminClient.create(kafkaProperties);
        return new KafkaAdminClient(adminClient);
    }

    /**
     * Close adminClient
     */
    @Override
    public void close() {
        synchronized (adminClient) {
            if (!closed) {
                adminClient.close();
                closed = true;
            }
        }
    }

    @NotNull
    public DescribeAclsResult describeAcls(@NotNull final AclBindingFilter filter) {
        return adminClient.describeAcls(filter);
    }

    public boolean isTopicPresent(@NotNull final String topicName) {
        fillTopicDataIfNeeded();
        return resourceController.topicExists(topicName);
    }

    public boolean doesTopicMatches(@NotNull final String topicNamePrefix) {
        fillTopicDataIfNeeded();
        return resourceController.topicPrefixMatches(topicNamePrefix);
    }

    private void fillTopicDataIfNeeded() {
        if (resourceController.hasNoTopicInfo()) {
            try {
                final Set<String> topicNames = adminClient.listTopics()
                        .names()
                        .get();
                resourceController.addTopics(topicNames);
            } catch (final InterruptedException | ExecutionException e) {
                LOGGER.error("Unable to get topic names.", e);
                throw new KafkaAdminException(e);
            }
        }
    }
}
