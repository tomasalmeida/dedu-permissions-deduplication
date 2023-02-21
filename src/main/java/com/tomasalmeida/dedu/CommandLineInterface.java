package com.tomasalmeida.dedu;

import static java.lang.System.exit;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

public class CommandLineInterface {

    @VisibleForTesting
    static final String OPTION_CONFIG_FILE = "config-file";
    private static final String OPTION_CONFIG_FILE_DESC = "Config file path";
    @VisibleForTesting
    static final String OPTION_HELP = "help";
    private static final String OPTION_HELP_DESC = "Show usage options";

    private final Options optionsList;
    private final DefaultParser optionsParser;
    private final Deduplicator deduplicator;

    public CommandLineInterface(final Deduplicator deduplicator) {
        this.optionsList = buildOptionsList();
        this.optionsParser = new DefaultParser();
        this.deduplicator = deduplicator;
    }

    public static void main(final String[] args) {
        final CommandLineInterface cli = new CommandLineInterface(new Deduplicator());
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
        parseArgs(args);
        deduplicator.run();
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
                .required(false)
                .build();
        final Options options = new Options();
        options.addOption(configFileOption);
        options.addOption(helpOption);
        return options;
    }
}
