package viewmodel;

import java.util.List;

public class InlineKeyboard {
    private final List<List<InlineButton>> rows;

    public InlineKeyboard(List<List<InlineButton>> rows) {
        this.rows = rows;
    }

    public List<List<InlineButton>> getRows() {
        return rows;
    }
}
