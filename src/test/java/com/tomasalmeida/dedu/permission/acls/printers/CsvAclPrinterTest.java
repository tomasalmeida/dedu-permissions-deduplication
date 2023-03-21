package com.tomasalmeida.dedu.permission.acls.printers;

import static com.tomasalmeida.dedu.permission.acls.printers.CsvAclPrinter.ACTIONABLE_OUTPUT_ENABLE;
import static com.tomasalmeida.dedu.permission.acls.printers.CsvAclPrinter.ACTIONABLE_OUTPUT_FILE_PATH;
import static com.tomasalmeida.dedu.permission.acls.printers.CsvAclPrinter.ACTIONABLE_OUTPUT_FILE_PATH_DEFAULT;
import static com.tomasalmeida.dedu.permission.acls.printers.CsvAclPrinter.CURRENT_OUTPUT_ENABLE;
import static com.tomasalmeida.dedu.permission.acls.printers.CsvAclPrinter.CURRENT_OUTPUT_FILE_PATH;
import static com.tomasalmeida.dedu.permission.acls.printers.CsvAclPrinter.CURRENT_OUTPUT_FILE_PATH_DEFAULT;
import static org.apache.kafka.common.acl.AclOperation.READ;
import static org.apache.kafka.common.acl.AclPermissionType.ALLOW;
import static org.apache.kafka.common.resource.PatternType.LITERAL;
import static org.apache.kafka.common.resource.ResourceType.TOPIC;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.resource.ResourcePattern;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tomasalmeida.dedu.configuration.MainConfiguration;
import com.tomasalmeida.dedu.permission.acls.AclPermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class CsvAclPrinterTest {

    private static final String TOPIC_NAME = "topic-topic";
    private static final String PRINCIPAL_NAME = "principal";
    private static final String HOST = "*";
    private static final String NOTE = "note";

    @Captor
    ArgumentCaptor<Object[]> recordCaptor;
    @Mock
    private MainConfiguration mainConfiguretion;
    @Mock
    private CSVPrinter csvPrinter;
    @Mock
    private BufferedWriter writer;
    @Mock
    private FileWriter fileWriter;
    private CsvAclPrinter csvAclPrinter;
    private AclPermissionBinding currentBinding;
    private ActionablePermissionBinding actionableBinding;

    @Test
    void shouldPrintCurrentBindings() throws IOException {
        givenMainConfigReturnsValues();
        givenPrinterIsCreated();
        givenPermissionBindingIsFulfilled();

        whenCurrentBindingIsPrinted();

        thenLineIsPrinted();
    }

    @Test
    void shouldPrintActionableBindings() throws IOException {
        givenMainConfigReturnsActionValues();
        givenPrinterIsCreated();
        givenActionableBindingIsCreated();

        whenActionableBindingIsPrinted();

        thenActionLineIsPrinted();
    }

    private void givenMainConfigReturnsValues() {
        when(mainConfiguretion.getDeduPropertyOrDefault(CURRENT_OUTPUT_ENABLE, "false")).thenReturn("true");
        when(mainConfiguretion.getDeduPropertyOrDefault(CURRENT_OUTPUT_FILE_PATH, CURRENT_OUTPUT_FILE_PATH_DEFAULT))
                .thenReturn(CURRENT_OUTPUT_FILE_PATH_DEFAULT);
    }

    private void givenMainConfigReturnsActionValues() {
        when(mainConfiguretion.getDeduPropertyOrDefault(ACTIONABLE_OUTPUT_ENABLE, "false")).thenReturn("true");
        when(mainConfiguretion.getDeduPropertyOrDefault(ACTIONABLE_OUTPUT_FILE_PATH, ACTIONABLE_OUTPUT_FILE_PATH_DEFAULT))
                .thenReturn(ACTIONABLE_OUTPUT_FILE_PATH_DEFAULT);
    }

    private void givenPrinterIsCreated() {
        csvAclPrinter = new CsvAclPrinter(mainConfiguretion) {

            @Override
            @NotNull CSVPrinter createPrinter(final @NotNull CSVFormat csvFormat, final @NotNull BufferedWriter sw) {
                return csvPrinter;
            }

            @Override
            @NotNull BufferedWriter createWriter(final @NotNull FileWriter file) {
                return writer;
            }

            @Override
            @NotNull FileWriter createFileWriter(final @NotNull String filePath) {
                return fileWriter;
            }
        };
    }

    private void givenPermissionBindingIsFulfilled() {
        final ResourcePattern pattern = new ResourcePattern(TOPIC, TOPIC_NAME, LITERAL);
        final AccessControlEntry entry = new AccessControlEntry(PRINCIPAL_NAME, HOST, READ, ALLOW);
        currentBinding = new AclPermissionBinding(new AclBinding(pattern, entry));
    }

    private void givenActionableBindingIsCreated() {
        givenPermissionBindingIsFulfilled();
        actionableBinding = new ActionablePermissionBinding(currentBinding, ActionablePermissionBinding.Action.DELETE, NOTE);
    }

    private void whenCurrentBindingIsPrinted() {
        csvAclPrinter.printCurrentBindings(List.of(currentBinding));
    }

    private void whenActionableBindingIsPrinted() {
        csvAclPrinter.printActionableBindings(List.of(actionableBinding));
    }

    private void thenLineIsPrinted() throws IOException {
        verify(csvPrinter).printRecord(recordCaptor.capture());
        final Object[] line = recordCaptor.getValue();
        assertArrayEquals(new Object[]{TOPIC, TOPIC_NAME, LITERAL, HOST, ALLOW.toString(), READ.toString(), PRINCIPAL_NAME}, line);
    }

    private void thenActionLineIsPrinted() throws IOException {
        verify(csvPrinter).printRecord(recordCaptor.capture());
        final Object[] line = recordCaptor.getValue();
        assertArrayEquals(new Object[]{ActionablePermissionBinding.Action.DELETE, NOTE, TOPIC, TOPIC_NAME, LITERAL, HOST, ALLOW.toString(),
                READ.toString(), PRINCIPAL_NAME}, line);
    }
}