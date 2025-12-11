package viewmodel;

public class BotMessage {
    private final String text;
    private final InlineKeyboard inlineKeyboard;
    private final ReplyKeyboard replyKeyboard;

    public BotMessage(String text, InlineKeyboard inlineKeyboard, ReplyKeyboard replyKeyboard) {
        this.text = text;
        this.inlineKeyboard = inlineKeyboard;
        this.replyKeyboard = replyKeyboard;
    }

    public String getText() {
        return text;
    }

    public InlineKeyboard getInlineKeyboard() {
        return inlineKeyboard;
    }

    public ReplyKeyboard getReplyKeyboard() {
        return replyKeyboard;
    }

    public static BotMessage textOnly(String text) {
        return new BotMessage(text, null, null);
    }
}
