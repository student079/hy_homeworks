package domain.user;

public class User {
    private int userId;
    private String id;
    private String username;
    private String email;
    private String password;
    private int channel;

    public int getUserId() {
        return userId;
    }

public String getId() {return id;}
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public int getChannel() {
        return channel;
    }

    public User(int userId, String username, String id, String password, String email, int channel) {
        this.userId = userId;
        this.username = username;
        this.id = id;
        this.email = email;
        this.password = password;
        this.channel = channel;
    }

    @Override
    public String toString() {
        return username;
    }
}
