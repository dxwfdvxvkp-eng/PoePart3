package poepart1;
import java.nio.file.Files;
import java.nio.file.Path;
/** Dependency-free automated regression tests; throws AssertionError on failure. */
public class QuickChatTests {
    private static int passed=0;
    static void check(boolean ok,String label) { if(!ok)throw new AssertionError(label);passed++; }
    public static void main(String[] args) throws Exception {
        Login l=new Login(); l.setFullName("Test","Student");
        check(l.checkUserName("kyl_1"),"username valid");
        check(!l.checkUserName("kyle!!!!!!"),"username invalid");
        check(l.checkPasswordComplexity("Ch&&sec@ke99!"),"password valid");
        check(!l.checkPasswordComplexity("password"),"password invalid");
        check(l.checkCellPhoneNumber("+27838968976"),"phone valid");
        check(!l.checkCellPhoneNumber("08966553"),"phone invalid");
        l.registerUser("kyl_1","Ch&&sec@ke99!","+27838968976");
        check(l.loginUser("kyl_1","Ch&&sec@ke99!"),"login success");
        check(!l.loginUser("kyl_1","bad"),"login fail");
        Message m=new Message();
        Path file=Files.createTempFile("quickchat-test",".json");m.setJsonPath(file);
        check(m.checkMessageID("0123456789"),"id valid");
        check(!m.checkMessageID("123"),"id invalid");
        check(m.createMessageHash("0012345678",0,"Hi Mike, can you join us for dinner tonight").equals("00:0:HITONIGHT"),"hash");
        check(m.checkMessageLength("a".repeat(251)).contains("by 1"),"length failure");
        check(m.checkMessageLength("Hello").equals("Message ready to send."),"length success");
        m.addMessage("0000000001","+27834557896","Did you get the cake?","Sent");
        String h=m.addMessage("0000000002","+27838884567","Where are you? You are late! I have asked you to be on time.","Stored");
        m.addMessage("0000000003","+27834484567","Yohoooo, I am at your gate.","Disregarded");
        m.addMessage("0000000004","+27838884567","It is dinner time!","Sent");
        m.addMessage("0000000005","+27838884567","Ok, I am leaving without you.","Stored");
        check(m.messages.equals(java.util.List.of(
            "Did you get the cake?",
            "Where are you? You are late! I have asked you to be on time.",
            "Yohoooo, I am at your gate.",
            "It is dinner time!",
            "Ok, I am leaving without you.")), "five exact rubric message texts");
        check(m.recipientNumber.equals(java.util.List.of(
            "+27834557896", "+27838884567", "+27834484567",
            "+27838884567", "+27838884567")), "five rubric recipients (message 4 normalised)");
        check(m.messageStatus.equals(java.util.List.of(
            "Sent", "Stored", "Disregarded", "Sent", "Stored")), "five exact rubric statuses");
        check(m.sentMessages.equals(java.util.List.of("Did you get the cake?", "It is dinner time!")), "exact sent array");
        check(m.storedMessages.equals(java.util.List.of(
            "Where are you? You are late! I have asked you to be on time.",
            "Ok, I am leaving without you.")), "exact stored array");
        check(m.disregardedMessages.equals(java.util.List.of("Yohoooo, I am at your gate.")), "exact disregarded array");
        check(m.messageIdsList.size()==5 && new java.util.HashSet<>(m.messageIdsList).size()==5, "IDs unique");
        check(m.msgHash.size()==5 && new java.util.HashSet<>(m.msgHash).size()==5, "hashes unique");
        check(m.sentMessages.size()==2,"sent array");
        check(m.disregardedMessages.size()==1,"discard array");
        check(m.storedMessages.size()==2,"stored array");
        check(m.longestStoredMessage().startsWith("Where are you?"),"longest stored");
        check(m.searchMessageId("0000000004").contains("It is dinner time!"),"search ID");
        check(m.searchRecipient("+27838884567").contains("Ok, I am leaving"),"search recipient");
        check(m.report().contains("Message Hash:") && m.report().contains("Recipient:"),"report");
        m.storeMessagesAsJSON(file.toString());
        Message loaded=new Message();loaded.setJsonPath(file);loaded.loadMessagesFromJSON(file.toString());
        check(loaded.messages.size()==5,"JSON loaded");
        check(loaded.msgHash.get(1).equals(h),"hash preserved");
        String newHash=loaded.addMessage("0000000006","+27838884567","New message after reload","Stored");
        check(newHash.startsWith("00:5:"),"message sequence continues after JSON reload");
        check(loaded.deleteByHash(h).contains("successfully deleted"),"delete hash");
        check(loaded.messages.size()==5,"delete aligned arrays");
        check(loaded.messageSenders.size()==5,"sender array stays aligned after delete");
        Message escaped=new Message(); escaped.setSender("Original Sender");
        escaped.addMessage("9000000001","+27834557896","Quoted \"text\" and \\ slash, newline\nnext","Stored");
        escaped.storeMessagesAsJSON(file.toString());
        Message restored=new Message(); restored.setSender("Different Sender");
        restored.loadMessagesFromJSON(file.toString());
        check(restored.messages.get(0).equals(escaped.messages.get(0)),"JSON escape roundtrip");
        check(restored.report().contains("Original Sender"),"sender preserved after JSON reload");
        check(restored.displayStoredSendersAndRecipients().contains("Original Sender"),"stored sender display");
        Files.deleteIfExists(file);
        System.out.println("PASS: "+passed+" automated assertions");
    }
}
