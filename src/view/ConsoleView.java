package view;

import viewmodel.ViewModel;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConsoleView implements View {
    private ViewModel viewModel;
    private final Scanner scanner;

    public ConsoleView() {
        this.scanner = new Scanner(System.in);
    }

    public ConsoleView(Scanner scanner, ViewModel vm) {
        this.scanner = scanner;
        this.viewModel = vm;
    }


    @Override
    public void start() {
        System.out.println("Введите: <userId> <сообщение> (или \"exit\").");
        while (true) {
            String line;
            try {
                line = scanner.nextLine().trim();
            } catch (NoSuchElementException e) {
                break;
            }

            if (line.equalsIgnoreCase("exit")) {
                System.out.println("Завершено.");
                break;
            }
            if (line.isEmpty()) continue;

            String[] parts = line.split(" ", 2);
            if (parts.length < 2) {
                System.out.println("Неверный формат. Используйте: <userId> <сообщение>");
                continue;
            }

            try {
                Long userId = Long.valueOf(parts[0]);
                String message = parts[1];

                List<String> responses = viewModel.processMessage(userId, message);

                for (String resp : responses) {
                    System.out.println(resp);
                }
            } catch (NumberFormatException ex) {
                System.out.println("userId должен быть числом.");
            }
        }
    }
}
