package com.tomasalmeida.dedu;

import static com.tomasalmeida.dedu.CommandLineInterface.OPTION_CONFIG_FILE;
import static com.tomasalmeida.dedu.CommandLineInterface.OPTION_DEDU_CONFIG_FILE;
import static com.tomasalmeida.dedu.CommandLineInterface.OPTION_HELP;
import static com.tomasalmeida.dedu.CommandLineInterface.OPTION_PRINCIPAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;

import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tomasalmeida.dedu.com.tomasalmeida.tests.system.DisabledExitSecurityManager;
import com.tomasalmeida.dedu.com.tomasalmeida.tests.system.SystemExitPreventedException;

@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(MockitoExtension.class)
class CommandLineInterfaceTest {

    private static final String KAFKA_CONFIG_PATH = "src/test/resources/config.properties";
    private static final String DEDU_CONFIG_PATH = "src/test/resources/config.properties";
    private static final String PRINCIPAL_NAME = "User:alice";

    @Mock
    private PrintStream output;

    @Mock
    private Deduplicator deduplicator;

    private String[] args;
    private MockedStatic<Deduplicator> deduplicatorMockedStatic;

    private DisabledExitSecurityManager disabledExitSecurityManager;
    private SecurityManager originalSecurityManager;

    @Test
    void shouldShowHelpIfRequested() {
        givenSystemExitIsDisabled();
        givenOutputIsSet();
        givenHelpArgIsPassed();

        whenCliIsCalledWithArgs();

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
    void shouldCallDeduplicatorIfArgsAreCorrect() throws ExecutionException, InterruptedException {
        givenSystemExitIsDisabled();
        givenOutputIsSet();
        givenArgIsPassed();
        givenDeduplicatorBuilderReturnsInstance();

        whenCliIsCalledWithArgs();

        thenDeduplicatorIsCalled();
        thenFinishedWithExit(0);
        thenDeduplicatorBuilderIsFinished();
    }

    private void givenSystemExitIsDisabled() {
        originalSecurityManager = System.getSecurityManager();
        disabledExitSecurityManager = new DisabledExitSecurityManager(originalSecurityManager);
        System.setSecurityManager(disabledExitSecurityManager);
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
        args = new String[]{"--" + OPTION_CONFIG_FILE, KAFKA_CONFIG_PATH,
                "--" + OPTION_DEDU_CONFIG_FILE, DEDU_CONFIG_PATH,
                "--" + OPTION_PRINCIPAL, PRINCIPAL_NAME};
    }

    private void givenDeduplicatorBuilderReturnsInstance() {
        deduplicatorMockedStatic = Mockito.mockStatic(Deduplicator.class);
        deduplicatorMockedStatic.when(() -> Deduplicator.build(KAFKA_CONFIG_PATH, DEDU_CONFIG_PATH, PRINCIPAL_NAME))
                .thenReturn(deduplicator);
    }

    private void whenCliIsCalledWithArgs() {
        try {
            assertThrows(SystemExitPreventedException.class, () -> CommandLineInterface.main(args));
        } finally {
            System.setSecurityManager(originalSecurityManager);
        }
    }

    private void thenDeduplicatorIsCalled() throws ExecutionException, InterruptedException {
        verify(deduplicator).run();
    }

    private void thenHelpIsShown() {
        verify(output).write(any(), anyInt(), anyInt());
    }

    private void thenFinishedWithExit(final int exitStatus) {
        assertEquals(exitStatus, disabledExitSecurityManager.getFirstExitStatusCode());
    }

    private void thenDeduplicatorBuilderIsFinished() {
        deduplicatorMockedStatic.close();
    }
}