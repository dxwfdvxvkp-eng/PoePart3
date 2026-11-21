/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package poepart1;

import java.util.ArrayList;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Unit tests for Message.java.
 *
 * Part 2 tests are built from the test data and expected system
 * responses given in that part of the brief:
 * - Recipient "+27718693002" / message "Hi Mike, can you join us for
 *   dinner tonight?" is Test Data for Task 1 (send).
 * - Recipient "08575975889" / message "Hi Keegan, did you receive the
 *   payment?" is Test Data for Message 2 (discard) - it is also the
 *   worked example of an invalid recipient number (no international
 *   code).
 * - "00:0:HITONIGHT" is the worked example for a correct Message Hash.
 *
 * Part 3 tests are built from this brief's Test Data Message 1-5:
 * - Message 1: +27834557896 / "Did you get the cake?" / Sent
 * - Message 2: +27838884567 / "Where are you? You are late! I have
 *   asked you to be on time." / Stored
 * - Message 3: +27834484567 / "Yohoooo, I am at your gate." / Disregard
 * - Message 4: Message ID "0838884567" (developer-supplied, so the
 *   search-by-ID test has a known value to look up) / "It is dinner
 *   time!" / Sent
 * - Message 5: +27838884567 / "Ok, I am leaving without you." / Stored
 *
 * @author User
 */
public class MessageNGTest {

    // ----- checkRecipientCell -----

    @Test
    public void testCheckRecipientCell_valid() {
        System.out.println("checkRecipientCell - valid, international format (Test Data for Task 1)");
        Message instance = new Message();
        String expResult = "Cell phone number successfully captured.";
        String result = instance.checkRecipientCell("+27718693002");
        assertEquals(result, expResult);
    }

    @Test
    public void testCheckRecipientCell_invalid_noInternationalCode() {
        System.out.println("checkRecipientCell - invalid, no international code (Test Data for Message 2)");
        Message instance = new Message();
        String expResult = "Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.";
        String result = instance.checkRecipientCell("08575975889");
        assertEquals(result, expResult);
    }

    // ----- checkMessageLength -----

    @Test
    public void testCheckMessageLength_success() {
        System.out.println("checkMessageLength - 250 characters or fewer");
        Message instance = new Message();
        String expResult = "Message ready to send.";
        String result = instance.checkMessageLength("Hi Mike, can you join us for dinner tonight?");
        assertEquals(result, expResult);
    }

    @Test
    public void testCheckMessageLength_failure() {
        System.out.println("checkMessageLength - over 250 characters");
        Message instance = new Message();
        String tooLong = "A".repeat(260); // 10 characters over the limit
        String expResult = "Message exceeds 250 characters by 10; please reduce the size.";
        String result = instance.checkMessageLength(tooLong);
        assertEquals(result, expResult);
    }

    // ----- checkMessageID -----

    @Test
    public void testCheckMessageID_valid() {
        System.out.println("checkMessageID - exactly 10 characters");
        Message instance = new Message();
        boolean expResult = true;
        boolean result = instance.checkMessageID("0012345678");
        assertEquals(result, expResult);
    }

    @Test
    public void testCheckMessageID_invalid_tooLong() {
        System.out.println("checkMessageID - more than 10 characters");
        Message instance = new Message();
        boolean expResult = false;
        boolean result = instance.checkMessageID("001234567890");
        assertEquals(result, expResult);
    }

    @Test
    public void testGenerateMessageID_alwaysValid() {
        System.out.println("generateMessageID - always produces a 10-digit ID that passes checkMessageID");
        Message instance = new Message();
        String id = instance.generateMessageID();
        assertEquals(id.length(), 10);
        assertTrue(id.matches("\\d{10}"));
        assertTrue(instance.checkMessageID(id));
    }

    @Test
    public void testGenerateMessageID_unique() {
        System.out.println("generateMessageID - repeated IDs are never reused");
        Message instance = new Message();
        String firstId = instance.generateMessageID();
        instance.messageIds.add(firstId);
        String secondId = instance.generateMessageID();
        assertNotEquals(secondId, firstId);
    }

    // ----- createMessageHash -----

    @Test
    public void testCreateMessageHash_workedExample() {
        System.out.println("createMessageHash - matches the brief's worked example 00:0:HITONIGHT");
        Message instance = new Message();
        String hash = instance.createMessageHash("0012345678", 0, "Hi there, I will call you tonight");
        assertEquals(hash, "00:0:HITONIGHT");
    }

