package com.tomasalmeida.dedu.permission.acls;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.configuration.MainConfiguration;
import com.tomasalmeida.dedu.permission.BindingDeduplicator;
import com.tomasalmeida.dedu.permission.BindingProvider;
import com.tomasalmeida.dedu.permission.acls.modifiers.DeletedTopicBindingRule;
import com.tomasalmeida.dedu.permission.acls.printers.CsvAclPrinter;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;
import com.tomasalmeida.dedu.permission.printers.DebugLogPrinter;

public class AclBindingDeduplicator extends BindingDeduplicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AclBindingDeduplicator.class);

    private final KafkaAdminClient adminClient;
    private final MainConfiguration mainConfiguration;

    @VisibleForTesting
    AclBindingDeduplicator(@NotNull final KafkaAdminClient adminClient, @NotNull final MainConfiguration mainConfiguration) {
        super("aclBindingDeduplicator");
        this.adminClient = adminClient;
        this.mainConfiguration = mainConfiguration;
        addRules(adminClient);
        addPrinters(mainConfiguration);
    }

    public static AclBindingDeduplicator build(@NotNull final KafkaAdminClient adminClient,
                                               @NotNull final MainConfiguration mainConfiguration) {
        return new AclBindingDeduplicator(adminClient, mainConfiguration);
    }

    private void addPrinters(@NotNull final MainConfiguration mainConfiguration) {
        this.addPrinter(new DebugLogPrinter(mainConfiguration));
        this.addPrinter(new CsvAclPrinter(mainConfiguration));
    }

    @VisibleForTesting
    void addRules(@NotNull final KafkaAdminClient adminClient) {
        this.addRule(new DeletedTopicBindingRule(adminClient));
    }

    @Override
    protected List<PermissionBinding> getPermissionBindingsForUsers() throws ExecutionException, InterruptedException {
        final String principal = mainConfiguration.getPrincipal();

        final BindingProvider bindingProvider = new AclBindingProvider(adminClient);
        final List<PermissionBinding> permissionBindings = bindingProvider.retrievePermissionsForPrincipal(principal);

        LOGGER.debug("Permissions for principal [{}] are {}.", principal, permissionBindings);
        return permissionBindings;
    }
}
