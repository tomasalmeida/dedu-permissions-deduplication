package com.tomasalmeida.dedu.permission;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.tomasalmeida.dedu.permission.PermissionBinding;

public interface BindingProvider {

    List<PermissionBinding> retrievePermissionsForPrincipal(final String principal) throws ExecutionException, InterruptedException;
}
