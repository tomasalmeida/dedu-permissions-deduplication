package com.tomasalmeida.dedu.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public abstract class PermissionBinding {

    private String resourceType;
    private String resourceName;
    private String resourcePattern;

    private String host;
    private String operation;
    private String principal;
    private String permissionType;

}
