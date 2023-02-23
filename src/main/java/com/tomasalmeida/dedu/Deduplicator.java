package com.tomasalmeida.dedu;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.ResourcePatternFilter;

import com.tomasalmeida.dedu.common.PropertiesLoader;

/**
 * Deduplicator coordinator
 */
public class Deduplicator implements AutoCloseable {

    private final AdminClient adminClient;
    private final String principal;

    public Deduplicator(final Properties properties, final String principal) {
        adminClient = AdminClient.create(properties);
        this.principal = principal;
    }

    public static Deduplicator build(final String configFile, final String principal) throws IOException {
        final Properties properties = PropertiesLoader.load(configFile);
        return new Deduplicator(properties, principal);
    }

    @Override
    public void close() {
        adminClient.close();
    }

    public void run() {
        final AccessControlEntryFilter entryFilter = new AccessControlEntryFilter(principal, null, AclOperation.ANY, AclPermissionType.ANY);
        final AclBindingFilter filter = new AclBindingFilter(ResourcePatternFilter.ANY, entryFilter);
        final DescribeAclsResult results = adminClient.describeAcls(filter);
        try {
            results.values()
                    .get()
                    .forEach(System.out::println);
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
