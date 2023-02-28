package com.tomasalmeida.dedu.api.kafka;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.acl.AclBindingFilter;

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

    public static KafkaAdminClient build(final Configuration configuration) throws IOException {
        final AdminClient adminClient = buildAdminFile(configuration);
        return new KafkaAdminClient(adminClient);
    }

    private static AdminClient buildAdminFile(final Configuration configuration) throws IOException {
        final Properties kafkaProperties = PropertiesLoader.loadFromFile(configuration.getKafkaConfig());
        return AdminClient.create(kafkaProperties);
    }

    /**
     * Close adminClient
     */
    @Override
    public void close() {
        synchronized (closed) {
            if (!closed) {
                adminClient.close();
                closed = true;
            }
        }
    }

    public DescribeAclsResult describeAcls(final AclBindingFilter filter) {
        return adminClient.describeAcls(filter);
    }
}
