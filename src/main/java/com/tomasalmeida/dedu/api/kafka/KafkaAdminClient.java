package com.tomasalmeida.dedu.api.kafka;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.resource.ResourceType;
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
    private final ResourceCache cache;

    private Boolean closed = false;

    private KafkaAdminClient(final AdminClient adminClient) {
        this.adminClient = adminClient;
        this.cache = new ResourceCache();
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

    public boolean topicExists(@NotNull final String topicName) {
        Boolean topicExists = cache.isResourceResultCached(topicName, ResourceType.TOPIC);

        // if there is nothing in the cache, check Kafka and update cache
        if (topicExists == null) {
            topicExists = checkTopicExistsInKafka(topicName);
            cache.cacheResourceExists(topicName, ResourceType.TOPIC, topicExists);
        }
        return topicExists;
    }

    private boolean checkTopicExistsInKafka(@NotNull final String topicName) {
        try {
            LOGGER.debug("Searching if topic [{}] exists.", topicName);

            return adminClient
                    .describeTopics(List.of(topicName))
                    .allTopicNames()
                    .thenApply(map -> map.size() > 0)
                    .get();
        } catch (final ExecutionException | InterruptedException exception) {
            if (exception.getCause() instanceof UnknownTopicOrPartitionException) {
                LOGGER.debug("Topic [{}] was not found.", topicName, exception);
                return false;
            }
            throw new KafkaAdminException(exception);
        }
    }
}
