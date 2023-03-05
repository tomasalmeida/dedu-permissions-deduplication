package com.tomasalmeida.dedu.permission.modifier;

import java.util.List;

import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

public interface BindingTransformationModifier extends Modifier {
    void run(List<PermissionBinding> permissions, List<ActionablePermissionBinding> addedPermissions, List<ActionablePermissionBinding> deletedPermissions);
}
