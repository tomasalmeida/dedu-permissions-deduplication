package com.tomasalmeida.dedu;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.permission.acls.AclBindingDeduplicator;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;

/**
 * Deduplicator coordinator
 */
public class Deduplicator implements AutoCloseable {

    private final Configuration configuration;
    private final KafkaAdminClient adminClient;

    private Deduplicator(final KafkaAdminClient adminClient, final Configuration configuration) {
        this.adminClient = adminClient;
        this.configuration = configuration;
    }

    @NotNull
    public static Deduplicator build(final String configFile, final String principal) throws IOException {
        final Configuration configuration = new Configuration(configFile, principal);
        final KafkaAdminClient kafkaAdminClient = KafkaAdminClient.build(configuration);
        return new Deduplicator(kafkaAdminClient, configuration);
    }

    @Override
    public void close() {
        adminClient.close();
    }

    public void run() throws ExecutionException, InterruptedException {
        modifyAclsBindings();
    }

    private void modifyAclsBindings() throws ExecutionException, InterruptedException {
        final AclBindingDeduplicator aclBindingDeduplicator = new AclBindingDeduplicator(adminClient, configuration);

        final List<ActionablePermissionBinding> newPermissions = aclBindingDeduplicator.run();

        newPermissions
                .forEach(System.out::println);
    }
}
