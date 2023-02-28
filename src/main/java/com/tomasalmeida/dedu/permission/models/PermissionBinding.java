package com.tomasalmeida.dedu.permission.models;

import org.apache.kafka.common.acl.AclBinding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
@AllArgsConstructor
public class PermissionBinding {

    private AclBinding aclBinding;

    @Override
    public String toString() {
        return "PermissionBinding{" +
                "aclBinding=" + aclBinding +
                '}';
    }
}
