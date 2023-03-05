package com.tomasalmeida.dedu.permission.acls.modifiers;

import java.util.List;
import java.util.Objects;

import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;
import com.tomasalmeida.dedu.permission.modifier.BindingDeletionModifier;

public class InexistentTopicBindingModifier implements BindingDeletionModifier {

    private final KafkaAdminClient adminClient;

    public InexistentTopicBindingModifier(final KafkaAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    private static boolean isSpecificResource(final String resourceName) {
        return !"*".equals(resourceName);
    }

    @Override
    public void run(@NotNull final List<PermissionBinding> originalPermissions,
                    @NotNull final List<ActionablePermissionBinding> actionablePermission) {
        originalPermissions
                .stream()
                .filter(this::isPermissionObsolete)
                .map(this::createActionablePermissionForDeletion)
                .forEach(actionablePermission::add);
    }

    @NotNull
    private ActionablePermissionBinding createActionablePermissionForDeletion(@NotNull final PermissionBinding permission) {
        return new ActionablePermissionBinding(permission, ActionablePermissionBinding.Action.DELETE, "Resource topic was deleted.");
    }

    private boolean isPermissionObsolete(final PermissionBinding permission) {
        if (Objects.requireNonNull(permission.getResourceType()) == ResourceType.TOPIC) {
            return isTopicPermissionObsolete(permission);
        }
        return false;
    }

    private boolean isTopicPermissionObsolete(@NotNull final PermissionBinding permission) {
        final String resourceName = permission.getResourceName();
        switch (permission.getPatternType()) {
            case LITERAL:
                return isSpecificResource(resourceName) && isTopicRemoved(resourceName);
            case PREFIXED:
                return isPrefixUnused(permission);
            default:
                return false;
        }
    }

    private boolean isPrefixUnused(final PermissionBinding permission) {
        return permission == null;
    }

    private boolean isTopicRemoved(final String topicName) {
        return !adminClient.topicExists(topicName);
    }
}
