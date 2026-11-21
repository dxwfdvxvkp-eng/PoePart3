/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poepart1;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles creating, validating, hashing, searching, deleting and
 * storing chat messages.
 *
 * @author User
 */
public class Message {

    // ----- master arrays: every message ever captured, any flag -----
    ArrayList<String> messages = new ArrayList<>();
    ArrayList<String> messageIdsList = new ArrayList<>();
    HashSet<String> messageIds = new HashSet<>();
    ArrayList<String> msgHash = new ArrayList<>();
    ArrayList<String> recipientNumber = new ArrayList<>();
    ArrayList<String> messageStatus = new ArrayList<>();

    // ----- Part 3: arrays split out by flag (populated through
    // captureMessage(), never hard-coded) -----
    ArrayList<String> sentMessages = new ArrayList<>();
    ArrayList<String> disregardedMessages = new ArrayList<>();

    /**
     * One captured "Stored" message, kept as its own small record so the
     * Stored Messages menu (search/delete/report) has something richer
     * than a bare String to work with.
     */
    public static class StoredMessageRecord {

        public String messageId;
        public String recipient;
        public String message;
        public String messageHash;
        public String sender;

        public StoredMessageRecord(String messageId, String recipient, String message,
                String messageHash, String sender) {
            this.messageId = messageId;
            this.recipient = recipient;
            this.message = message;
            this.messageHash = messageHash;
            this.sender = sender;
        }
    }

    ArrayList<StoredMessageRecord> storedMessages = new ArrayList<>();

    // The logged-in user's username, shown as "sender" in the Stored
    // Messages reports. Set via setSender() after a successful login.
    private String senderUsername = "";

    Random random = new Random();
    Scanner input;

    // "Num messages sent" - starts at 0 and is only incremented once a
    // message is actually sent (not stored/disregarded). Also used as
    // the message-number component of the Message Hash - see
    // createMessageHash().
    int numMessageSent = 0;

    /**
     * Default constructor - creates its own Scanner. Kept so any
     * existing code/tests that do `new Message()` still compiles.
     */
    public Message() {
        this.input = new Scanner(System.in);
    }

    /**
     * Accepts a Scanner from the caller instead of always opening a
     * second Scanner on System.in. Two separate Scanner objects reading
     * the same InputStream can each buffer ahead and "steal" input meant
     * for the other, causing confusing skipped prompts. PoePart1 passes
     * its single Scanner in here so there is only ever one Scanner
     * reading the console.
     *
     * @param sharedScanner the single Scanner the whole application reads
     * console input from
     */
    public Message(Scanner sharedScanner) {
        this.input = sharedScanner;
    }

    /**
     * Records which logged-in user is sending messages this session, so
     * the Stored Messages report can show a sender alongside the
     * recipient.
     *
     * @param username the username of the logged-in user
     */
    public void setSender(String username) {
        this.senderUsername = (username == null) ? "" : username;
    }

    // =====================================================================
    // Part 2 - interactive message capture
    // =====================================================================

    public void sendingMessages() {

        System.out.print("How many messages do you want to send? ");
        int numberOfMessages = readInt();

        input.nextLine();

        for (int i = 1; i <= numberOfMessages; i++) {

            System.out.println("\n==============================");
            System.out.println("MESSAGE " + i);
            System.out.println("==============================");

            String messageID = generateMessageID();
            System.out.println(createMessageIDReport(messageID));

            System.out.print("Enter recipient cell number: ");
            String recipient = input.nextLine();

            String recipientResult = checkRecipientCell(recipient);

            if (!recipientResult.equals(CELL_SUCCESS)) {
                System.out.println(recipientResult);
                i--;
                continue;
            }
            System.out.println(recipientResult);

            System.out.print("Enter your message: ");
            String messageText = input.nextLine();

            String lengthResult = checkMessageLength(messageText);

            if (!lengthResult.equals(MESSAGE_READY)) {
                System.out.println(lengthResult);
                i--;
                continue;
            }
            System.out.println(lengthResult);

            // Ask the user what to do with this message BEFORE it is
            // filed into any array - this way a message the user
            // actually deletes (Disregard -> press 0) never has to be
            // added and then un-added again.
            String disposition = SentMessage();

            if (disposition.equals("Deleted")) {
                captureMessage(messageID, recipient, messageText, "Disregarded");
                continue;
            }

            captureMessage(messageID, recipient, messageText, disposition);
        }

        printMessages();

        System.out.println("Total messages sent: " + returnTotalMessages());

        storeMessagesAsJSON("messages.json");
        saveStoredMessagesToJSON("storedMessages.json");
    }

