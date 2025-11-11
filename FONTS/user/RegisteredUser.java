package user;

public class RegisteredUser extends User {
    private String username;
    private String password;

    public RegisteredUser(String id, String displayName, String createdAt, String username, String password) {
        super(id, displayName);
        this.username = username;
        this.password = password;
    }   

    public String getUsername() {
        return username;
    }   

    public String getPasswordHash() {
        return password;
    }   

    //Setters
    public void changeUsername(String name){
        username = name;
    }

    public void changePassword(String passwd){
        password = passwd;
    }


}
