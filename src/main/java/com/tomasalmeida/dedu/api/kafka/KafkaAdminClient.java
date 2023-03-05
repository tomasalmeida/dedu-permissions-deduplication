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
import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.Configuration;
import com.tomasalmeida.dedu.api.system.PropertiesLoader;

/**
 * Kafka Admin Client Wrapper
 */
public class KafkaAdminClient implements Closeable {

    private final AdminClient adminClient;
    private Boolean closed = false;

    private KafkaAdminClient(final AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    @NotNull
    public static KafkaAdminClient build(@NotNull final Configuration configuration) throws IOException {
        final Properties kafkaProperties = PropertiesLoader.loadFromFile(configuration.getKafkaConfig());
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

    public DescribeAclsResult describeAcls(final AclBindingFilter filter) {
        return adminClient.describeAcls(filter);
    }

    public boolean topicExists(final String topicName) {
        try {
            return adminClient
                    .describeTopics(List.of(topicName))
                    .allTopicNames()
                    .thenApply(map -> map.size() > 0)
                    .get();
        } catch (final ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                return false;
            }
            throw new KafkaAdminException(e);
        }
    }
}
