package com.tomasalmeida.dedu.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;
import com.tomasalmeida.dedu.permission.modifier.BindingDeletionRule;
import com.tomasalmeida.dedu.permission.modifier.BindingTransformationRule;
import com.tomasalmeida.dedu.permission.modifier.Rule;
import com.tomasalmeida.dedu.permission.printers.Printer;

public abstract class BindingDeduplicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BindingDeduplicator.class);

    private final List<BindingDeletionRule> deletionModifiers;
    private final List<BindingTransformationRule> transformationModifiers;
    private final List<Printer> printers;

    private final String name;

    public BindingDeduplicator(@NotNull final String name) {
        this.name = name;
        this.deletionModifiers = new ArrayList<>();
        this.transformationModifiers = new ArrayList<>();
        this.printers = new ArrayList<>();
    }

    protected void addRule(@NotNull final Rule rule) {
        if (rule instanceof BindingDeletionRule) {
            deletionModifiers.add((BindingDeletionRule) rule);
        } else if (rule instanceof BindingTransformationRule) {
            transformationModifiers.add((BindingTransformationRule) rule);
        }
        LOGGER.debug("Rule [{}] added to Binding deduplicator [{}]", rule, name);
    }

    protected void addPrinter(@NotNull final Printer printer) {
        printers.add(printer);
    }

    protected abstract List<PermissionBinding> getPermissionBindingsForUsers() throws ExecutionException, InterruptedException;

    public void run() throws ExecutionException, InterruptedException {
        final List<PermissionBinding> originalPermissions = getPermissionBindingsForUsers();

        final List<ActionablePermissionBinding> actionablePermissionBindings = deduplicatePermissions(originalPermissions);

        printPermissions(originalPermissions, actionablePermissionBindings);
    }

    private void printPermissions(@NotNull final List<PermissionBinding> originalPermissions,
                                  @NotNull final List<ActionablePermissionBinding> actionablePermissions) {
        for (final Printer printer : printers) {
            printer.printCurrentBindings(originalPermissions);
            printer.printActionableBindings(actionablePermissions);
        }
    }

    @NotNull
    private List<ActionablePermissionBinding> deduplicatePermissions(@NotNull final List<PermissionBinding> originalPermissions) {
        final List<ActionablePermissionBinding> actionablePermissions = new ArrayList<>();

        final List<PermissionBinding> cleanedPermissions = cleanObsoletePermissions(originalPermissions, actionablePermissions);

        return optimizePermissions(cleanedPermissions, actionablePermissions);
    }

    @NotNull
    private List<PermissionBinding> cleanObsoletePermissions(@NotNull final List<PermissionBinding> originalPermissions,
                                                             @NotNull final List<ActionablePermissionBinding> actionablePermissions) {
        for (final BindingDeletionRule modifier : deletionModifiers) {
            modifier.run(originalPermissions, actionablePermissions);
            originalPermissions.removeAll(actionablePermissions);
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

        for (final BindingTransformationRule modifier : transformationModifiers) {
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
