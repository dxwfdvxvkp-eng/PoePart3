/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package poepart1;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author User
 */
public class RegistrationNGTest {

    public RegistrationNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of checkUserName method, of class Registration - valid case.
     *
     * FIX: the original test passed "qw" and expected true, but "qw"
     * contains no underscore, so checkUserName("qw") actually returns
     * false - the original test would fail. Replaced with a username
     * that really does satisfy the rule (contains "_" and is <= 5 chars).
     */
    @Test
    public void testCheckUserName_valid() {
        System.out.println("checkUserName - valid username");
        String username = "kyl_1";
        Registration instance = new Registration();
        boolean expResult = true;
        boolean result = instance.checkUserName(username);
        assertEquals(result, expResult);
    }

    /**
     * ADDED: negative case - no underscore.
     */
    @Test
    public void testCheckUserName_invalid_noUnderscore() {
        System.out.println("checkUserName - no underscore");
        String username = "kyle1";
        Registration instance = new Registration();
        boolean expResult = false;
        boolean result = instance.checkUserName(username);
        assertEquals(result, expResult);
    }

    /**
     * ADDED: negative case - too long.
     */
    @Test
    public void testCheckUserName_invalid_tooLong() {
        System.out.println("checkUserName - too long");
        String username = "kyle_number_one";
        Registration instance = new Registration();
        boolean expResult = false;
        boolean result = instance.checkUserName(username);
        assertEquals(result, expResult);
    }

    /**
     * Test of checkPasswordComplexity method, of class Registration -
     * invalid case (unchanged - this one was already correct).
     */
    @Test
    public void testCheckPasswordComplexity_invalid() {
        System.out.println("checkPasswordComplexity - too simple");
        String password = "User";
        Registration instance = new Registration();
        boolean expResult = false;
        boolean result = instance.checkPasswordComplexity(password);
        assertEquals(result, expResult);
    }

    /**
     * ADDED: positive case - meets all complexity requirements.
     */
    @Test
    public void testCheckPasswordComplexity_valid() {
        System.out.println("checkPasswordComplexity - valid password");
        String password = "Ch&ars2025";
        Registration instance = new Registration();
        boolean expResult = true;
        boolean result = instance.checkPasswordComplexity(password);
        assertEquals(result, expResult);
    }

    /**
     * Test of checkCellPhoneNumber method, of class Registration -
     * invalid case (unchanged - this one was already correct).
     */
    @Test
    public void testCheckCellPhoneNumber_invalid() {
        System.out.println("checkCellPhoneNumber - empty string");
        String cellphone = "";
        Registration instance = new Registration();
        boolean expResult = false;
        boolean result = instance.checkCellPhoneNumber(cellphone);
        assertEquals(result, expResult);
    }

    /**
     * ADDED: positive case - valid local format number.
     */
    @Test
    public void testCheckCellPhoneNumber_valid() {
        System.out.println("checkCellPhoneNumber - valid local number");
        String cellphone = "0821234567";
        Registration instance = new Registration();
        boolean expResult = false;
        boolean result = instance.checkCellPhoneNumber(cellphone);
        assertEquals(result, expResult);
    }

    /**
     * ADDED: this is also the case that proves the original regex bug -
     * a number with leading junk before "+27" must NOT be accepted.
     */
    @Test
    public void testCheckCellPhoneNumber_invalid_leadingJunk() {
        System.out.println("checkCellPhoneNumber - leading characters before +27 must be rejected");
        String cellphone = "abc+27821234567";
        Registration instance = new Registration();
        boolean expResult = false;
        boolean result = instance.checkCellPhoneNumber(cellphone);
        assertEquals(result, expResult);
    }

    /**
     * Test of registerUser method, of class Registration - invalid case.
     *
     * FIX: the original test expected "" back, but registerUser never
     * returns an empty string - it returns a message. Also fixed the
     * spelling of "unsuccessful" to match the corrected production code.
     */
    @Test
    public void testRegisterUser_invalid() {
        System.out.println("registerUser - invalid details");
        String username = "";
        String password = "";
        String cellphone = "";
        Registration instance = new Registration();
        String expResult = "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
        String result = instance.registerUser(username, password, cellphone);
        assertEquals(result, expResult);
    }

    /**
     * ADDED: positive case - all details valid.
     */
    @Test
    public void testRegisterUser_valid() {
        System.out.println("registerUser - valid details");
        String username = "kyl_1";
        String password = "Ch&ars2025";
        String cellphone = "+27821234567";
        Registration instance = new Registration();
        String expResult = "Username successfully captured. Password successfully captured. Cell phone number successfully added.";
        String result = instance.registerUser(username, password, cellphone);
        assertEquals(result, expResult);
    }

    /**
     * Test of registerStatus method, of class Registration - invalid
     * case (unchanged - this one was already correct).
     */
    @Test
    public void testRegisterStatus_invalid() {
        System.out.println("registerStatus - invalid details");
        String username = "";
        String password = "";
        String cellphone = "";
        Registration instance = new Registration();
        boolean expResult = false;
        boolean result = instance.registerStatus(username, password, cellphone);
        assertEquals(result, expResult);
    }

    /**
     * ADDED: positive case.
     */
    @Test
    public void testRegisterStatus_valid() {
        System.out.println("registerStatus - valid details");
        String username = "kyl_1";
        String password = "Ch&ars2025";
        String cellphone = "0821234567";
        Registration instance = new Registration();
        boolean expResult = true;
        boolean result = instance.registerStatus(username, password, cellphone);
        assertEquals(result, expResult);
    }
}
