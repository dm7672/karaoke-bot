package model.domain.entities;

public class User {
    private final Integer userId;
    private final String platform;

    public User(Integer userId, String platform){
        this.userId = userId;
        this.platform = platform;
    }

    public String getPlatform() { return platform; }
    public Integer getUserId() { return userId; }
}
