/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poepart1;

/**
 * Handles user registration: validating the username, password and
 * cellphone number, and storing the registered user's details.
 *
 * @author User
 */
public class Registration { // start of class

    String userName;
    String passWord;
    String cellPhone;

    /**
     * A valid username must contain an underscore and be no more than
     * five characters long.
     */
    public boolean checkUserName(String username) { // start of username method
        return username != null && username.contains("_")
                && username.length() <= 5;
    } // end of username method

    /**
     * A valid password must be at least 8 characters long and contain
     * at least one capital letter, one number and one special character.
     */
    public boolean checkPasswordComplexity(String password) { // start of password method
        return password != null && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[^a-zA-Z0-9].*");
    } // end of password method

    /**
     * A valid South African cellphone number is either in the
     * international format (+27 followed by 9 digits) or the local
     * format (0 followed by 9 digits).
     *
     * FIX: the original regular expressions were not anchored to the
     * start of the string (".*+27..." / ".*^0..."), which meant a
     * string such as "abc+27821234567" was incorrectly accepted.
     * Anchoring both patterns with ^ and $ makes sure the WHOLE string
     * has to match the expected format.
     */
    public boolean checkCellPhoneNumber(String cellphone) { // start of cellphone method
        return cellphone != null && cellphone.matches("^\\+27[0-9]{9}$");
    } // end of cellphone method

    public String registerUser(String username,
            String password,
            String cellphone) {

        if (checkUserName(username)
                && checkPasswordComplexity(password)
                && checkCellPhoneNumber(cellphone)) {

            this.userName = username;
            this.passWord = password;
            this.cellPhone = cellphone;

            return "Username successfully captured. Password successfully captured. Cell phone number successfully added.";
        } else {
            // FIX: corrected spelling ("unsucessfull" -> "unsuccessful")
            if (!checkUserName(username)) return "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
            if (!checkPasswordComplexity(password)) return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
            return "Cell phone number incorrectly formatted or does not contain international code.";
        }
    }

    public boolean registerStatus(String username, String password, String cellphone) {
        return checkUserName(username)
                && checkPasswordComplexity(password)
                && checkCellPhoneNumber(cellphone);
    }

} // end of class
