package com.tomasalmeida.dedu.permission.acls;

import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

public class AclPermissionBinding extends PermissionBinding {

    private static final Logger LOGGER = LoggerFactory.getLogger(AclPermissionBinding.class);

    public AclPermissionBinding(@NotNull final AclBinding aclBinding) {
        super(aclBinding.pattern().resourceType(),
                aclBinding.pattern().name(),
                aclBinding.pattern().patternType(),
                aclBinding.entry().host(),
                aclBinding.entry().operation().toString(),
                aclBinding.entry().principal(),
                aclBinding.entry().permissionType().toString());
        LOGGER.debug("AclPermissionBinding created with [{}]", aclBinding);
    }

    public AclPermissionBinding(final ResourceType resourceType,
                                final String resourceName,
                                final PatternType patternType,
                                final String host,
                                final String operation,
                                final String principal,
                                final String permissionType) {
        super(resourceType, resourceName, patternType, host, operation, principal, permissionType);
    }
}
