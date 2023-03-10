package com.tomasalmeida.dedu.permission;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

public interface BindingProvider {

    @NotNull
    List<PermissionBinding> retrievePermissionsForPrincipal(@NotNull final String principal) throws ExecutionException, InterruptedException;
}
