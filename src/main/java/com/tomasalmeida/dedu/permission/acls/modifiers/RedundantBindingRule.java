package com.tomasalmeida.dedu.permission.acls.modifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;
import com.tomasalmeida.dedu.permission.modifier.BindingDeletionRule;

public class RedundantBindingRule implements BindingDeletionRule {

    private static boolean prefixCanFindMatches(@NotNull final PermissionBinding prefixedBinding, @NotNull final PermissionBinding literalBinding) {
        return literalBinding.getResourceName().compareTo(prefixedBinding.getResourceName()) > 0;
    }

    @Override
    public void run(@NotNull final List<PermissionBinding> originalPermissions, @NotNull final List<ActionablePermissionBinding> actionablePermissions) {
        final List<CandidatesGroup> candidateGroups = groupByCommonElements(originalPermissions);
        final List<CandidatesGroup> candidatesGroupsWithPrefix = removeCandidatesWithoutPrefix(candidateGroups);
        for (final CandidatesGroup candidatesGroup : candidatesGroupsWithPrefix) {
            findRedundantInGroup(candidatesGroup, actionablePermissions);
        }
    }

    private void findRedundantInGroup(@NotNull final CandidatesGroup candidatesGroup, @NotNull final List<ActionablePermissionBinding> actionablePermissions) {
        candidatesGroup.sortPrefixBindingByLength();
        candidatesGroup.sortLiteralBindingsByResourceName();
        removeRedundantLiteralBindings(candidatesGroup, actionablePermissions);
        removeRedundantPrefixedBindings(candidatesGroup, actionablePermissions);
    }

    private void removeRedundantLiteralBindings(@NotNull final CandidatesGroup candidatesGroup, @NotNull final List<ActionablePermissionBinding> actionablePermissions) {
        for (final PermissionBinding prefixedBinding : candidatesGroup.prefixBindings) {
            final boolean isGenericHost = "*".equals(prefixedBinding.getHost());

            int literalPosition = 0;
            boolean prefixCanMatch = true;
            while (prefixCanMatch && literalPosition < candidatesGroup.literalBindings.size()) {
                final PermissionBinding literalBinding = candidatesGroup.literalBindings.get(literalPosition);
                if (isBindingRedundant(prefixedBinding, literalBinding, isGenericHost)) {
                    candidatesGroup.literalBindings.remove(literalPosition);
                    actionablePermissions.add(new ActionablePermissionBinding(literalBinding,
                            ActionablePermissionBinding.Action.DELETE,
                            "Replaced by " + prefixedBinding));
                } else {
                    literalPosition++;
                    prefixCanMatch = prefixCanFindMatches(prefixedBinding, literalBinding);
                }
            }
        }
    }

    private void removeRedundantPrefixedBindings(@NotNull final CandidatesGroup candidatesGroup, @NotNull final List<ActionablePermissionBinding> actionablePermissions) {
        for (int prefixPosition = 0; prefixPosition < candidatesGroup.prefixBindings.size(); prefixPosition++) {
            final PermissionBinding prefixedBinding = candidatesGroup.prefixBindings.get(prefixPosition);
            final boolean isGenericHost = "*".equals(prefixedBinding.getHost());
            int prefixDeletionCandidatePosition = prefixPosition + 1;

            while (prefixDeletionCandidatePosition < candidatesGroup.prefixBindings.size()) {
                final PermissionBinding candidateForDeletion = candidatesGroup.prefixBindings.get(prefixDeletionCandidatePosition);
                if (isBindingRedundant(prefixedBinding, candidateForDeletion, isGenericHost)) {
                    candidatesGroup.prefixBindings.remove(candidateForDeletion);
                    actionablePermissions.add(new ActionablePermissionBinding(candidateForDeletion,
                            ActionablePermissionBinding.Action.DELETE,
                            "Replaced by " + prefixedBinding));
                } else {
                    prefixDeletionCandidatePosition++;
                }
            }
        }
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

    private List<CandidatesGroup> removeCandidatesWithoutPrefix(@NotNull final List<CandidatesGroup> candidateGroups) {
        return candidateGroups
                .stream()
                .filter(candidatesGroup -> !candidatesGroup.prefixBindings.isEmpty())
                .collect(Collectors.toList());
    }

    private List<CandidatesGroup> groupByCommonElements(@NotNull final List<PermissionBinding> originalPermissions) {
        final List<CandidatesGroup> candidatesGroups = new ArrayList<>();
        for (final PermissionBinding binding : originalPermissions) {
            final boolean matchFound = candidatesGroups.stream().anyMatch(candidatesGroup -> candidatesGroup.addIfMatches(binding));
            if (!matchFound) {
                candidatesGroups.add(new CandidatesGroup(binding));
            }
        }
        return candidatesGroups;
    }
}
