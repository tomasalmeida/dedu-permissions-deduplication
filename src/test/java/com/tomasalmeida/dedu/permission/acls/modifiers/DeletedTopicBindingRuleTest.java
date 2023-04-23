package com.tomasalmeida.dedu.permission.acls.modifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.com.tomasalmeida.tests.acls.AclPermissionCreator;
import com.tomasalmeida.dedu.permission.acls.AclPermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;
import com.tomasalmeida.dedu.permission.context.ContextExecution;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class DeletedTopicBindingRuleTest {

    private static final String TOPIC_NAME = "topic-topic";
    private static final String PRINCIPAL_NAME = "principal";
    private static final String HOST = "*";

    @Mock
    KafkaAdminClient adminClient;

    private DeletedTopicBindingRule deletedTopicBindingModifier;
    private List<PermissionBinding> originalPermissions;
    private final List<ActionablePermissionBinding> actionablePermissions = new ArrayList<>();

    @Test
    void shouldDeleteObsoleteTopicBinding() {
        givenDeletedTopicBindingModifier();
        givenPermissionListIsFulfilled();
        givenTopicExistsReturns(false);

        whenModifierRuns();

        thenItemIsRemoved();
    }

    @Test
    void shouldKeepValidTopicBinding() {
        givenDeletedTopicBindingModifier();
        givenPermissionListIsFulfilled();
        givenTopicExistsReturns(true);

        whenModifierRuns();

        thenActionableItemIsEmpty();
    }

    @Test
    void shouldKeepValidTopicPatternBinding() {
        givenDeletedTopicBindingModifier();
        givenPermissionListIsFulfilledWithPattern();
        givenTopicMatchesReturns(true);

        whenModifierRuns();

        thenActionableItemIsEmpty();
    }

    @Test
    void shouldDeleteObsoleteTopicPatternBinding() {
        givenDeletedTopicBindingModifier();
        givenPermissionListIsFulfilledWithPattern();
        givenTopicMatchesReturns(false);

        whenModifierRuns();

        thenItemIsRemoved();
    }

    @Test
    void shouldKeepOtherBinding() {
        givenDeletedTopicBindingModifier();
        givenPermissionListIsFulfilledWithAnotherPermission();

        whenModifierRuns();

        thenActionableItemIsEmpty();
    }

    private void givenTopicExistsReturns(final boolean exists) {
        when(adminClient.isTopicPresent(TOPIC_NAME)).thenReturn(exists);
    }

    private void givenTopicMatchesReturns(final boolean matches) {
        when(adminClient.doesTopicMatches(TOPIC_NAME)).thenReturn(matches);
    }

    private void givenDeletedTopicBindingModifier() {
        deletedTopicBindingModifier = new DeletedTopicBindingRule(adminClient);
    }

    private void givenPermissionListIsFulfilled() {
        final AclPermissionBinding binding = AclPermissionCreator.givenLiteralTopicBinding(TOPIC_NAME, PRINCIPAL_NAME, HOST);
        originalPermissions = List.of(binding);
    }

    private void givenPermissionListIsFulfilledWithAnotherPermission() {
        final ResourcePattern pattern = new ResourcePattern(ResourceType.CLUSTER, "another", PatternType.LITERAL);
        final AccessControlEntry entry = new AccessControlEntry(PRINCIPAL_NAME, HOST, AclOperation.READ, AclPermissionType.ALLOW);
        originalPermissions = List.of(new AclPermissionBinding(new AclBinding(pattern, entry)));
    }

    private void givenPermissionListIsFulfilledWithPattern() {
        final AclPermissionBinding binding = AclPermissionCreator.givenPrefixTopicBinding(TOPIC_NAME, PRINCIPAL_NAME, HOST);
        originalPermissions = List.of(binding);
    }

    private void whenModifierRuns() {
        final ContextExecution context = new ContextExecution() {
            @Override
            @NotNull
            public List<PermissionBinding> getOriginalPermissions() {
                return originalPermissions;
            }

            @Override
            @NotNull
            public List<ActionablePermissionBinding> getActionablePermissionBindings() {
                return actionablePermissions;
            }
        };
        deletedTopicBindingModifier.run(context);
    }

    private void thenItemIsRemoved() {
        assertEquals(1, actionablePermissions.size());
        assertEquals(ActionablePermissionBinding.Action.DELETE, actionablePermissions.get(0).getAction());
    }

    private void thenActionableItemIsEmpty() {
        assertEquals(0, actionablePermissions.size());
    }
}