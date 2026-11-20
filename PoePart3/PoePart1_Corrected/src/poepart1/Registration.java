package poepart1;
/** Registration validation and user information. */
public class Registration {
    protected String userName, passWord, cellPhone;
    public boolean checkUserName(String name) { return name != null && name.length() <= 5 && name.contains("_"); }
    public boolean checkPasswordComplexity(String password) {
        return password != null && password.length() >= 8 && password.matches(".*[A-Z].*")
            && password.matches(".*[0-9].*") && password.matches(".*[^a-zA-Z0-9].*");
    }
    /** South African international numbers: +27 and nine digits. */
    public boolean checkCellPhoneNumber(String phone) { return phone != null && phone.matches("\\+27[0-9]{9}"); }
    public String registerUser(String username, String password, String phone) {
        if (!checkUserName(username)) return "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
        if (!checkPasswordComplexity(password)) return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        if (!checkCellPhoneNumber(phone)) return "Cell number is incorrectly formatted or does not contain an international code; please correct the number and try again.";
        userName=username; passWord=password; cellPhone=phone;
        return "Username successfully captured. Password successfully captured. Cell phone number successfully added.";
    }
    public boolean registerStatus(String u,String p,String c) { return checkUserName(u)&&checkPasswordComplexity(p)&&checkCellPhoneNumber(c); }
}
