package com.tomasalmeida.dedu.permission.bindings;

import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public abstract class PermissionBinding {

    private ResourceType resourceType;
    private String resourceName;
    private PatternType patternType;

    private String host;
    private String operation;
    private String principal;
    private String permissionType;

    /**
     * Create new PermissionBinding using the values from another PermissionBinding
     *
     * @param permission to be cloned
     */
    protected PermissionBinding(@NotNull final PermissionBinding permission) {
        this.resourceType = permission.resourceType;
        this.resourceName = permission.resourceName;
        this.patternType = permission.patternType;

        this.host = permission.host;
        this.operation = permission.operation;
        this.principal = permission.principal;
        this.permissionType = permission.permissionType;
    }
}
