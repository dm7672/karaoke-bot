package viewmodel;

import java.util.List;

public class ReplyKeyboard {
    private final List<List<String>> rows;

    public ReplyKeyboard(List<List<String>> rows) {
        this.rows = rows;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public static ReplyKeyboard simple(List<String> row) {
        return new ReplyKeyboard(List.of(row));
    }
}
