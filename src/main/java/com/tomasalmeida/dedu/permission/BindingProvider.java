package com.tomasalmeida.dedu.permission;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

public interface BindingProvider {

    /**
     *
     * @param principal principal to search, if null is passed, all data is retrieved
     * @return list of bindings for principal or all acls if principal is null
     * @throws ExecutionException in case future errors
     * @throws InterruptedException in case of errors
     */
    @NotNull
    List<PermissionBinding> retrievePermissionsForPrincipal(@Nullable final String principal) throws ExecutionException, InterruptedException;
}
