package com.tomasalmeida.dedu.permission.acls;

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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.permission.BindingProvider;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

public class AclBindingProvider implements BindingProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AclBindingProvider.class);

    private final KafkaAdminClient adminClient;

    public AclBindingProvider(final KafkaAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    @Override
    @NotNull
    public List<PermissionBinding> retrievePermissionsForPrincipal(@Nullable final String principal) throws ExecutionException,
            InterruptedException {
        final AclBindingFilter filter = buildAclFilter(principal);
        final DescribeAclsResult results = adminClient.describeAcls(filter);
        final Collection<AclBinding> aclBindings = results
                .values()
                .get();
        return buildPermissionBindingList(aclBindings);
    }

    @NotNull
    private AclBindingFilter buildAclFilter(@Nullable final String principal) {
        final AccessControlEntryFilter entryFilter = new AccessControlEntryFilter(principal, null, AclOperation.ANY, AclPermissionType.ANY);
        final AclBindingFilter aclBindingFilter = new AclBindingFilter(ResourcePatternFilter.ANY, entryFilter);
        LOGGER.debug("Filter for ACL for one principal is [{}]", aclBindingFilter);
        return aclBindingFilter;
    }

    @NotNull
    private List<PermissionBinding> buildPermissionBindingList(final Collection<AclBinding> aclBindings) {
        return aclBindings.stream()
                .map(AclPermissionBinding::new)
                .collect(Collectors.toList());
    }
}
