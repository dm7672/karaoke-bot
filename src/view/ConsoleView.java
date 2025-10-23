package view;


import viewmodel.*;


import java.util.List;
import java.util.Scanner;

public class ConsoleView implements View {
    private ViewModel viewModel;
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        System.out.println("Введите: <userId> <сообщение> (или \"exit\").");
        while (true) {
            String line = scanner.nextLine().trim();
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
                Integer userId = Integer.valueOf(parts[0]);
                String message = parts[1];

                // Передаём всё во ViewModel и получаем список ответов
                List<String> responses = viewModel.processMessage(userId, message);

                // Выводим каждый ответ
                for (String resp : responses) {
                    System.out.println(resp);
                }
            } catch (NumberFormatException ex) {
                System.out.println("userId должен быть числом.");
            }
        }
    }
}