    @Test
    public void testCreateMessageHash_secondMessageNumber() {
        System.out.println("createMessageHash - message number component reflects messages already sent");
        Message instance = new Message();
        String hash = instance.createMessageHash("0857597588", 1, "Hi Keegan, did you receive the payment?");
        assertEquals(hash, "08:1:HIPAYMENT?");
    }

    // ----- returnTotalMessages -----

    @Test
    public void testReturnTotalMessages_startsAtZero() {
        System.out.println("returnTotalMessages - no messages sent yet");
        Message instance = new Message();
        assertEquals(instance.returnTotalMessages(), 0);
    }

    // =====================================================================
    // Part 3 tests - built on the brief's Test Data Message 1-5
    // =====================================================================

    /**
     * Captures Test Data Message 1-4 into a fresh Message instance, via
     * captureMessage() (never by hard-coding the arrays directly).
     */
    private Message captureTestMessages1to4() {
        Message instance = new Message();
        instance.captureMessage(null, "+27834557896", "Did you get the cake?", "Sent");
        instance.captureMessage(null, "+27838884567",
                "Where are you? You are late! I have asked you to be on time.", "Stored");
        instance.captureMessage(null, "+27834484567", "Yohoooo, I am at your gate.", "Disregard");
        instance.captureMessage("0838884567", null, "It is dinner time!", "Sent");
        return instance;
    }

    @Test
    public void testSentMessagesArrayPopulated() {
        System.out.println("Sent Messages array correctly populated - Test Data Message 1-4");
        Message instance = captureTestMessages1to4();
        ArrayList<String> expected = new ArrayList<>();
        expected.add("Did you get the cake?");
        expected.add("It is dinner time!");
        assertEquals(instance.getSentMessages(), expected);
    }

    @Test
    public void testDisplayLongestMessage() {
        System.out.println("Display the longest Message - Test Data Message 1-4");
        Message instance = captureTestMessages1to4();
        String expected = "Where are you? You are late! I have asked you to be on time.";
        assertEquals(instance.getLongestMessage(), expected);
    }

    @Test
    public void testSearchForMessageID() {
        System.out.println("Search for messageID - Test Data Message 4, id \"0838884567\"");
        Message instance = captureTestMessages1to4();
        String expected = "It is dinner time!";
        assertEquals(instance.searchByMessageID("0838884567"), expected);
    }

    @Test
    public void testSearchMessagesByRecipient() {
        System.out.println("Search all messages sent or stored for a particular recipient - +27838884567");
        Message instance = captureTestMessages1to4();
        // Test Data Message 5 also goes to +27838884567, and is Stored.
        instance.captureMessage(null, "+27838884567", "Ok, I am leaving without you.", "Stored");

        ArrayList<String> expected = new ArrayList<>();
        expected.add("Where are you? You are late! I have asked you to be on time.");
        expected.add("Ok, I am leaving without you.");

        assertEquals(instance.searchMessagesByRecipient("+27838884567"), expected);
    }

    @Test
    public void testDeleteStoredMessageByHash() {
        System.out.println("Delete a message using a message hash - Test Data Message 2");
        Message instance = captureTestMessages1to4();

        String hashOfMessage2 = instance.getStoredMessages().get(0).messageHash;

        String result = instance.deleteStoredMessageByHash(hashOfMessage2);

        String expected = "Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.";
        assertEquals(result, expected);
        assertTrue(instance.getStoredMessages().isEmpty());
    }

    @Test
    public void testDisplayStoredMessagesReport() {
        System.out.println("Display Report - shows Message Hash, Recipient and Message for every stored message");
        Message instance = captureTestMessages1to4();
        String report = instance.displayStoredMessagesReport();

        assertTrue(report.contains("Message Hash:"));
        assertTrue(report.contains("Recipient: +27838884567"));
        assertTrue(report.contains("Message: Where are you? You are late! I have asked you to be on time."));
    }

    @Test
    public void testSaveAndLoadStoredMessagesJSON() throws Exception {
        System.out.println("Read JSON file into an array and display it to the user");
        Message writer = captureTestMessages1to4();
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("storedMessagesTest", ".json");
        try {
            writer.saveStoredMessagesToJSON(tempFile.toString());

            Message reader = new Message();
            reader.loadStoredMessagesFromJSON(tempFile.toString());

            assertEquals(reader.getStoredMessages().size(), 1);
            assertEquals(reader.getStoredMessages().get(0).message,
                    "Where are you? You are late! I have asked you to be on time.");
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }
    }
}
