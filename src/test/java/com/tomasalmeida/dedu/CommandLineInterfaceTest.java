package com.tomasalmeida.dedu;

import static com.tomasalmeida.dedu.CommandLineInterface.OPTION_CONFIG_FILE;
import static com.tomasalmeida.dedu.CommandLineInterface.OPTION_HELP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;

import java.io.PrintStream;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tomasalmeida.dedu.com.tomasalmeida.tests.system.DisabledExitSecurityManager;
import com.tomasalmeida.dedu.com.tomasalmeida.tests.system.SystemExitPreventedException;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class CommandLineInterfaceTest {

    @Mock
    private PrintStream output;


    private String[] args;
    private DisabledExitSecurityManager securityManager;
    private Optional<Deduplicator> deduplicator;

    @Test
    void shouldShowHelpIfRequested() {
        givenSystemExitIsDisabled();
        givenOutputIsSet();
        givenHelpArgIsPassed();

        whenCliIsCalledWithArgs();

        thenHelpIsShown();
        thenFinishedWithExit(0);
    }

    @Test
    void shouldShowHelpIfNoArgs() {
        givenSystemExitIsDisabled();
        givenOutputIsSet();
        givenArgIsEmpty();

        whenCliIsCalledWithArgs();

        thenHelpIsShown();
        thenFinishedWithExit(1);
    }

    @Test
    void shouldCallDeduplicatorIfArgsAreCorrect() {
        givenSystemExitIsDisabled();
        givenOutputIsSet();
        givenArgIsPassed();

        whenCliIsCalledWithArgs();

        thenDeduplicatorIsCalled();
        thenFinishedWithExit(0);
    }

    private void givenSystemExitIsDisabled() {
        final SecurityManager originalSecurityManager = System.getSecurityManager();
        securityManager = new DisabledExitSecurityManager(originalSecurityManager);
        System.setSecurityManager(securityManager);
    }

    private void givenOutputIsSet() {
        System.setOut(output);
    }

    private void givenHelpArgIsPassed() {
        args = new String[]{"--" + OPTION_HELP};
    }

    private void givenArgIsEmpty() {
        args = new String[]{};
    }

    private void givenArgIsPassed() {
        args = new String[]{"--" + OPTION_CONFIG_FILE, "file"};
    }

    private void whenCliIsCalledWithArgs() {
        try (final MockedConstruction<Deduplicator> mocked = Mockito.mockConstruction(Deduplicator.class)) {
            assertThrows(SystemExitPreventedException.class, () -> CommandLineInterface.main(args));
            deduplicator = mocked.constructed().stream().findAny();
        }
    }

    private void thenDeduplicatorIsCalled() {
        assertTrue(deduplicator.isPresent());
        verify(deduplicator.get()).run();
    }

    private void thenHelpIsShown() {
        verify(output).write(any(), anyInt(), anyInt());
    }

    private void thenFinishedWithExit(final int exitStatus) {
        assertEquals(exitStatus, securityManager.getFirstExitStatusCode());
    }
}