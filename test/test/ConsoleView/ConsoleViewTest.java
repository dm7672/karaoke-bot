package test.view;

import org.junit.jupiter.api.*;
import view.ConsoleView;
import viewmodel.ViewModel;

import java.io.*;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleViewTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private String getOutput() {
        return outContent.toString().replace("\r\n", "\n");
    }

    private ViewModel stubVmReturning(List<String> responses) {
        return new ViewModel(null, null, null, "test") {
            @Override
            public List<String> processMessage(Long userId, String msg) {
                return responses;
            }
        };
    }

    @Test
    void exitImmediatelyPrintsPromptAndExit() {
        String input = "exit\n";
        ConsoleView view = new ConsoleView(new Scanner(new ByteArrayInputStream(input.getBytes())));
        view.setViewModel(stubVmReturning(List.of()));

        view.start();

        String output = getOutput();
        assertTrue(output.contains("Введите: <userId> <сообщение>"));
        assertTrue(output.contains("Завершено."));
    }

    @Test
    void invalidFormatPrintsError() {
        String input = "123\nexit\n";
        ConsoleView view = new ConsoleView(new Scanner(new ByteArrayInputStream(input.getBytes())));
        view.setViewModel(stubVmReturning(List.of()));

        view.start();

        String output = getOutput();
        assertTrue(output.contains("Неверный формат"));
    }

    @Test
    void nonNumericUserIdPrintsError() {
        String input = "abc hello\nexit\n";
        ConsoleView view = new ConsoleView(new Scanner(new ByteArrayInputStream(input.getBytes())));
        view.setViewModel(stubVmReturning(List.of()));

        view.start();

        String output = getOutput();
        assertTrue(output.contains("userId должен быть числом."));
    }

    @Test
    void validInputDelegatesToViewModelAndPrintsResponses() {
        String input = "42 hi\nexit\n";
        ConsoleView view = new ConsoleView(new Scanner(new ByteArrayInputStream(input.getBytes())));
        view.setViewModel(new ViewModel(null, null, null, "test") {
            @Override
            public List<String> processMessage(Long userId, String msg) {
                assertEquals(42L, userId);
                assertEquals("hi", msg);
                return List.of("resp1", "resp2");
            }
        });

        view.start();

        String output = getOutput();
        assertTrue(output.contains("resp1"));
        assertTrue(output.contains("resp2"));
    }

    @Test
    void emptyLineIsIgnored() {
        String input = "\n42 hello\nexit\n";
        ConsoleView view = new ConsoleView(new Scanner(new ByteArrayInputStream(input.getBytes())));
        view.setViewModel(stubVmReturning(List.of("ok")));

        view.start();

        String output = getOutput();
        assertTrue(output.contains("ok"));
    }

    @Test
    void multipleValidInputsAllProcessed() {
        String input = "1 hello\n2 world\nexit\n";
        ConsoleView view = new ConsoleView(new Scanner(new ByteArrayInputStream(input.getBytes())));
        view.setViewModel(new ViewModel(null, null, null, "test") {
            @Override
            public List<String> processMessage(Long userId, String msg) {
                return List.of("resp:" + userId + ":" + msg);
            }
        });

        view.start();

        String output = getOutput();
        assertTrue(output.contains("resp:1:hello"));
        assertTrue(output.contains("resp:2:world"));
    }

    @Test
    void processMessageReturnsEmptyListPrintsNothingExtra() {
        String input = "5 test\nexit\n";
        ConsoleView view = new ConsoleView(new Scanner(new ByteArrayInputStream(input.getBytes())));
        view.setViewModel(new ViewModel(null, null, null, "test") {
            @Override
            public List<String> processMessage(Long userId, String msg) {
                return List.of(); // пустой список
            }
        });

        view.start();

        String output = getOutput();
        assertTrue(output.contains("Введите: <userId> <сообщение>"));
        assertTrue(output.contains("Завершено."));
        assertFalse(output.contains("null"));
    }

    @Test
    void veryLargeUserIdIsHandled() {
        String input = "999999999 hello\nexit\n";
        ConsoleView view = new ConsoleView(new Scanner(new ByteArrayInputStream(input.getBytes())));
        view.setViewModel(new ViewModel(null, null, null, "test") {
            @Override
            public List<String> processMessage(Long userId, String msg) {
                assertEquals(999999999L, userId);
                return List.of("big id ok");
            }
        });

        view.start();

        String output = getOutput();
        assertTrue(output.contains("big id ok"));
    }

    @Test
    void inputWithoutMessageShowsError() {
        String input = "42\nexit\n";
        ConsoleView view = new ConsoleView(new Scanner(new ByteArrayInputStream(input.getBytes())));
        view.setViewModel(stubVmReturning(List.of("should not be called")));

        view.start();

        String output = getOutput();
        assertTrue(output.contains("Неверный формат"));
        assertFalse(output.contains("should not be called"));
    }
}