    public int returnTotalMessages() {
        return numMessageSent;
    }

    /**
     * Builds the Message Hash: the first two characters of the Message
     * ID, a colon, the message number, a colon, then the first and last
     * word of the message, all in upper case - e.g. "00:0:HITONIGHT".
     */
    public String createMessageHash(String messageID, int messageNumber, String messageText) {

        String firstTwoNumbers = messageID.substring(0, 2);

        String cleanMessage = messageText.trim();

        String[] words = cleanMessage.split("\\s+");

        String firstWord = words[0];

        String lastWord = words[words.length - 1];

        String hash = firstTwoNumbers + ":" + messageNumber + ":" + firstWord + lastWord;

        return hash.toUpperCase();
    }

    /**
     * Generates a unique 10-digit Message ID using string manipulation
     * (substring): a longer random string of digits is built and then
     * cut down to exactly 10 characters.
     */
    public String generateMessageID() {

        String messageID;

        do {
            StringBuilder randomDigits = new StringBuilder();
            for (int d = 0; d < 15; d++) {
                randomDigits.append(random.nextInt(10));
            }
            messageID = randomDigits.substring(0, 10);

        } while (messageIds.contains(messageID) || !checkMessageID(messageID));

        return messageID;
    }

    /**
     * Ensures the Message ID is no more than ten characters long.
     */
    public boolean checkMessageID(String messageID) {
        return messageID != null && messageID.length() <= 10;
    }

    /**
     * Wraps generateMessageID()'s result in the exact confirmation text
     * the brief asks for: "Message ID generated: <Message ID>".
     */
    public String createMessageIDReport(String messageID) {
        return "Message ID generated: " + messageID;
    }

    private static final String CELL_SUCCESS = "Cell phone number successfully captured.";
    private static final String CELL_FAILURE = "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";

    /**
     * Validates the recipient's cellphone number. Must be in the South
     * African international format: "+27" followed by 9 digits.
     */
    public String checkRecipientCell(String cellphone) {
        if (cellphone != null && cellphone.matches("^\\+27[0-9]{9}$")) {
            return CELL_SUCCESS;
        } else {
            return CELL_FAILURE;
        }
    }

    private static final int MAX_MESSAGE_LENGTH = 250;
    private static final String MESSAGE_READY = "Message ready to send.";

    /**
     * Validates that the message is not more than 250 characters long.
     */
    public String checkMessageLength(String message) {
        if (message == null) {
            message = "";
        }
        if (message.length() <= MAX_MESSAGE_LENGTH) {
            return MESSAGE_READY;
        } else {
            int excess = message.length() - MAX_MESSAGE_LENGTH;
            return "Message exceeds 250 characters by " + excess + "; please reduce the size.";
        }
    }

    /**
     * Lets the user choose what to do with the message that was just
     * captured. Returns one of "Sent", "Stored", "Disregarded" (kept,
     * but flagged as disregarded) or "Deleted" (the user pressed 0 to
     * confirm deletion, so the message is not captured anywhere at all).
     *
     * NOTE: kept as "SentMessage()" (capital S) rather than the usual
     * camelCase "sendMessage()" because that is the exact method name
     * given in the assignment's method table.
     */
    public String SentMessage() {

        System.out.println("\nChoose what you want to do with this message!");

        System.out.println("1. Send Message");
        System.out.println("2. Disregard Message");
        System.out.println("3. Store Message to send later");

        System.out.println("Enter your choice: ");
        int choice = readInt();

        input.nextLine();

        switch (choice) {

            case 1 -> {
                System.out.println("Message successfully sent.");
                return "Sent";
            }

            case 2 -> {
                System.out.println("Press 0 to delete the message.");
                int deleteChoice = readInt();

                input.nextLine();

                if (deleteChoice == 0) {
                    System.out.println("Message deleted");
                    return "Deleted";
                } else {
                    System.out.println("Invalid option!");
                    return "Disregarded";
                }
            }

            case 3 -> {
                System.out.println("Message successfully stored.");
                return "Stored";
            }

            default -> {
                System.out.println("Invalid option");
                return "Disregarded";
            }
        }
    }

    // =====================================================================
    // Part 3 - populating the arrays (no hard-coding)
    // =====================================================================

