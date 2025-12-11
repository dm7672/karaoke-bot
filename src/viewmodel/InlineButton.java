package viewmodel;

public class InlineButton {
    private final String text;
    private final String callbackData;

    public InlineButton(String text, String callbackData) {
        this.text = text;
        this.callbackData = callbackData;
    }

    public String getText() {
        return text;
    }

    public String getCallbackData() {
        return callbackData;
    }
}
