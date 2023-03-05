package com.tomasalmeida.dedu.permission.acls;

import org.apache.kafka.common.acl.AclBinding;

import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

public class AclPermissionBinding extends PermissionBinding {

    public AclPermissionBinding(final AclBinding aclBinding) {
        super(aclBinding.pattern().resourceType(),
                aclBinding.pattern().name(),
                aclBinding.pattern().patternType(),
                aclBinding.entry().host(),
                aclBinding.entry().operation().toString(),
                aclBinding.entry().principal(),
                aclBinding.entry().permissionType().toString());
    }
}