    /**
     * Files a message into every array it belongs in, based on its
     * flag. This is the single place messages are ever added to the
     * master and per-flag arrays - sendingMessages() calls it once the
     * user's disposition choice is known, and unit tests call it
     * directly to build up known test data without hard-coding array
     * contents.
     *
     * @param id the Message ID to use; if null/blank a fresh one is
     * generated
     * @param recipient the recipient's cellphone number (may be null for
     * test data that only cares about ID-based lookup)
     * @param messageText the message body
     * @param flag one of "Sent", "Stored", "Disregarded"/"Disregard"
     * @return the Message ID that was used/generated for this message
     */
    public String captureMessage(String id, String recipient, String messageText, String flag) {

        String messageId = (id == null || id.isBlank()) ? generateMessageID() : id;

        if (!checkMessageID(messageId) || messageIds.contains(messageId)) {
            throw new IllegalArgumentException("Message ID must be no more than 10 characters long.");
        }

        String safeRecipient = (recipient == null) ? "" : recipient;

        // the message-number component of the hash is the count of
        // messages already actually SENT before this one (see
        // createMessageHash's Javadoc / the brief's worked example).
        String hash = createMessageHash(messageId, numMessageSent, messageText);

        messageIds.add(messageId);
        messageIdsList.add(messageId);
        recipientNumber.add(safeRecipient);
        messages.add(messageText);
        msgHash.add(hash);

        String normalizedFlag = normalizeFlag(flag);
        messageStatus.add(normalizedFlag);

        switch (normalizedFlag) {
            case "Sent" -> {
                sentMessages.add(messageText);
                numMessageSent++;
            }
            case "Stored" ->
                storedMessages.add(new StoredMessageRecord(messageId, safeRecipient, messageText, hash, senderUsername));
            default ->
                disregardedMessages.add(messageText);
        }

        return messageId;
    }

    private String normalizeFlag(String flag) {
        if (flag == null) {
            return "Disregarded";
        }
        return switch (flag.trim().toLowerCase()) {
            case "sent" -> "Sent";
            case "stored" -> "Stored";
            default -> "Disregarded";
        };
    }

    public ArrayList<String> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    public ArrayList<String> getDisregardedMessages() {
        return new ArrayList<>(disregardedMessages);
    }

    public ArrayList<StoredMessageRecord> getStoredMessages() {
        return new ArrayList<>(storedMessages);
    }

    /**
     * Returns the longest message captured so far, across every flag
     * (Sent, Stored and Disregarded alike).
     */
    public String getLongestMessage() {
        String longest = "";
        for (String m : messages) {
            if (m.length() > longest.length()) {
                longest = m;
            }
        }
        return longest;
    }

    /**
     * Searches every captured message (any flag) for the given Message
     * ID and returns its text, or a clear "not found" message.
     */
    public String searchByMessageID(String id) {
        int index = messageIdsList.indexOf(id);
        if (index == -1) {
            return "No message found with ID " + id + ".";
        }
        return messages.get(index);
    }

