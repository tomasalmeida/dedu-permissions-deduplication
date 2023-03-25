package com.tomasalmeida.dedu.permission.printers;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.configuration.MainConfiguration;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

public abstract class Printer {

    private static final String DEFAULT_PRINT_VALUE = "false";

    private final MainConfiguration mainConfiguration;

    protected Printer(@NotNull final MainConfiguration mainConfiguration) {
        this.mainConfiguration = mainConfiguration;
    }

    /**
     * Verify if should print a certain output
     *
     * @param property name
     * @return true if variable is set and equals true (case ignored), false otherwise
     */
    protected boolean shouldPrint(@NotNull final String property) {
        final String valueString = mainConfiguration.getDeduPropertyOrDefault(property, DEFAULT_PRINT_VALUE);
        return Boolean.parseBoolean(valueString);
    }

    protected String getPrintDestination(@NotNull final String property, @NotNull final String defaultValue) {
        return mainConfiguration.getDeduPropertyOrDefault(property, defaultValue);
    }

    public abstract void printCurrentBindings(@NotNull final List<PermissionBinding> bindings);

    public abstract void printActionableBindings(@NotNull final List<ActionablePermissionBinding> bindings);
}
