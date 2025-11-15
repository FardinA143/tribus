package user;


public abstract class User {
    protected String id;
    protected String displayName;

    public User(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    // Getters
    public String getId(){
        return id;
    }

    public String getDisplayName() {
         return displayName; 
    }

    //Setters
    public void changeDisplayName(String name) {
        displayName = name;
    }

}