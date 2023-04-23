package com.tomasalmeida.dedu.permission.acls.modifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tomasalmeida.dedu.com.tomasalmeida.tests.acls.AclPermissionCreator;
import com.tomasalmeida.dedu.permission.acls.AclPermissionBinding;
import com.tomasalmeida.dedu.permission.context.ContextExecution;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class RedundantBindingRuleTest {

    private static final String TOPIC_NAME1 = "topic-t";
    private static final String TOPIC_NAME2 = "topic-to";
    private static final String TOPIC_NAME3 = "topic-top";
    private static final String TOPIC_NAME4 = "topic-topi";
    private static final String PRINCIPAL_NAMEA = "principalA";
    private static final String PRINCIPAL_NAMEB = "principalB";
    private static final String HOST = "*";

    private static final AclPermissionBinding bindL1A = AclPermissionCreator.givenLiteralTopicBinding(TOPIC_NAME1, PRINCIPAL_NAMEA, HOST);
    private static final AclPermissionBinding bindL2A = AclPermissionCreator.givenLiteralTopicBinding(TOPIC_NAME2, PRINCIPAL_NAMEA, HOST);
    private static final AclPermissionBinding bindL3A = AclPermissionCreator.givenLiteralTopicBinding(TOPIC_NAME3, PRINCIPAL_NAMEA, HOST);
    private static final AclPermissionBinding bindL4A = AclPermissionCreator.givenLiteralTopicBinding(TOPIC_NAME4, PRINCIPAL_NAMEA, HOST);
    private static final AclPermissionBinding bindL1B = AclPermissionCreator.givenLiteralTopicBinding(TOPIC_NAME1, PRINCIPAL_NAMEB, HOST);

    private static final AclPermissionBinding bindP1A = AclPermissionCreator.givenPrefixTopicBinding(TOPIC_NAME1, PRINCIPAL_NAMEA, HOST);
    private static final AclPermissionBinding bindP2A = AclPermissionCreator.givenPrefixTopicBinding(TOPIC_NAME2, PRINCIPAL_NAMEA, HOST);
    private static final AclPermissionBinding bindP1B = AclPermissionCreator.givenPrefixTopicBinding(TOPIC_NAME1, PRINCIPAL_NAMEB, HOST);

    @Test
    void shouldRemoveRedundants() {
        final RedundantBindingRule redundantBindingRule = new RedundantBindingRule();

        final ContextExecution context = new ContextExecution();
        context.getOriginalPermissions().addAll(
                List.of(bindL1A, bindL2A, bindL3A, bindL4A, bindL1B, bindP1A, bindP2A, bindP1B));

        redundantBindingRule.run(context);

        assertEquals(6, context.getActionablePermissionBindings().size());

        assertTrue(context.getActionablePermissionBindings().contains(bindL1A));
        assertTrue(context.getActionablePermissionBindings().contains(bindL2A));
        assertTrue(context.getActionablePermissionBindings().contains(bindL3A));
        assertTrue(context.getActionablePermissionBindings().contains(bindL4A));
        assertFalse(context.getActionablePermissionBindings().contains(bindP1A));
        assertTrue(context.getActionablePermissionBindings().contains(bindP2A));
        assertTrue(context.getActionablePermissionBindings().contains(bindL1B));
        assertFalse(context.getActionablePermissionBindings().contains(bindP1B));
    }
}