    /**
     * Returns every message (Sent or Stored - Disregarded messages are
     * excluded) addressed to the given recipient, in the order they
     * were captured.
     */
    public ArrayList<String> searchMessagesByRecipient(String recipient) {
        ArrayList<String> results = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            String status = messageStatus.get(i);
            if (recipientNumber.get(i).equals(recipient)
                    && (status.equals("Sent") || status.equals("Stored"))) {
                results.add(messages.get(i));
            }
        }
        return results;
    }

    public String printMessages() {

        StringBuilder allMessages = new StringBuilder();

        System.out.println("\n===== All Messages =====");

        for (int i = 0; i < messages.size(); i++) {
            int messageNumber = i + 1;

            allMessages.append("\nMessage Number: ").append(messageNumber);
            allMessages.append("\nMessage ID: ").append(messageIdsList.get(i));
            allMessages.append("\nMessage Hash: ").append(msgHash.get(i));
            allMessages.append("\nRecipient: ").append(recipientNumber.get(i));
            allMessages.append("\nMessage: ").append(messages.get(i));
            allMessages.append("\nStatus: ").append(messageStatus.get(i));

            System.out.println("\n==============================");

            System.out.println("\nMessage Number: " + messageNumber);
            System.out.println("Message ID: " + messageIdsList.get(i));
            System.out.println("Message Hash: " + msgHash.get(i));
            System.out.println("Recipient: " + recipientNumber.get(i));
            System.out.println("Message: " + messages.get(i));
            System.out.println("Status: " + messageStatus.get(i));
        }

        return allMessages.toString();
    }

    // =====================================================================
    // Part 3 - Stored Messages menu (4th main menu option)
    // =====================================================================

    /**
     * Interactive "Stored Messages" menu. Reloads storedMessages.json
     * first (so messages stored in a previous run are available too),
     * then lets the user browse/search/delete/report on the stored
     * messages.
     */
    public void storedMessagesMenu() {

        loadStoredMessagesFromJSON("storedMessages.json");

        String option;
        do {
            System.out.println("\n===== Stored Messages =====");
            System.out.println("a) Display sender and recipient of all stored messages");
            System.out.println("b) Display the longest stored message");
            System.out.println("c) Search for a message by Message ID");
            System.out.println("d) Search for messages stored for a particular recipient");
            System.out.println("e) Delete a message using its Message Hash");
            System.out.println("f) Display a full report of all stored messages");
            System.out.println("0) Back to main menu");

            option = input.nextLine().trim().toLowerCase();

            switch (option) {
                case "a", "1" ->
                    System.out.println(displayStoredSendersAndRecipients());
                case "b", "2" ->
                    System.out.println("Longest stored message: " + getLongestStoredMessage());
                case "c", "3" -> {
                    System.out.print("Enter Message ID to search for: ");
                    String id = input.nextLine();
                    System.out.println(searchStoredByMessageID(id));
                }
                case "d", "4" -> {
                    System.out.print("Enter recipient cell number to search for: ");
                    String recipient = input.nextLine();
                    System.out.println(searchStoredByRecipient(recipient));
                }
                case "e", "5" -> {
                    System.out.print("Enter Message Hash of the message to delete: ");
                    String hash = input.nextLine();
                    System.out.println(deleteStoredMessageByHash(hash));
                    saveStoredMessagesToJSON("storedMessages.json");
                }
                case "f", "6" ->
                    System.out.println(displayStoredMessagesReport());
                case "0" -> {
                    // back to main menu
                }
                default ->
                    System.out.println("Invalid option!!");
            }

        } while (!option.equals("0"));
    }

    /**
     * Displays the sender and recipient of every stored message.
     */
    public String displayStoredSendersAndRecipients() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (StoredMessageRecord rec : storedMessages) {
            sb.append("\nSender: ").append(rec.sender)
                    .append(" | Recipient: ").append(rec.recipient);
        }
        return sb.toString();
    }

    /**
     * Returns the longest message among the stored messages only.
     */
    public String getLongestStoredMessage() {
        String longest = "";
        for (StoredMessageRecord rec : storedMessages) {
            if (rec.message.length() > longest.length()) {
                longest = rec.message;
            }
        }
        return longest;
    }

    /**
     * Searches the stored messages for the given Message ID and reports
     * its recipient and message text.
     */
    public String searchStoredByMessageID(String id) {
        for (StoredMessageRecord rec : storedMessages) {
            if (rec.messageId.equals(id)) {
                return "Recipient: " + rec.recipient + " | Message: " + rec.message;
            }
        }
        return "No stored message found with ID " + id + ".";
    }

    /**
     * Returns every stored message addressed to the given recipient.
     */
    public ArrayList<String> searchStoredByRecipient(String recipient) {
        ArrayList<String> results = new ArrayList<>();
        for (StoredMessageRecord rec : storedMessages) {
            if (rec.recipient.equals(recipient)) {
                results.add(rec.message);
            }
        }
        return results;
    }

    /**
     * Deletes the stored message with the given Message Hash.
     *
     * @return a confirmation naming the deleted message, or a
     * not-found message if no stored message has that hash
     */
    public String deleteStoredMessageByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            StoredMessageRecord rec = storedMessages.get(i);
            if (rec.messageHash.equals(hash)) {
                storedMessages.remove(i);
                for (int j = messageIdsList.size() - 1; j >= 0; j--) {
                    if (messageIdsList.get(j).equals(rec.messageId)) {
                        messageIdsList.remove(j);
                        recipientNumber.remove(j);
                        messages.remove(j);
                        msgHash.remove(j);
                        messageStatus.remove(j);
                    }
                }
                messageIds.remove(rec.messageId);
                return "Message: \"" + rec.message + "\" successfully deleted.";
            }
        }
        return "No stored message found with hash " + hash + ".";
    }

    /**
     * Full report of every stored message: Message ID, Message Hash,
     * Recipient and Message.
     */
    public String displayStoredMessagesReport() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder sb = new StringBuilder("\n===== Stored Messages Report =====");
        for (StoredMessageRecord rec : storedMessages) {
            sb.append("\nSender: ").append(rec.sender);
            sb.append("\nMessage ID: ").append(rec.messageId);
            sb.append("\nMessage Hash: ").append(rec.messageHash);
            sb.append("\nRecipient: ").append(rec.recipient);
            sb.append("\nMessage: ").append(rec.message);
            sb.append("\n------------------------------");
        }
        return sb.toString();
    }

    // =====================================================================
    // JSON storage
    // =====================================================================

    /**
     * Stores every message captured this session (any flag) to a JSON
     * file. Written by hand (no external JSON library needed) so the
     * project compiles out of the box.
     *
     * @param filePath the path of the JSON file to write the messages to
     */
    public void storeMessagesAsJSON(String filePath) {

        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < messages.size(); i++) {
            json.append("  {\n");
            json.append("    \"messageNumber\": ").append(i + 1).append(",\n");
            json.append("    \"messageId\": \"").append(escapeJson(messageIdsList.get(i))).append("\",\n");
            json.append("    \"recipient\": \"").append(escapeJson(recipientNumber.get(i))).append("\",\n");
            json.append("    \"message\": \"").append(escapeJson(messages.get(i))).append("\",\n");
            json.append("    \"messageHash\": \"").append(escapeJson(msgHash.get(i))).append("\",\n");
            json.append("    \"status\": \"").append(escapeJson(messageStatus.get(i))).append("\"\n");
            json.append("  }");
            if (i < messages.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]\n");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(json.toString());
            System.out.println("Messages stored to " + filePath);
        } catch (IOException e) {
            System.out.println("Could not store messages to JSON file: " + e.getMessage());
        }
    }

    /**
     * Writes the current storedMessages array out to a JSON file.
     *
     * @param filePath the path of the JSON file to write to
     */
    public void saveStoredMessagesToJSON(String filePath) {

        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < storedMessages.size(); i++) {
            StoredMessageRecord rec = storedMessages.get(i);
            json.append("  {\n");
            json.append("    \"messageId\": \"").append(escapeJson(rec.messageId)).append("\",\n");
            json.append("    \"recipient\": \"").append(escapeJson(rec.recipient)).append("\",\n");
            json.append("    \"message\": \"").append(escapeJson(rec.message)).append("\",\n");
            json.append("    \"messageHash\": \"").append(escapeJson(rec.messageHash)).append("\",\n");
            json.append("    \"sender\": \"").append(escapeJson(rec.sender)).append("\"\n");
            json.append("  }");
            if (i < storedMessages.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]\n");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(json.toString());
        } catch (IOException e) {
            System.out.println("Could not store the stored-messages JSON file: " + e.getMessage());
        }
    }

    // matches  "key": "value"  pairs inside one JSON object block
    private static final Pattern JSON_FIELD = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    /**
     * Reads a JSON file previously written by saveStoredMessagesToJSON()
     * back into the storedMessages array, so stored messages survive
     * between runs of the application. storedMessages is replaced with
     * whatever is in the file (if the file does not exist yet, nothing
     * happens and whatever is already in memory is left untouched).
     *
     * Implemented with simple pattern matching rather than a JSON
     * library, since the file's structure is entirely our own and known
     * in advance - this keeps the project dependency-free and
     * guaranteed to compile. (Libraries such as Gson or org.json would
     * do the same job if your course wants one used instead.)
     *
     * @param filePath the path of the JSON file to read
     */
    public void loadStoredMessagesFromJSON(String filePath) {

        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            return;
        }

        String content;
        try {
            content = Files.readString(path);
        } catch (IOException e) {
            System.out.println("Could not read " + filePath + ": " + e.getMessage());
            return;
        }

        ArrayList<StoredMessageRecord> loaded = new ArrayList<>();

        // Each stored message is one { ... } block in the JSON array.
        int start = content.indexOf('{');
        while (start != -1) {
            int end = content.indexOf('}', start);
            if (end == -1) {
                break;
            }
            String block = content.substring(start, end);

            String id = "", recipient = "", message = "", hash = "", sender = "";
            Matcher matcher = JSON_FIELD.matcher(block);
            while (matcher.find()) {
                String key = matcher.group(1);
                String value = unescapeJson(matcher.group(2));
                switch (key) {
                    case "messageId" -> id = value;
                    case "recipient" -> recipient = value;
                    case "message" -> message = value;
                    case "messageHash" -> hash = value;
                    case "sender" -> sender = value;
                    default -> {
                        // ignore unknown fields
                    }
                }
            }

            if (!id.isBlank()) {
                loaded.add(new StoredMessageRecord(id, recipient, message, hash, sender));
            }

            start = content.indexOf('{', end);
        }

        storedMessages = loaded;

        System.out.println("Loaded " + storedMessages.size() + " stored message(s) from " + filePath + ".");
        System.out.println(displayStoredMessagesReport());
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unescapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    /**
     * Reads an integer from the console, re-prompting instead of
     * crashing with an InputMismatchException when the user types
     * something that isn't a number.
     */
    private int readInt() {
        while (!input.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            input.next();
        }
        return input.nextInt();
    }
}
