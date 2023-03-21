package com.tomasalmeida.dedu.api.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.KafkaFuture;
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

import com.tomasalmeida.dedu.configuration.MainConfiguration;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class KafkaAdminClientTest {

    private static final String TOPIC = "topic-name";
    private static final String TOPIC2 = "topic-name2";

    @Mock
    private AdminClient adminClient;
    @Mock
    private AclBindingFilter filter;
    @Mock
    private DescribeAclsResult mockedDescribeAcls;
    @Mock
    private Properties properties;
    @Mock
    private MainConfiguration mainConfiguration;

    private MockedStatic<AdminClient> adminClientMocked;

    private KafkaAdminClient kafkaAdminClient;
    private DescribeAclsResult describeAclsResult;

    @BeforeEach
    void beforeEach() throws IOException {
        givenPropertiesCanBeLoaded();
        givenAdminClientIsCreated();
    }

    @AfterEach
    void afterEach() {
        thenAdminClientMockIsClosed();
    }

    @Test
    void shouldReturnDescribeAcls() throws Exception {
        givenAdminClientReturnsAcls();
        givenAdminclientIsCreated();

        whenDescribeAclsIsCalled();

        thenAdminClientDescribeAclsIsCalled();
    }

    @Test
    void shouldCloseCorrectly() throws Exception {
        givenAdminclientIsCreated();

        whenKafkaAdminIsClosed();

        thenAdminClientCloseIsCalledOnce();
    }

    @Test
    void shouldCloseCorrectlyWhenCloseIsCalledTwice() throws Exception {
        givenAdminclientIsCreated();

        whenKafkaAdminIsClosed();
        whenKafkaAdminIsClosed();

        thenAdminClientCloseIsCalledOnce();
    }

    @Test
    void shouldReturnTrueIfTopicExists() throws Exception {
        givenAdminclientIsCreated();
        givenKafkaAdminListTopics();

        final boolean topicExists = whenAdminClientTopicExists(TOPIC);

        assertTrue(topicExists);
    }

    @Test
    void shouldReturnFalseIfTopicDoesNotExist() throws Exception {
        givenAdminclientIsCreated();
        givenKafkaAdminListTopics();

        final boolean topicExists = whenAdminClientTopicExists(TOPIC2);

        assertFalse(topicExists);
    }

    @Test
    void shouldReturnTrueIfTopicPatternMatches() throws Exception {
        givenAdminclientIsCreated();
        givenKafkaAdminListTopics();

        final boolean topicMatches = whenAdminClientTopicPatternMatches(TOPIC);

        assertTrue(topicMatches);
    }

    @Test
    void shouldReturnFalseIfTopicPatternDoesNotMatch() throws Exception {
        givenAdminclientIsCreated();
        givenKafkaAdminListTopics();

        final boolean topicMatches = whenAdminClientTopicPatternMatches(TOPIC2);

        assertFalse(topicMatches);
    }

    private void givenKafkaAdminListTopics(){
        final Set<String> topicNames = Set.of(TOPIC);
        final KafkaFuture<Set<String>> futureTopicNames = KafkaFuture.completedFuture(topicNames);

        final ListTopicsResult listTopicResult  = mock(ListTopicsResult.class);

        when(adminClient.listTopics()).thenReturn(listTopicResult);
        when(listTopicResult.names()).thenReturn(futureTopicNames);
    }

    private void givenAdminClientIsCreated() {
        adminClientMocked = Mockito.mockStatic(AdminClient.class);
        adminClientMocked.when(() -> AdminClient.create(any(Properties.class)))
                .thenReturn(adminClient);
    }

    private void givenPropertiesCanBeLoaded() throws IOException {
        when(mainConfiguration.getKafkaConfigProperties()).thenReturn(properties);
    }

    private void givenAdminClientReturnsAcls() {
        when(adminClient.describeAcls(filter)).thenReturn(mockedDescribeAcls);
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

    private boolean whenAdminClientTopicExists(final String topic) {
        return kafkaAdminClient.isTopicPresent(topic);
    }

    private boolean whenAdminClientTopicPatternMatches(final String topic) {
        return kafkaAdminClient.doesTopicMatches(topic);
    }

    private void thenAdminClientMockIsClosed() {
        adminClientMocked.close();
    }

    private void thenAdminClientDescribeAclsIsCalled() {
        assertEquals(mockedDescribeAcls, describeAclsResult);
    }

    private void thenAdminClientCloseIsCalledOnce() {
        verify(adminClient).close();
    }
}