package com.tomasalmeida.dedu.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.Configuration;
import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.permission.acls.AclBindingProvider;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;
import com.tomasalmeida.dedu.permission.modifier.BindingDeletionModifier;
import com.tomasalmeida.dedu.permission.modifier.BindingTransformationModifier;
import com.tomasalmeida.dedu.permission.modifier.Modifier;

public abstract class BindingDeduplicator {

    private final List<BindingDeletionModifier> deletionModifiers;
    private final List<BindingTransformationModifier> transformationModifiers;
    private final KafkaAdminClient adminClient;
    private final Configuration configuration;

    public BindingDeduplicator(final KafkaAdminClient adminClient, final Configuration configuration) {
        this.deletionModifiers = new ArrayList<>();
        this.transformationModifiers = new ArrayList<>();
        this.adminClient = adminClient;
        this.configuration = configuration;
    }

    public void addRule(final Modifier modifier) {
        if (modifier instanceof BindingDeletionModifier) {
            deletionModifiers.add((BindingDeletionModifier) modifier);
        } else if (modifier instanceof BindingTransformationModifier) {
            transformationModifiers.add((BindingTransformationModifier) modifier);
        }
    }

    @NotNull
    public List<ActionablePermissionBinding> run() throws ExecutionException, InterruptedException {
        final List<PermissionBinding> originalPermissions = getPermissionBindingsForUsers();
        final List<ActionablePermissionBinding> actionablePermissions = new ArrayList<>();

        final List<PermissionBinding> cleanedPermissions = cleanObsoletePermissions(originalPermissions, actionablePermissions);

        return optimizePermissions(cleanedPermissions, actionablePermissions);
    }

    @NotNull
    private List<PermissionBinding> getPermissionBindingsForUsers() throws ExecutionException, InterruptedException {
        final BindingProvider bindingProvider = new AclBindingProvider(adminClient);
        return bindingProvider.retrievePermissionsForPrincipal(configuration.getPrincipal());
    }

    @NotNull
    private List<PermissionBinding> cleanObsoletePermissions(@NotNull final List<PermissionBinding> originalPermissions,
                                                             @NotNull final List<ActionablePermissionBinding> actionablePermissions) {
        for (final BindingDeletionModifier modifier : deletionModifiers) {
            modifier.run(originalPermissions, actionablePermissions);
        }

        return originalPermissions
                .stream()
                .filter(permission -> !actionablePermissions.contains(permission))
                .collect(Collectors.toList());
    }

    @NotNull
    private List<ActionablePermissionBinding> optimizePermissions(@NotNull final List<PermissionBinding> permissions,
                                                                  @NotNull final List<ActionablePermissionBinding> actionablePermissions) {
        final List<ActionablePermissionBinding> deletedPermissions = new ArrayList<>();
        final List<ActionablePermissionBinding> addedPermissions = new ArrayList<>();

        for (final BindingTransformationModifier modifier : transformationModifiers) {
            modifier.run(permissions, addedPermissions, deletedPermissions);
        }

        final List<ActionablePermissionBinding> unchangedPermissions = getUnchangedPermissions(permissions, deletedPermissions);

        // add new, deleted and unchanged permissions to the actionable list
        actionablePermissions.addAll(addedPermissions);
        actionablePermissions.addAll(deletedPermissions);
        actionablePermissions.addAll(unchangedPermissions);
        return actionablePermissions;
    }

    @NotNull
    private List<ActionablePermissionBinding> getUnchangedPermissions(@NotNull final List<PermissionBinding> permissions,
                                                                      @NotNull final List<ActionablePermissionBinding> deletedPermissions) {
        return permissions
                .stream()
                .filter(permission -> !deletedPermissions.contains(permission))
                .map(this::createUnchangedPermission).collect(Collectors.toList());
    }

    @NotNull
    private ActionablePermissionBinding createUnchangedPermission(final PermissionBinding permission) {
        return new ActionablePermissionBinding(permission, ActionablePermissionBinding.Action.NONE, "Permission unchanged.");
    }
}
