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
import com.tomasalmeida.dedu.permission.context.ContextExecution;
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

    protected BindingDeduplicator(@NotNull final String name) {
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
        final ContextExecution context = createContext();

        printCurrentPermissions(context);

        cleanObsoletePermissions(context);
        optimizePermissions(context);

        printActionablePermissions(context);
    }

    @NotNull
    private ContextExecution createContext() throws ExecutionException, InterruptedException {
        final ContextExecution context = new ContextExecution();
        final List<PermissionBinding> originalPermissions = getPermissionBindingsForUsers();
        context.getOriginalPermissions().addAll(originalPermissions);
        return context;
    }

    private void printCurrentPermissions(@NotNull final ContextExecution context) {
        for (final Printer printer : printers) {
            printer.printCurrentBindings(context.getOriginalPermissions());
        }
    }

    private void printActionablePermissions(@NotNull final ContextExecution context) {
        for (final Printer printer : printers) {
            printer.printActionableBindings(context.getActionablePermissionBindings());
        }
    }

    private void cleanObsoletePermissions(@NotNull final ContextExecution context) {
        for (final BindingDeletionRule modifier : deletionModifiers) {
            modifier.run(context);
            context.removeActionableFromOriginals();
        }
    }

    private void optimizePermissions(@NotNull final ContextExecution context) {
        for (final BindingTransformationRule modifier : transformationModifiers) {
            modifier.run(context);
            context.removeActionableFromOriginals();
        }

        final List<ActionablePermissionBinding> unchangedPermissions = getUnchangedPermissions(context.getOriginalPermissions());

        context.getActionablePermissionBindings().addAll(unchangedPermissions);
    }

    @NotNull
    private List<ActionablePermissionBinding> getUnchangedPermissions(@NotNull final List<PermissionBinding> permissions) {
        return permissions
                .stream()
                .map(this::createUnchangedPermission).collect(Collectors.toList());
    }

    @NotNull
    private ActionablePermissionBinding createUnchangedPermission(final PermissionBinding permission) {
        return new ActionablePermissionBinding(permission, ActionablePermissionBinding.Action.NONE, "Permission unchanged.");
    }
}
