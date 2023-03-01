package com.tomasalmeida.dedu.permission.provider.acl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.permission.models.PermissionBinding;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class AclBindingProviderTest {

    private static final String PRINCIPAL = "principal";
    @Mock
    private KafkaAdminClient adminClient;
    @Mock
    private KafkaFuture<Collection<AclBinding>> futureAclBinding;
    @Mock
    private DescribeAclsResult describeAclsResult;
    @Mock
    private AclBinding aclBinding;

    private AclBindingProvider aclBindingProvider;
    private List<PermissionBinding> permissions;

    @Test
    void shouldReturnAclList() throws ExecutionException, InterruptedException {
        givenAdminReturnsDescribeAclsResult();
        givenFutureAclBinding();
        givenAclBindingProviderIsCreated();

        whenPermissionsFromPrincipalAreRequested();

        thenAclsAreRetrieved();
    }

    private void givenAclBindingProviderIsCreated() {
        aclBindingProvider = new AclBindingProvider(adminClient);
    }

    private void givenAdminReturnsDescribeAclsResult() {
        when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
        when(describeAclsResult.values()).thenReturn(futureAclBinding);
    }

    private void givenFutureAclBinding() throws ExecutionException, InterruptedException {
        when(futureAclBinding.get()).thenReturn(List.of(aclBinding));
    }

    private void whenPermissionsFromPrincipalAreRequested() throws ExecutionException, InterruptedException {
        permissions = aclBindingProvider.retrievePermissionsForPrincipal(PRINCIPAL);
    }

    private void thenAclsAreRetrieved() {
        assertEquals(1, permissions.size());
        assertEquals(aclBinding, permissions.get(0).getAclBinding());
    }
}