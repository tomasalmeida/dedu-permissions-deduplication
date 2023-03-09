package com.tomasalmeida.dedu.permission.acls.modifiers;

import java.util.List;

import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;
import com.tomasalmeida.dedu.permission.modifier.BindingDeletionRule;

public class DeletedTopicBindingRule implements BindingDeletionRule {

    private final KafkaAdminClient adminClient;

    public DeletedTopicBindingRule(@NotNull final KafkaAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    @Override
    public void run(@NotNull final List<PermissionBinding> originalPermissions,
                    @NotNull final List<ActionablePermissionBinding> actionablePermissions) {
        originalPermissions
                .stream()
                .filter(this::isPermissionObsolete)
                .map(this::createActionablePermissionForDeletion)
                .forEach(actionablePermissions::add);
    }

    @NotNull
    private ActionablePermissionBinding createActionablePermissionForDeletion(@NotNull final PermissionBinding permission) {
        return new ActionablePermissionBinding(permission, ActionablePermissionBinding.Action.DELETE, "Resource topic was deleted.");
    }

    private boolean isPermissionObsolete(@NotNull final PermissionBinding permission) {
        if (ResourceType.TOPIC.equals(permission.getResourceType())) {
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

    private boolean isPrefixUnused(@NotNull final PermissionBinding permission) {
        return permission == null;
    }

    private boolean isTopicRemoved(@NotNull final String topicName) {
        return !adminClient.topicExists(topicName);
    }

    private boolean isSpecificResource(@NotNull final String resourceName) {
        return !"*".equals(resourceName);
    }

}
