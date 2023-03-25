package com.tomasalmeida.dedu.permission.acls.modifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tomasalmeida.dedu.com.tomasalmeida.tests.acls.AclPermissionCreator;
import com.tomasalmeida.dedu.permission.acls.AclPermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

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
        final List<PermissionBinding> originalPermissions = new ArrayList<>(
                List.of(bindL1A, bindL2A, bindL3A, bindL4A, bindL1B, bindP1A, bindP2A, bindP1B));
        final ArrayList<ActionablePermissionBinding> actionablePermissions = new ArrayList<>();

        redundantBindingRule.run(originalPermissions, actionablePermissions);

        assertEquals(6, actionablePermissions.size());

        assertTrue(actionablePermissions.contains(bindL1A));
        assertTrue(actionablePermissions.contains(bindL2A));
        assertTrue(actionablePermissions.contains(bindL3A));
        assertTrue(actionablePermissions.contains(bindL4A));
        assertFalse(actionablePermissions.contains(bindP1A));
        assertTrue(actionablePermissions.contains(bindP2A));
        assertTrue(actionablePermissions.contains(bindL1B));
        assertFalse(actionablePermissions.contains(bindP1B));
    }
}