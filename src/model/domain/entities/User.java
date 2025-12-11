package model.domain.entities;

public class User {
    private final Long userId;
    private final String platform;
    private transient String pendingAction;

    public User(Long userId, String platform){
        this.userId = userId;
        this.platform = platform;
    }

    public String getPlatform() { return platform; }
    public Long getUserId() { return userId; }

    public String getPendingAction() { return pendingAction; }
    public void setPendingAction(String pendingAction) { this.pendingAction = pendingAction; }
}
