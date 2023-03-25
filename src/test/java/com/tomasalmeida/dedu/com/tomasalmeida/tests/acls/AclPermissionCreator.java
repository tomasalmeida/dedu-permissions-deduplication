package com.tomasalmeida.dedu.com.tomasalmeida.tests.acls;

import static org.apache.kafka.common.acl.AclOperation.READ;
import static org.apache.kafka.common.acl.AclPermissionType.ALLOW;
import static org.apache.kafka.common.resource.PatternType.LITERAL;
import static org.apache.kafka.common.resource.PatternType.PREFIXED;
import static org.apache.kafka.common.resource.ResourceType.TOPIC;

import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.permission.acls.AclPermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;

public class AclPermissionCreator {

    @NotNull
    public static AclPermissionBinding givenLiteralTopicBinding(final String topicName, final String principal, final String host) {
        return createTopicBinding(topicName, principal, host, LITERAL);
    }

    @NotNull
    public static AclPermissionBinding givenPrefixTopicBinding(final String topicName, final String principal, final String host) {
        return createTopicBinding(topicName, principal, host, PREFIXED);
    }

    @NotNull
    private static AclPermissionBinding createTopicBinding(final String topicName,
                                                           final String principal,
                                                           final String host,
                                                           final PatternType patternType) {
        final ResourcePattern pattern = new ResourcePattern(TOPIC, topicName, patternType);
        final AccessControlEntry entry = new AccessControlEntry(principal, host, READ, ALLOW);
        return new AclPermissionBinding(new AclBinding(pattern, entry));
    }

    public static ActionablePermissionBinding givenActionLiteralTopicBinding(final String topicName,
                                                                             final String principalName,
                                                                             final String host,
                                                                             final ActionablePermissionBinding.Action action,
                                                                             final String note) {
        final AclPermissionBinding binding = givenLiteralTopicBinding(topicName, principalName, host);
        return new ActionablePermissionBinding(binding, action, note);
    }
}
