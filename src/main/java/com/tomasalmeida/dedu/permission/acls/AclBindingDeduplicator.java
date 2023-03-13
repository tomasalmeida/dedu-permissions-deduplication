package com.tomasalmeida.dedu.permission.acls;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.configuration.MainConfiguration;
import com.tomasalmeida.dedu.permission.BindingDeduplicator;
import com.tomasalmeida.dedu.permission.BindingProvider;
import com.tomasalmeida.dedu.permission.acls.modifiers.DeletedTopicBindingRule;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

public class AclBindingDeduplicator extends BindingDeduplicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AclBindingDeduplicator.class);

    private final KafkaAdminClient adminClient;
    private final MainConfiguration mainConfiguration;

    public AclBindingDeduplicator(final KafkaAdminClient adminClient, final MainConfiguration mainConfiguration) {
        super("aclBindingDeduplicator");
        this.adminClient = adminClient;
        this.mainConfiguration = mainConfiguration;
        addRules(adminClient);
    }

    void addRules(final KafkaAdminClient adminClient) {
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
