package com.tomasalmeida.dedu.permission.printers;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomasalmeida.dedu.configuration.MainConfiguration;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

/**
 * Debug log class
 */
public class DebugLogPrinter extends Printer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugLogPrinter.class);

    private static final String CURRENT_OUTPUT_ENABLE = "acl.current.output.log.enable";
    private static final String ACTIONABLE_OUTPUT_ENABLE = "acl.actionable.output.log.enable";

    public DebugLogPrinter(@NotNull final MainConfiguration mainConfiguration) {
        super(mainConfiguration);
    }

    @Override
    public void printCurrentBindings(@NotNull final List<PermissionBinding> bindings) {
        if (shouldPrint(CURRENT_OUTPUT_ENABLE)) {
            bindings.forEach(binding -> LOGGER.debug(binding.toString()));
        }
    }

    @Override
    public void printActionableBindings(@NotNull final List<ActionablePermissionBinding> bindings) {
        if (shouldPrint(ACTIONABLE_OUTPUT_ENABLE)) {
            bindings.forEach(binding -> LOGGER.debug(binding.toString()));
        }
    }
}
