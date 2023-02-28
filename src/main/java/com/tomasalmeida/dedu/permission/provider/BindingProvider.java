package com.tomasalmeida.dedu.permission.provider;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.tomasalmeida.dedu.permission.models.PermissionBinding;

public interface BindingProvider {

    List<PermissionBinding> retrievePermissionsForPrincipal(final String principal) throws ExecutionException, InterruptedException;
}
