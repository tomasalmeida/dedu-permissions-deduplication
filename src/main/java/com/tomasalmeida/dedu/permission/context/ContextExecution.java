package com.tomasalmeida.dedu.permission.context;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

public class ContextExecution {

    private final List<CandidatesGroup> candidatesGroups = new ArrayList<>();
    private final List<PermissionBinding> originalPermissions = new ArrayList<>();
    private final List<ActionablePermissionBinding> actionablePermissionBindings = new ArrayList<>();

    @NotNull
    public List<CandidatesGroup> getCandidatesGroups() {
        if (candidatesGroups.isEmpty()) {
            populateCandidatesGroup();
        }
        return candidatesGroups;
    }

    @NotNull
    public List<PermissionBinding> getOriginalPermissions() {
        return originalPermissions;
    }

    @NotNull
    public List<ActionablePermissionBinding> getActionablePermissionBindings() {
        return actionablePermissionBindings;
    }

    private void populateCandidatesGroup() {
        for (final PermissionBinding binding : originalPermissions) {
            final boolean matchFound = candidatesGroups.stream().anyMatch(candidatesGroup -> candidatesGroup.addIfMatches(binding));
            if (!matchFound) {
                candidatesGroups.add(new CandidatesGroup(binding));
            }
        }
    }

    public void removeActionableFromOriginals() {
        originalPermissions.removeAll(actionablePermissionBindings);
    }
}
