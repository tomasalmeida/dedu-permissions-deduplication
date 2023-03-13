package com.tomasalmeida.dedu.api.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
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

import com.tomasalmeida.dedu.api.system.PropertiesLoader;
import com.tomasalmeida.dedu.configuration.MainConfiguration;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class KafkaAdminClientTest {

    private static final String KAFKA_CONFIG_PATH = "/etc/path/to/file";
    private static final String DEDU_CONFIG_PATH = "/etc/path/to/another/file";
    private static final String PRINCIPAL = "principal";
    private static final String TOPIC = "topic-name";

    @Mock
    private AdminClient adminClient;
    @Mock
    private AclBindingFilter filter;
    @Mock
    private DescribeAclsResult mockedDescribeAcls;
    @Mock
    private Properties properties;
    @Mock
    private DescribeTopicsResult describeTopicResult;
    @Mock
    private TopicDescription topicDescription;

    private MockedStatic<AdminClient> adminClientMocked;
    private MockedStatic<PropertiesLoader> propertiesLoaderMocked;
    private MainConfiguration mainConfiguration;
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

    @Test
    void shouldReturnTrueIfTopicExists() throws Exception {
        givenConfigurationIsCreated();
        givenAdminclientIsCreated();
        givenAdminClientFindsTopic();

        final boolean topicExists = whenAdminClientTopicExists();

        assertTrue(topicExists);
    }

    @Test
    void shouldReturnFalseIfTopicDoesNotExist() throws Exception {
        givenConfigurationIsCreated();
        givenAdminclientIsCreated();
        givenAdminClientDoesNotFindTopic();

        final boolean topicExists = whenAdminClientTopicExists();

        assertFalse(topicExists);
    }

    private void givenAdminClientFindsTopic() {
        final Map<String, TopicDescription> topicDescriptionMap = Map.of(TOPIC, topicDescription);
        final KafkaFuture<Map<String, TopicDescription>> futureTopicDescription = KafkaFuture.completedFuture(topicDescriptionMap);
        when(adminClient.describeTopics(anyList())).thenReturn(describeTopicResult);
        when(describeTopicResult.allTopicNames()).thenReturn(futureTopicDescription);
    }

    private void givenAdminClientDoesNotFindTopic() {
        when(adminClient.describeTopics(anyList())).thenReturn(describeTopicResult);
        given(describeTopicResult.allTopicNames())
                .willAnswer(invocation -> {
                    throw new ExecutionException(new UnknownTopicOrPartitionException());
                });
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

    private void givenConfigurationIsCreated() throws IOException {
        mainConfiguration = new MainConfiguration(KAFKA_CONFIG_PATH, DEDU_CONFIG_PATH, PRINCIPAL);
    }

    private void givenAdminclientIsCreated() throws Exception {
        kafkaAdminClient = KafkaAdminClient.build(mainConfiguration);
    }

    private void whenDescribeAclsIsCalled() {
        describeAclsResult = kafkaAdminClient.describeAcls(filter);
    }

    private void whenKafkaAdminIsClosed() {
        kafkaAdminClient.close();
    }

    private boolean whenAdminClientTopicExists() {
        return kafkaAdminClient.topicExists(TOPIC);
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