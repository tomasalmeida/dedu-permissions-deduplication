package com.tomasalmeida.dedu.permission.acls.printers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomasalmeida.dedu.configuration.MainConfiguration;
import com.tomasalmeida.dedu.permission.acls.AclBindingDeduplicator;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;
import com.tomasalmeida.dedu.permission.printers.Printer;
import com.tomasalmeida.dedu.permission.printers.exception.KafkaPrinterException;

public class CsvAclPrinter extends Printer {

    private static final String[] CURRENT_CSV_HEADER = {"resourceType", "resourceName", "patternType", "host", "permissionType",
            "operation", "principal"};
    private static final String[] ACTIONABLE_CSV_HEADER = {"action", "note", "resourceType", "resourceName", "patternType", "host",
            "permissionType", "operation", "principal"};
    static final String CURRENT_OUTPUT_ENABLE = "acl.current.output.csv.enable";
    static final String CURRENT_OUTPUT_FILE_PATH = "acl.current.output.csv.path";
    static final String CURRENT_OUTPUT_FILE_PATH_DEFAULT = "current.csv";
    static final String ACTIONABLE_OUTPUT_ENABLE = "acl.actionable.output.csv.enable";
    static final String ACTIONABLE_OUTPUT_FILE_PATH = "acl.actionable.output.csv.path";
    static final String ACTIONABLE_OUTPUT_FILE_PATH_DEFAULT = "actionable.csv";

    private static final Logger LOGGER = LoggerFactory.getLogger(AclBindingDeduplicator.class);

    public CsvAclPrinter(@NotNull final MainConfiguration mainConfiguration) {
        super(mainConfiguration);
    }

    @Override
    public void printCurrentBindings(@NotNull final List<PermissionBinding> bindings) {
        final boolean printCurrent = shouldPrint(CURRENT_OUTPUT_ENABLE);
        if (!printCurrent) {
            LOGGER.debug("CSV current printing is disabled");
            return;
        }
        final String filePath = getPrintDestination(CURRENT_OUTPUT_FILE_PATH, CURRENT_OUTPUT_FILE_PATH_DEFAULT);
        LOGGER.debug("CSV current bindings printing with destination [{}]", filePath);

        writeFile(filePath, CURRENT_CSV_HEADER, bindings, this::createCurrentBindingLine);
    }

    @Override
    public void printActionableBindings(@NotNull final List<ActionablePermissionBinding> bindings) {
        final boolean printCurrent = shouldPrint(ACTIONABLE_OUTPUT_ENABLE);
        if (!printCurrent) {
            LOGGER.debug("CSV actionable bindings printing is disabled");
            return;
        }
        final String filePath = getPrintDestination(ACTIONABLE_OUTPUT_FILE_PATH, ACTIONABLE_OUTPUT_FILE_PATH_DEFAULT);
        LOGGER.debug("CSV actionable bindings printing with destination [{}]", filePath);

        writeFile(filePath, ACTIONABLE_CSV_HEADER, bindings, this::createActionableBindingLine);
    }

    private Object[] createCurrentBindingLine(@NotNull final PermissionBinding binding) {
        return new Object[]{
                binding.getResourceType(),
                binding.getResourceName(),
                binding.getPatternType(),
                binding.getHost(),
                binding.getPermissionType(),
                binding.getOperation(),
                binding.getPrincipal()
        };
    }

    private Object[] createActionableBindingLine(@NotNull final ActionablePermissionBinding binding) {
        return new Object[]{
                binding.getAction(),
                binding.getNote(),
                binding.getResourceType(),
                binding.getResourceName(),
                binding.getPatternType(),
                binding.getHost(),
                binding.getPermissionType(),
                binding.getOperation(),
                binding.getPrincipal()
        };
    }

    private <T> void writeFile(@NotNull final String filePath,
                               @NotNull final String[] header,
                               @NotNull final List<T> lines,
                               @NotNull final Function<T, Object[]> lineCreator) {
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(header)
                .build();
        try (final FileWriter file = createFileWriter(filePath);
             final BufferedWriter sw = createWriter(file);
             final CSVPrinter printer = createPrinter(csvFormat, sw)) {

            for (final T line : lines) {
                printer.printRecord(lineCreator.apply(line));
            }

        } catch (final IOException e) {
            throw new KafkaPrinterException("Unable to print csv", e);
        }
    }

    @NotNull
    @VisibleForTesting
    BufferedWriter createWriter(@NotNull final FileWriter file) {
        return new BufferedWriter(file);
    }

    @NotNull
    @VisibleForTesting
    FileWriter createFileWriter(@NotNull final String filePath) throws IOException {
        return new FileWriter(filePath);
    }

    @NotNull
    @VisibleForTesting
    CSVPrinter createPrinter(@NotNull final CSVFormat csvFormat,
                             @NotNull final BufferedWriter sw) throws IOException {
        return new CSVPrinter(sw, csvFormat);
    }
}
