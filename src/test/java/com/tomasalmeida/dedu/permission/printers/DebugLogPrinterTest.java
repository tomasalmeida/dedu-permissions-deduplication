package com.tomasalmeida.dedu.permission.printers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import com.tomasalmeida.dedu.configuration.MainConfiguration;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class DebugLogPrinterTest {

    private static final String PERMISSION_STRING = "permission-binding-to-string";
    private static final String ACTION_PERMISSION_STRING = "actionable-permission-binding-to-string";
    @InjectMocks
    DebugLogPrinter debugLogPrinter;
    @Mock
    private MainConfiguration mainConfiguration;
    @Mock
    private PermissionBinding permissionBinding;
    @Mock
    private ActionablePermissionBinding actionablePermissionBinding;
    @Mock
    private Logger logger;

    @Test
    void shouldPrintCurrentBinding() {
        givenShouldPrintReturns();
        givenPermissionPrintsValue();

        whenPrintCurrentBindings();

        thenLoggerDebugIsCalledForCurrent();
    }

    @Test
    void shouldPrintActionableBinding() {
        givenShouldPrintReturns();
        givenActionablePermissionPrintsValue();

        whenPrintActionableBindings();

        thenLoggerDebugIsCalledForAction();
    }

    private void givenShouldPrintReturns() {
        debugLogPrinter = new DebugLogPrinter(mainConfiguration) {
            @Override
            protected boolean shouldPrint(@NotNull final String property) {
                return true;
            }
        };
    }

    private void givenPermissionPrintsValue() {
        when(permissionBinding.toString()).thenReturn(PERMISSION_STRING);
    }

    private void givenActionablePermissionPrintsValue() {
        when(actionablePermissionBinding.toString()).thenReturn(ACTION_PERMISSION_STRING);
    }

    private void whenPrintCurrentBindings() {
        debugLogPrinter.printCurrentBindings(List.of(permissionBinding));
    }

    private void whenPrintActionableBindings() {
        debugLogPrinter.printActionableBindings(List.of(actionablePermissionBinding));
    }

    private void thenLoggerDebugIsCalledForCurrent() {
        verify(logger).debug(PERMISSION_STRING);
    }

    private void thenLoggerDebugIsCalledForAction() {
        verify(logger).debug(ACTION_PERMISSION_STRING);
    }
}