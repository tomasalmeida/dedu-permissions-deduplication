package com.tomasalmeida.dedu.permission.provider.acl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.permission.models.PermissionBinding;
import com.tomasalmeida.dedu.permission.provider.BindingProvider;

public class AclBindingProvider implements BindingProvider {

    private final KafkaAdminClient adminClient;

    public AclBindingProvider(final KafkaAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    @Override
    public List<PermissionBinding> retrievePermissionsForPrincipal(final String principal) throws ExecutionException, InterruptedException {
        final AclBindingFilter filter = buildAclFilter(principal);
        final DescribeAclsResult results = adminClient.describeAcls(filter);
        final Collection<AclBinding> aclBindings = results.values().get();
        return buildPermissionBindingList(aclBindings);
    }

    @NotNull
    private AclBindingFilter buildAclFilter(final String principal) {
        final AccessControlEntryFilter entryFilter = new AccessControlEntryFilter(principal, null, AclOperation.ANY, AclPermissionType.ANY);
        return new AclBindingFilter(ResourcePatternFilter.ANY, entryFilter);
    }

    @NotNull
    private List<PermissionBinding> buildPermissionBindingList(final Collection<AclBinding> aclBindings) {
        return aclBindings.stream()
                .map(PermissionBinding::new)
                .collect(Collectors.toList());
    }


}
