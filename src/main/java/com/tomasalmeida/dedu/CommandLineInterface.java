package com.tomasalmeida.dedu;

import static java.lang.System.exit;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Main CLI class to read args from command line and call deduplicator with right parameters
 */
public class CommandLineInterface {

    public static final String OPTION_CONFIG_FILE = "config-file";
    private static final String OPTION_CONFIG_FILE_DESC = "Kafka Config file path";
    public static final String OPTION_HELP = "help";
    private static final String OPTION_HELP_DESC = "Show usage options";
    public static final String OPTION_PRINCIPAL = "principal";
    private static final String OPTION_PRINCIPAL_DESC = "Optimize permissions for a given principal";

    private final Options optionsList;
    private final DefaultParser optionsParser;

    public CommandLineInterface() {
        this.optionsList = buildOptionsList();
        this.optionsParser = new DefaultParser();
    }

    public static void main(final String[] args) {
        final CommandLineInterface cli = new CommandLineInterface();
        try {
            cli.run(args);
        } catch (final ParseException e) {
            System.err.println("Error: " + e.getMessage());
            cli.printUsage(1);
        }
        exit(0);
    }

    @VisibleForTesting
    void run(final String[] args) throws ParseException {
        printUsageIfRequested(args);
        final CommandLine commandLine = parseArgs(args);
        runDeduplicator(commandLine);
    }

    private void runDeduplicator(final CommandLine commandLine) {
        final String configFile = commandLine.getOptionValue(OPTION_CONFIG_FILE);
        final String principal = commandLine.getOptionValue(OPTION_PRINCIPAL);

        try (final Deduplicator deduplicator = Deduplicator.build(configFile, principal)) {
            deduplicator.run();
        } catch (final IOException e) {
            System.err.println("Error: " + e.getMessage());
            printUsage(1);
        }
    }

    private void printUsageIfRequested(final String[] args) {
        final boolean helpRequested = Arrays.asList(args).contains("--" + OPTION_HELP);
        if (helpRequested) {
            printUsage(0);
        }
    }

    @NotNull
    private CommandLine parseArgs(final String[] args) throws ParseException {
        return optionsParser.parse(optionsList, args);
    }

    private void printUsage(final int exitStatus) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("cli", optionsList, true);
        exit(exitStatus);
    }

    @NotNull
    private Options buildOptionsList() {
        final Option configFileOption = Option.builder()
                .longOpt(OPTION_CONFIG_FILE)
                .desc(OPTION_CONFIG_FILE_DESC)
                .required(true)
                .numberOfArgs(1)
                .type(String.class)
                .build();
        final Option helpOption = Option.builder()
                .longOpt(OPTION_HELP)
                .desc(OPTION_HELP_DESC)
                .build();
        final Option userOption = Option.builder()
                .longOpt(OPTION_PRINCIPAL)
                .desc(OPTION_PRINCIPAL_DESC)
                .numberOfArgs(1)
                .build();
        final Options options = new Options();
        options.addOption(configFileOption);
        options.addOption(helpOption);
        options.addOption(userOption);
        return options;
    }
}
