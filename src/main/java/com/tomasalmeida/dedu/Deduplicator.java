package com.tomasalmeida.dedu;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.configuration.MainConfiguration;
import com.tomasalmeida.dedu.permission.acls.AclBindingDeduplicator;

/**
 * Deduplicator coordinator
 */
public class Deduplicator implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Deduplicator.class);

    private final MainConfiguration mainConfiguration;
    private final KafkaAdminClient adminClient;

    private Deduplicator(final KafkaAdminClient adminClient, final MainConfiguration mainConfiguration) {
        this.adminClient = adminClient;
        this.mainConfiguration = mainConfiguration;
    }

    @NotNull
    public static Deduplicator build(@NotNull final String kafkaConfigPath,
                                     @NotNull final String deduConfigPath,
                                     @Nullable final String principal) throws IOException {
        final MainConfiguration mainConfiguration = MainConfiguration.build(kafkaConfigPath, deduConfigPath, principal);
        final KafkaAdminClient kafkaAdminClient = KafkaAdminClient.build(mainConfiguration);
        return new Deduplicator(kafkaAdminClient, mainConfiguration);
    }

    @Override
    public void close() {
        adminClient.close();
    }

    public void run() throws ExecutionException, InterruptedException {
        modifyAclsBindings();
    }

    private void modifyAclsBindings() throws ExecutionException, InterruptedException {
        LOGGER.debug("Running ACL deduplicator");
        final AclBindingDeduplicator aclBindingDeduplicator = AclBindingDeduplicator.build(adminClient, mainConfiguration);
        aclBindingDeduplicator.run();

        LOGGER.debug("TODO Running RBAC deduplicator");
        // add rbacBindingDeduplicator in the future
    }
}
