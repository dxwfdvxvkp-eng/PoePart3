package poepart1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class QuickChatJUnitTest {
    @Test void registrationValidation() {
        Registration r=new Registration();
        assertTrue(r.checkUserName("kyl_1"));
        assertFalse(r.checkUserName("invalidname"));
        assertTrue(r.checkPasswordComplexity("Ch&&sec@ke99!"));
        assertFalse(r.checkPasswordComplexity("password"));
        assertTrue(r.checkCellPhoneNumber("+27838968976"));
        assertFalse(r.checkCellPhoneNumber("08966553"));
    }
    @Test void login() {
        Login login=new Login();
        login.setFullName("Test","Student");
        login.registerUser("kyl_1","Ch&&sec@ke99!","+27838968976");
        assertTrue(login.loginUser("kyl_1","Ch&&sec@ke99!"));
        assertFalse(login.loginUser("kyl_1","wrong"));
        assertTrue(login.returnLoginStatus(true).contains("Test"));
    }
    @Test void hashAndValidation() {
        Message m=new Message();
        assertEquals("00:0:HITONIGHT",m.createMessageHash("0012345678",0,"Hi Mike, can you join us for dinner tonight"));
        assertTrue(m.checkMessageLength("a".repeat(251)).contains("by 1"));
        assertEquals("Message ready to send.",m.checkMessageLength("Hello"));
        assertThrows(IllegalArgumentException.class,()->m.addMessage("invalid","+27834557896","hello","Sent"));
    }
    @Test void collectionsSearchDeleteAndReport() throws Exception {
        Message m=new Message();
        Path path=Files.createTempFile("quickchat-junit",".json");
        try {
            m.setJsonPath(path);m.setSender("Alice");
            m.addMessage("0000000001","+27834557896","Did you get the cake?","Sent");
            String hash=m.addMessage("0000000002","+27838884567","Where are you? You are late!","Stored");
            m.addMessage("0000000003","+27838884567","Please hurry","Disregarded");
            assertEquals(1,m.returnTotalMessages());
            assertEquals("Where are you? You are late!",m.longestStoredMessage());
            assertTrue(m.searchMessageId("0000000001").contains("Did you get the cake?"));
            assertTrue(m.searchRecipient("+27838884567").contains("Where are you?"));
            assertFalse(m.searchRecipient("+27838884567").contains("Please hurry"));
            assertTrue(m.report().contains("Alice"));
            m.storeMessagesAsJSON(path.toString());
            Message loaded=new Message();loaded.setSender("Bob");loaded.setJsonPath(path);
            loaded.loadMessagesFromJSON(path.toString());
            assertEquals(3,loaded.messages.size());
            assertEquals("Alice",loaded.messageSenders.get(0));
            assertEquals(hash,loaded.msgHash.get(1));
            assertTrue(loaded.deleteByHash(hash).contains("successfully deleted"));
            assertEquals(2,loaded.messages.size());
            assertEquals(2,loaded.messageSenders.size());
        } finally {Files.deleteIfExists(path);}
    }
    @Test void exactFiveRubricMessagesAndReports() throws Exception {
        Message m = new Message();
        Path path = Files.createTempFile("quickchat-five-rubric", ".json");
        try {
            m.setJsonPath(path);
            m.setSender("Developer");
            m.addMessage("0000000001", "+27834557896", "Did you get the cake?", "Sent");
            String storedHash = m.addMessage("0000000002", "+27838884567",
                "Where are you? You are late! I have asked you to be on time.", "Stored");
            m.addMessage("0000000003", "+27834484567", "Yohoooo, I am at your gate.", "Disregarded");
            m.addMessage("0000000004", "+27838884567", "It is dinner time!", "Sent");
            m.addMessage("0000000005", "+27838884567", "Ok, I am leaving without you.", "Stored");
            assertEquals(java.util.List.of("Did you get the cake?", "It is dinner time!"), m.sentMessages);
            assertEquals(java.util.List.of("Yohoooo, I am at your gate."), m.disregardedMessages);
            assertEquals(java.util.List.of(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."), m.storedMessages);
            assertEquals(5, m.messageIdsList.size());
            assertEquals(5, m.msgHash.size());
            assertEquals("Where are you? You are late! I have asked you to be on time.", m.longestStoredMessage());
            assertTrue(m.searchMessageId("0000000004").contains("It is dinner time!"));
            assertTrue(m.searchRecipient("+27838884567").contains("Ok, I am leaving without you."));
            assertTrue(m.report().contains("Message Hash:"));
            assertTrue(m.report().contains("Recipient:"));
            assertTrue(m.report().contains("Message:"));
            m.storeMessagesAsJSON(path.toString());
            Message reloaded = new Message();
            reloaded.setJsonPath(path);
            reloaded.loadMessagesFromJSON(path.toString());
            assertEquals(m.messages, reloaded.messages);
            assertEquals(m.msgHash, reloaded.msgHash);
            assertTrue(reloaded.deleteByHash(storedHash).contains("successfully deleted"));
            assertEquals(4, reloaded.messages.size());
            assertFalse(reloaded.storedMessages.contains("Where are you? You are late! I have asked you to be on time."));
        } finally { Files.deleteIfExists(path); }
    }

    @Test void duplicateIdsAndInvalidJsonDoNotMutateArrays() throws Exception {
        Message m = new Message();
        m.addMessage("1234567890", "+27834557896", "Hello", "Sent");
        assertFalse(m.isAvailableMessageID("1234567890"));
        assertThrows(IllegalArgumentException.class, () ->
            m.addMessage("1234567890", "+27834557896", "Again", "Sent"));
        Path path = Files.createTempFile("quickchat-bad", ".json");
        try {
            Files.writeString(path, "[{\"messageId\":\"0000000001\",\"recipient\":\"bad\",\"message\":\"Hello\",\"status\":\"Stored\"}]");
            assertThrows(IllegalArgumentException.class, () -> m.loadMessagesFromJSON(path.toString()));
            assertEquals(1, m.messages.size());
        } finally { Files.deleteIfExists(path); }
    }

    @Test void jsonEscapes() throws Exception {
        Path path=Files.createTempFile("quickchat-escapes",".json");
        try {
            Message m=new Message();m.setSender("First");
            m.addMessage("9000000001","+27834557896","A \\\"quote\\\", slash \\\\ and newline\\nnext","Stored");
            m.storeMessagesAsJSON(path.toString());
            Message loaded=new Message();loaded.loadMessagesFromJSON(path.toString());
            assertEquals(m.messages.get(0),loaded.messages.get(0));
            assertEquals("First",loaded.messageSenders.get(0));
            assertThrows(IllegalArgumentException.class,()->JsonMessageParser.parse("[{bad json}]"));
        } finally {Files.deleteIfExists(path);}
    }
}
