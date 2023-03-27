package com.tomasalmeida.dedu.permission.acls.modifiers;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;
import com.tomasalmeida.dedu.permission.modifier.BindingDeletionRule;
import com.tomasalmeida.dedu.permission.modifier.context.CandidatesGroup;
import com.tomasalmeida.dedu.permission.modifier.context.ContextRule;

public class RedundantBindingRule implements BindingDeletionRule {

    private boolean prefixCanFindMatches(@NotNull final PermissionBinding prefixedBinding, @NotNull final PermissionBinding literalBinding) {
        return literalBinding.getResourceName().compareTo(prefixedBinding.getResourceName()) > 0;
    }

    @Override
    public void run(@NotNull final ContextRule contextRule) {
        final List<CandidatesGroup> candidateGroups = contextRule.getCandidatesGroups();
        for (final CandidatesGroup candidatesGroup : candidateGroups) {
            findRedundantInGroup(candidatesGroup, contextRule.getActionablePermissionBindings());
        }
    }

    private void findRedundantInGroup(@NotNull final CandidatesGroup candidatesGroup, @NotNull final List<ActionablePermissionBinding> actionablePermissions) {
        candidatesGroup.sortPrefixBindingByLength();
        candidatesGroup.sortLiteralBindingsByResourceName();
        removeRedundantLiteralBindings(candidatesGroup, actionablePermissions);
        removeRedundantPrefixedBindings(candidatesGroup, actionablePermissions);
    }

    private void removeRedundantLiteralBindings(@NotNull final CandidatesGroup candidatesGroup, @NotNull final List<ActionablePermissionBinding> actionablePermissions) {
        for (final PermissionBinding prefixedBinding : candidatesGroup.getPrefixBindings()) {
            final boolean isGenericHost = "*".equals(prefixedBinding.getHost());

            int literalPosition = 0;
            boolean prefixCanMatch = true;
            while (prefixCanMatch && literalPosition < candidatesGroup.getLiteralBindings().size()) {
                final PermissionBinding literalBinding = candidatesGroup.getLiteralBindings().get(literalPosition);
                if (isBindingRedundant(prefixedBinding, literalBinding, isGenericHost)) {
                    candidatesGroup.getLiteralBindings().remove(literalPosition);
                    actionablePermissions.add(createActionBinding(prefixedBinding, literalBinding));
                } else {
                    literalPosition++;
                    prefixCanMatch = prefixCanFindMatches(prefixedBinding, literalBinding);
                }
            }
        }
    }

    private void removeRedundantPrefixedBindings(@NotNull final CandidatesGroup candidatesGroup, @NotNull final List<ActionablePermissionBinding> actionablePermissions) {
        for (int prefixPosition = 0; prefixPosition < candidatesGroup.getPrefixBindings().size(); prefixPosition++) {
            final PermissionBinding prefixedBinding = candidatesGroup.getPrefixBindings().get(prefixPosition);
            final boolean isGenericHost = "*".equals(prefixedBinding.getHost());
            int prefixDeletionCandidatePosition = prefixPosition + 1;

            while (prefixDeletionCandidatePosition < candidatesGroup.getPrefixBindings().size()) {
                final PermissionBinding candidateForDeletion = candidatesGroup.getPrefixBindings().get(prefixDeletionCandidatePosition);
                if (isBindingRedundant(prefixedBinding, candidateForDeletion, isGenericHost)) {
                    candidatesGroup.getPrefixBindings().remove(candidateForDeletion);
                    actionablePermissions.add(createActionBinding(prefixedBinding, candidateForDeletion));
                } else {
                    prefixDeletionCandidatePosition++;
                }
            }
        }
    }

    @NotNull
    private ActionablePermissionBinding createActionBinding(final PermissionBinding prefixedBinding, final PermissionBinding literalBinding) {
        return new ActionablePermissionBinding(literalBinding,
                ActionablePermissionBinding.Action.DELETE,
                "Replaced by prefixed binding started with [" + prefixedBinding.getResourceName() + "]");
    }

    private boolean isBindingRedundant(@NotNull final PermissionBinding prefixedBinding,
                                       @NotNull final PermissionBinding candidateForDeletion,
                                       final boolean isGenericHost) {
        if (prefixedBinding.equals(candidateForDeletion)) {
            return false;
        }
        final boolean literalResourceMatchesPrefix = candidateForDeletion.getResourceName().startsWith(prefixedBinding.getResourceName());
        final boolean isHostRedundant = isGenericHost || prefixedBinding.getHost().equals(candidateForDeletion.getHost());
        return literalResourceMatchesPrefix && isHostRedundant;
    }
}
