package com.tomasalmeida.dedu.permission.acls;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.tomasalmeida.dedu.Configuration;
import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.permission.BindingDeduplicator;
import com.tomasalmeida.dedu.permission.BindingProvider;
import com.tomasalmeida.dedu.permission.acls.modifiers.DeletedTopicBindingRule;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;


public class AclBindingDeduplicator extends BindingDeduplicator {

    private final KafkaAdminClient adminClient;
    private final Configuration configuration;

    public AclBindingDeduplicator(final KafkaAdminClient adminClient, final Configuration configuration) {
        this.adminClient = adminClient;
        this.configuration = configuration;
        addRules(adminClient);
    }

    void addRules(final KafkaAdminClient adminClient) {
        this.addRule(new DeletedTopicBindingRule(adminClient));
    }

    @Override
    protected List<PermissionBinding> getPermissionBindingsForUsers() throws ExecutionException, InterruptedException {
        final BindingProvider bindingProvider = new AclBindingProvider(adminClient);
        return bindingProvider.retrievePermissionsForPrincipal(configuration.getPrincipal());
    }
}
