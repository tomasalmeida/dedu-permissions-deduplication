package com.tomasalmeida.dedu.api.kafka;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tomasalmeida.dedu.Configuration;
import com.tomasalmeida.dedu.api.system.PropertiesLoader;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class KafkaAdminClientTest {

    private static final String KAFKA_CONFIG_PATH = "/etc/path/to/file";
    private static final String PRINCIPAL = "principal";

    @Mock
    private AdminClient adminClient;
    @Mock
    private AclBindingFilter filter;
    @Mock
    private DescribeAclsResult mockedDescribeAcls;
    @Mock
    private Properties properties;

    private MockedStatic<AdminClient> adminClientMocked;
    private MockedStatic<PropertiesLoader> propertiesLoaderMocked;
    private Configuration configuration;
    private KafkaAdminClient kafkaAdminClient;
    private DescribeAclsResult describeAclsResult;

    @BeforeEach
    void beforeEach() {
        givenAdminClientIsCreated();
        givenPropertiesCanBeLoaded();
    }

    @AfterEach
    void afterEach() {
        thenAdminClientMockIsClosed();
        thenPropertiesMockIsClosed();
    }

    @Test
    void shouldReturnDescribeAcls() throws Exception {
        givenAdminClientReturnsAcls();
        givenConfigurationIsCreated();
        givenAdminclientIsCreated();

        whenDescribeAclsIsCalled();

        thenAdminClientDescribeAclsIsCalled();
    }

    @Test
    void shouldCloseCorrectly() throws Exception {
        givenConfigurationIsCreated();
        givenAdminclientIsCreated();

        whenKafkaAdminIsClosed();

        thenAdminClientCloseIsCalledOnce();
    }

    @Test
    void shouldCloseCorrectlyWhenCloseIsCalledTwice() throws Exception {
        givenConfigurationIsCreated();
        givenAdminclientIsCreated();

        whenKafkaAdminIsClosed();
        whenKafkaAdminIsClosed();

        thenAdminClientCloseIsCalledOnce();
    }

    private void givenAdminClientIsCreated() {
        adminClientMocked = Mockito.mockStatic(AdminClient.class);
        adminClientMocked.when(() -> AdminClient.create(any(Properties.class)))
                .thenReturn(adminClient);
    }

    private void givenPropertiesCanBeLoaded() {
        propertiesLoaderMocked = Mockito.mockStatic(PropertiesLoader.class);
        propertiesLoaderMocked.when(() -> PropertiesLoader.loadFromFile(anyString()))
                .thenReturn(properties);
    }

    private void givenAdminClientReturnsAcls() {
        when(adminClient.describeAcls(filter)).thenReturn(mockedDescribeAcls);
    }

    private void givenConfigurationIsCreated() {
        configuration = new Configuration(KAFKA_CONFIG_PATH, PRINCIPAL);
    }

    private void givenAdminclientIsCreated() throws Exception {
        kafkaAdminClient = KafkaAdminClient.build(configuration);
    }

    private void whenDescribeAclsIsCalled() {
        describeAclsResult = kafkaAdminClient.describeAcls(filter);
    }

    private void whenKafkaAdminIsClosed() {
        kafkaAdminClient.close();
    }

    private void thenAdminClientMockIsClosed() {
        adminClientMocked.close();
    }

    private void thenPropertiesMockIsClosed() {
        propertiesLoaderMocked.close();
    }

    private void thenAdminClientDescribeAclsIsCalled() {
        assertEquals(mockedDescribeAcls, describeAclsResult);
    }

    private void thenAdminClientCloseIsCalledOnce() {
        verify(adminClient).close();
    }
}