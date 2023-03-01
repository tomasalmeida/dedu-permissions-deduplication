package com.tomasalmeida.dedu.permission.models;

import org.apache.kafka.common.acl.AclBinding;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class PermissionBinding {

    private AclBinding aclBinding;

}
