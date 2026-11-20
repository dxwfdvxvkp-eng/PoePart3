package poepart1;
public class Login extends Registration {
    private String firstName="", lastName="";
    public void setFullName(String first, String last) { firstName=first; lastName=last; }
    public boolean loginUser(String name,String password) { return userName!=null && userName.equals(name) && passWord.equals(password); }
    public String returnLoginStatus(boolean success) { return success ? "Welcome " + firstName + ", " + lastName + " it is great to see you again." : "Username or password incorrect, please try again."; }
}
