package com.tomasalmeida.dedu.permission.acls;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.configuration.MainConfiguration;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;
import com.tomasalmeida.dedu.permission.modifier.BindingDeletionRule;
import com.tomasalmeida.dedu.permission.modifier.BindingTransformationRule;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class AclBindingDeduplicatorTest {

    public static final String PRINCIPAL = "principal";
    @Mock
    private KafkaAdminClient adminClient;
    @Mock
    private MainConfiguration mainConfiguration;
    @Mock
    private BindingDeletionRule deletionRule;
    @Mock
    private BindingTransformationRule transformationRule;
    @Mock
    private ActionablePermissionBinding actionableDeletedPermission1;
    @Mock
    private ActionablePermissionBinding actionableDeletedPermission2;
    @Mock
    private ActionablePermissionBinding actionableTransfPermission;

    private AclBindingDeduplicator aclBindingDeduplicator;

    @Test
    void shouldRunRules() throws Exception {
        givenAclBindingDeduplicatorIsCreated();
        givenDeletionRuleChangeOneElement();
        givenTransformationRuleChangeOneElement();

        whenDeduplicatorRuns();

        thenRulesAreLaunched();
    }

    @Test
    void shouldGetPermissions() throws Exception {
        when(mainConfiguration.getPrincipal()).thenReturn(PRINCIPAL);
        try (final MockedConstruction<AclBindingProvider> mockConstruction = mockConstruction(AclBindingProvider.class)) {
            aclBindingDeduplicator = new AclBindingDeduplicator(adminClient, mainConfiguration);

            aclBindingDeduplicator.getPermissionBindingsForUsers();

            final List<AclBindingProvider> construced = mockConstruction.constructed();
            verify(mainConfiguration).getPrincipal();
            verify(construced.get(0)).retrievePermissionsForPrincipal(PRINCIPAL);
        }
    }

    private void givenDeletionRuleChangeOneElement() {
        Mockito.doAnswer(invocation -> {
            final List<ActionablePermissionBinding> actionablePermissions = invocation.getArgument(1);
            actionablePermissions.add(actionableDeletedPermission1);
            return null;
        }).when(deletionRule).run(anyList(), anyList());
    }

    private void givenTransformationRuleChangeOneElement() {
        Mockito.doAnswer(invocation -> {
            final List<ActionablePermissionBinding> actionableAddedPermissions = invocation.getArgument(1);
            final List<ActionablePermissionBinding> actionableDeletedPermissions = invocation.getArgument(2);
            actionableAddedPermissions.add(actionableDeletedPermission2);
            actionableDeletedPermissions.add(actionableTransfPermission);
            return null;
        }).when(transformationRule).run(anyList(), anyList(), anyList());
    }

    private void givenAclBindingDeduplicatorIsCreated() {
        aclBindingDeduplicator = new AclBindingDeduplicator(adminClient, mainConfiguration) {

            @Override
            void addRules(final KafkaAdminClient adminClient) {
                this.addRule(deletionRule);
                this.addRule(transformationRule);
            }

            @Override
            protected List<PermissionBinding> getPermissionBindingsForUsers() {
                return List.of();
            }
        };
    }

    private void whenDeduplicatorRuns() throws ExecutionException, InterruptedException {
        aclBindingDeduplicator.run();
    }

    private void thenRulesAreLaunched() {
        verify(deletionRule).run(anyList(), anyList());
        verify(transformationRule).run(anyList(), anyList(), anyList());
    }
}