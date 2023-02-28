package com.tomasalmeida.dedu;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.permission.provider.BindingProvider;
import com.tomasalmeida.dedu.permission.provider.acl.AclBindingProvider;

/**
 * Deduplicator coordinator
 */
public class Deduplicator implements AutoCloseable {

    private final Configuration configuration;
    private final KafkaAdminClient adminClient;
    private final BindingProvider bindingProvider;

    private Deduplicator(final KafkaAdminClient adminClient, final Configuration configuration, final BindingProvider bindingProvider) {
        this.adminClient = adminClient;
        this.configuration = configuration;
        this.bindingProvider = bindingProvider;
    }

    public static Deduplicator build(final String configFile, final String principal) throws IOException {
        final Configuration configuration = new Configuration(configFile, principal);
        final KafkaAdminClient kafkaAdminClient = KafkaAdminClient.build(configuration);
        final BindingProvider bindingProvider = new AclBindingProvider(kafkaAdminClient);
        return new Deduplicator(kafkaAdminClient, configuration, bindingProvider);
    }

    @Override
    public void close() throws IOException {
        adminClient.close();
    }

    public void run() throws ExecutionException, InterruptedException {
        bindingProvider.retrievePermissionsForPrincipal(configuration.getPrincipal())
                .forEach(System.out::println);

    }
}
