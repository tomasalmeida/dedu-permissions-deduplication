package com.tomasalmeida.dedu.com.tomasalmeida.tests.system;

import java.security.Permission;

public class DisabledExitSecurityManager extends SecurityManager {
    private final SecurityManager delegatedSecurityManager;
    private Integer firstExitStatusCode;

    public DisabledExitSecurityManager(final SecurityManager originalSecurityManager) {
        this.delegatedSecurityManager = originalSecurityManager;
    }

    @Override
    public void checkPermission(final Permission perm) {
        /**
         * do nothing
         * this method needs to be overriden because the real implementation throws an exception when
         * the exit method of System is called.
         **/
    }

    /**
     * disable exit from jvm
     *
     * @param statusCode the exit status
     */
    @Override
    public void checkExit(final int statusCode) {
        if (firstExitStatusCode == null) {
            this.firstExitStatusCode = statusCode;
        }
        throw new SystemExitPreventedException();
    }

    public Integer getFirstExitStatusCode() {
        return firstExitStatusCode;
    }
}

