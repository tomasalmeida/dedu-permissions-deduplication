package com.tomasalmeida.dedu.permission.acls.modifiers;

import static com.tomasalmeida.dedu.permission.acls.modifiers.ConsolidateLiteralTopicBindingRule.CONFIG_RULE_ENABLED;
import static com.tomasalmeida.dedu.permission.acls.modifiers.ConsolidateLiteralTopicBindingRule.CONFIG_RULE_MIN_PREFIX;
import static com.tomasalmeida.dedu.permission.acls.modifiers.ConsolidateLiteralTopicBindingRule.CONFIG_RULE_MIN_REPLACED;
import static com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding.Action.ADD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.com.tomasalmeida.tests.acls.AclPermissionCreator;
import com.tomasalmeida.dedu.configuration.MainConfiguration;
import com.tomasalmeida.dedu.permission.acls.AclPermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;
import com.tomasalmeida.dedu.permission.context.ContextExecution;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class ConsolidateLiteralTopicBindingRuleTest {

    private static final String TOPIC_PREFIX = "topic-";
    private static final String TOPIC_NAME1 = "topic-1";
    private static final String TOPIC_NAME2 = "topic-topic2";
    private static final String TOPIC_NAME3 = "topic-topic3";
    private static final String TOPIC_NAME4 = "topic-topic4";
    private static final String PRINCIPAL_NAME = "principal";
    private static final String HOST = "*";

    @Mock
    private KafkaAdminClient adminClient;

    private ConsolidateLiteralTopicBindingRule consolidateLiteralTopicBindingRule;
    private ContextExecution context;
    private AclPermissionBinding binding1;
    private AclPermissionBinding binding2;
    private AclPermissionBinding binding3;
    private AclPermissionBinding binding4;

    @Test
    void shouldConsolidateTopicBinding() {
        givenRuleIsCreatedAndActivated();
        givenContextIsFilled();
        givenAdminClientFindsSameNumberOfPrefixes();

        whenRuleRuns();

        thenActionableItemsShouldBeCorrect();
    }

    private void givenRuleIsCreatedAndActivated() {
        final MainConfiguration mainConfiguration = mock(MainConfiguration.class);
        when(mainConfiguration.getDeduPropertyOrDefault(CONFIG_RULE_ENABLED, "false")).thenReturn("true");
        when(mainConfiguration.getDeduPropertyOrDefault(CONFIG_RULE_MIN_PREFIX, "0")).thenReturn("3");
        when(mainConfiguration.getDeduPropertyOrDefault(CONFIG_RULE_MIN_REPLACED, "0")).thenReturn("3");
        consolidateLiteralTopicBindingRule = new ConsolidateLiteralTopicBindingRule(adminClient, mainConfiguration);
    }

    private void givenContextIsFilled() {
        binding1 = AclPermissionCreator.givenLiteralTopicBinding(TOPIC_NAME1, PRINCIPAL_NAME, HOST);
        binding2 = AclPermissionCreator.givenLiteralTopicBinding(TOPIC_NAME2, PRINCIPAL_NAME, HOST);
        binding3 = AclPermissionCreator.givenLiteralTopicBinding(TOPIC_NAME3, PRINCIPAL_NAME, HOST);
        binding4 = AclPermissionCreator.givenLiteralTopicBinding(TOPIC_NAME4, PRINCIPAL_NAME, HOST);
        final List<PermissionBinding> originalPermissions = new ArrayList<>(List.of(binding1, binding2, binding3, binding4));

        context = new ContextExecution() {
            @Override
            @NotNull
            public List<PermissionBinding> getOriginalPermissions() {
                return originalPermissions;
            }
        };
    }

    private void givenAdminClientFindsSameNumberOfPrefixes() {
        when(adminClient.countMatches(TOPIC_PREFIX)).thenReturn(4L);
//        when(adminClient.countMatches(argThat(s -> !s.equals(TOPIC_PREFIX)))).thenReturn(5L);
    }

    private void whenRuleRuns() {
        consolidateLiteralTopicBindingRule.run(context);
    }

    private void thenActionableItemsShouldBeCorrect() {
        final List<ActionablePermissionBinding> actionablePermissionBindings = context.getActionablePermissionBindings();

        assertEquals(5, actionablePermissionBindings.size());
        assertTrue(actionablePermissionBindings.contains(binding1));
        assertTrue(actionablePermissionBindings.contains(binding2));
        assertTrue(actionablePermissionBindings.contains(binding3));
        assertTrue(actionablePermissionBindings.contains(binding4));

        final long foundCorrectPrefix = actionablePermissionBindings
                .stream()
                .filter(actionableBinding -> ADD.equals(actionableBinding.getAction()))
                .filter(actionableBinding -> TOPIC_PREFIX.equals(actionableBinding.getResourceName()))
                .count();
        assertEquals(1, foundCorrectPrefix);
    }

}