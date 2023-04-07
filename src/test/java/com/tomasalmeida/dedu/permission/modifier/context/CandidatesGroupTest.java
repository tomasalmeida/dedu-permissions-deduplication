package com.tomasalmeida.dedu.permission.modifier.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tomasalmeida.dedu.com.tomasalmeida.tests.acls.AclPermissionCreator;
import com.tomasalmeida.dedu.permission.acls.AclPermissionBinding;
import com.tomasalmeida.dedu.permission.context.CandidatesGroup;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class CandidatesGroupTest {

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
    void shouldGroupPermissionsTogether() {
        final CandidatesGroup candidatesGroup = new CandidatesGroup(bindL1A);

        givenPermissionsAreAdded(candidatesGroup);

        assertEquals(4, candidatesGroup.getLiteralBindings().size());
        assertEquals(2, candidatesGroup.getPrefixBindings().size());
        assertFalse(candidatesGroup.getLiteralBindings().contains(bindL1B));
        assertFalse(candidatesGroup.getPrefixBindings().contains(bindP1B));
    }

    @Test
    void shouldSortCorrectly() {
        final CandidatesGroup candidatesGroup = new CandidatesGroup(bindL1A);
        givenPermissionsAreAdded(candidatesGroup);

        candidatesGroup.sortPrefixBindingByLength();
        candidatesGroup.sortLiteralBindingsByResourceName();

        assertEquals(TOPIC_NAME1, candidatesGroup.getPrefixBindings().get(0).getResourceName());
        assertEquals(TOPIC_NAME2, candidatesGroup.getPrefixBindings().get(1).getResourceName());

        assertEquals(TOPIC_NAME1, candidatesGroup.getLiteralBindings().get(0).getResourceName());
        assertEquals(TOPIC_NAME2, candidatesGroup.getLiteralBindings().get(1).getResourceName());
        assertEquals(TOPIC_NAME3, candidatesGroup.getLiteralBindings().get(2).getResourceName());
        assertEquals(TOPIC_NAME4, candidatesGroup.getLiteralBindings().get(3).getResourceName());
    }

    private void givenPermissionsAreAdded(final CandidatesGroup candidatesGroup) {
        assertTrue(candidatesGroup.addIfMatches(bindL4A));
        assertTrue(candidatesGroup.addIfMatches(bindL3A));
        assertTrue(candidatesGroup.addIfMatches(bindL2A));
        assertFalse(candidatesGroup.addIfMatches(bindL1B));

        assertTrue(candidatesGroup.addIfMatches(bindP2A));
        assertTrue(candidatesGroup.addIfMatches(bindP1A));
        assertFalse(candidatesGroup.addIfMatches(bindP1B));
    }
}