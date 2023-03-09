package com.tomasalmeida.dedu.permission.modifier;

import java.util.List;

import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

public interface BindingDeletionRule extends Rule {

     void run(List<PermissionBinding> originalPermissions, List<ActionablePermissionBinding> actionablePermissions);
}
