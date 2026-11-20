package poepart1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/** Manages aligned message arrays, JSON persistence and task reports. */
public class Message {
    public final ArrayList<String> messages = new ArrayList<>();
    public final ArrayList<String> messageIdsList = new ArrayList<>();
    public final ArrayList<String> msgHash = new ArrayList<>();
    public final ArrayList<String> recipientNumber = new ArrayList<>();
    public final ArrayList<String> messageStatus = new ArrayList<>();
    public final ArrayList<String> messageSenders = new ArrayList<>();
    public final ArrayList<String> sentMessages = new ArrayList<>();
    public final ArrayList<String> disregardedMessages = new ArrayList<>();
    public final ArrayList<String> storedMessages = new ArrayList<>();
    private final HashSet<String> usedIds = new HashSet<>();
    private final Random random = new Random();
    private final Scanner input;
    private int totalSent=0;
    private int nextNumber=0;
    private String sender="Developer";
    private Path jsonPath=Path.of("messages.json");
    public Message() { this(new Scanner(System.in)); }
    public Message(Scanner scanner) { input=scanner; }
    public void setSender(String value) { sender=value; }
    public void setJsonPath(Path path) { jsonPath=path; }
    public boolean checkMessageID(String id) { return id!=null && id.matches("[0-9]{10}"); }
    /** True when a valid ID is not already assigned to a message. */
    public boolean isAvailableMessageID(String id) { return checkMessageID(id) && !usedIds.contains(id); }
    public String generateMessageID() {
        String id;
        do { StringBuilder b=new StringBuilder(); for(int i=0;i<10;i++) b.append(random.nextInt(10)); id=b.toString(); }
        while(usedIds.contains(id));
        usedIds.add(id);
        return id;
    }
    public String createMessageIDReport(String id) { return "Message ID generated: " + id; }
    public String checkRecipientCell(String phone) {
        return phone!=null && phone.matches("\\+27[0-9]{9}")
            ? "Cell phone number successfully captured."
            : "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }
    public String checkMessageLength(String message) {
        if(message==null) message="";
        return message.length()<=250 ? "Message ready to send." : "Message exceeds 250 characters by " +(message.length()-250)+ "; please reduce the size.";
    }
    public String createMessageHash(String id,int number,String message) {
        if(!checkMessageID(id) || message==null || message.trim().isEmpty()) throw new IllegalArgumentException("Invalid message ID or text");
        String[] words=message.trim().split("\\s+");
        return (id.substring(0,2)+":"+number+":"+words[0]+words[words.length-1]).toUpperCase();
    }
    /** Adds a message, returning its generated hash. Status: Sent, Stored or Disregarded. */
    public String addMessage(String id,String recipient,String body,String status) {
        if(!checkMessageID(id) || !checkRecipientCell(recipient).equals("Cell phone number successfully captured.")
            || body==null || body.isBlank() || body.length()>250 || !List.of("Sent","Stored","Disregarded").contains(status))
            throw new IllegalArgumentException("Invalid message details");
        if(messageIdsList.contains(id)) throw new IllegalArgumentException("Duplicate message ID");
        usedIds.add(id);
        String hash=createMessageHash(id,nextNumber++,body);
        messageIdsList.add(id); recipientNumber.add(recipient); messages.add(body); msgHash.add(hash); messageStatus.add(status); messageSenders.add(sender);
        if(status.equals("Sent")) { sentMessages.add(body); totalSent++; }
        if(status.equals("Stored")) storedMessages.add(body);
        if(status.equals("Disregarded")) disregardedMessages.add(body);
        return hash;
    }
    public int returnTotalMessages() { return totalSent; }
    public String SentMessage() {
        System.out.println("1) Send Message  2) Disregard Message  3) Store Message to send later");
        while(true) {
            String choice=input.nextLine().trim();
            switch(choice) {
                case "1": System.out.println("Message successfully sent."); return "Sent";
                case "2": System.out.println("Press 0 to delete the message.");
                    if(input.nextLine().trim().equals("0")) return "Disregarded";
                    System.out.println("Please press 0."); break;
                case "3": System.out.println("Message successfully stored."); return "Stored";
                default: System.out.println("Enter 1, 2 or 3.");
            }
        }
    }
    public void sendingMessages() {
        System.out.print("How many messages do you want to enter? ");
        int count=readNonnegativeInt();
        for(int i=0;i<count;i++) {
            String recipient;
            do { System.out.print("Recipient (+27...): "); recipient=input.nextLine().trim();
                if(!checkRecipientCell(recipient).startsWith("Cell phone number successfully")) System.out.println(checkRecipientCell(recipient));
            } while(!checkRecipientCell(recipient).startsWith("Cell phone number successfully"));
            String body;
            do { System.out.print("Message (1-250 characters): "); body=input.nextLine();
                if(body.isBlank() || body.length()>250) System.out.println(checkMessageLength(body));
            } while(body.isBlank() || body.length()>250);
            String id=generateMessageID();
            String status=SentMessage();
            String hash=addMessage(id,recipient,body,status);
            System.out.println("Message ID: " + id + "\nMessage Hash: " + hash + "\nRecipient: " + recipient + "\nMessage: " + body);
            try { storeMessagesAsJSON(jsonPath.toString()); } catch (RuntimeException ex) { System.out.println(ex.getMessage()); }
        }
        System.out.println("Total messages sent: " + totalSent);
    }
    private int readNonnegativeInt() {
        while(true) { try { int n=Integer.parseInt(input.nextLine().trim()); if(n>=0)return n; } catch(NumberFormatException ignored) {}
            System.out.print("Enter a non-negative whole number: "); }
    }
    public String printMessages() { return report(); }
    /** A compact view of messages actually sent in this session or loaded from JSON. */
    public String printSentMessages() {
        StringBuilder result = new StringBuilder();
        for (int i=0;i<messages.size();i++) if ("Sent".equals(messageStatus.get(i)))
            result.append("Message ID: ").append(messageIdsList.get(i)).append("\nMessage Hash: ")
                  .append(msgHash.get(i)).append("\nRecipient: ").append(recipientNumber.get(i))
                  .append("\nMessage: ").append(messages.get(i)).append("\n\n");
        return result.length()==0 ? "No sent messages." : result.toString();
    }
    public String displayStoredSendersAndRecipients() {
        StringBuilder b=new StringBuilder();
        for(int i=0;i<messages.size();i++) if(messageStatus.get(i).equals("Stored"))
            b.append("Sender: ").append(messageSenders.get(i)).append(" | Recipient: ").append(recipientNumber.get(i)).append('\n');
        return b.length()==0?"No stored messages.":b.toString();
    }
    public String longestStoredMessage() {
        String longest="";
        for(int i=0;i<messages.size();i++) if(messageStatus.get(i).equals("Stored") && messages.get(i).length()>longest.length()) longest=messages.get(i);
        return longest.isEmpty()?"No stored messages.":longest;
    }
    public String searchMessageId(String id) {
        int i=messageIdsList.indexOf(id);
        return i<0?"Message not found.":"Recipient: "+recipientNumber.get(i)+"\nMessage: "+messages.get(i);
    }
    public String searchRecipient(String recipient) {
        StringBuilder b=new StringBuilder();
        for(int i=0;i<messages.size();i++) if(recipientNumber.get(i).equals(recipient) && !messageStatus.get(i).equals("Disregarded"))
            b.append(messages.get(i)).append('\n');
        return b.length()==0?"No messages found.":b.toString();
    }
    public String deleteByHash(String hash) {
        int i=msgHash.indexOf(hash);
        if(i<0)return "Message hash not found.";
        String body=messages.get(i), status=messageStatus.get(i);
        if(status.equals("Sent")) {sentMessages.remove(body); totalSent--;}
        if(status.equals("Stored")) storedMessages.remove(body);
        if(status.equals("Disregarded")) disregardedMessages.remove(body);
        messages.remove(i); recipientNumber.remove(i); messageIdsList.remove(i); msgHash.remove(i); messageStatus.remove(i); messageSenders.remove(i);
        storeMessagesAsJSON(jsonPath.toString());
        return "Message: \""+body+"\" successfully deleted.";
    }
    public String report() {
        StringBuilder b=new StringBuilder("===== MESSAGE REPORT =====\n");
        for(int i=0;i<messages.size();i++) {
            b.append("Message ID: ").append(messageIdsList.get(i)).append("\nMessage Hash: ").append(msgHash.get(i))
             .append("\nSender: ").append(messageSenders.get(i)).append("\nRecipient: ").append(recipientNumber.get(i))
             .append("\nMessage: ").append(messages.get(i)).append("\nStatus: ").append(messageStatus.get(i)).append("\n\n");
        }
        return b.toString();
    }
    private static String escape(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
    }
    public void storeMessagesAsJSON(String file) {
        StringBuilder b=new StringBuilder("[\n");
        for(int i=0;i<messages.size();i++) {
            if(i>0)b.append(",\n");
            b.append("  {\"messageId\":\"").append(escape(messageIdsList.get(i)))
             .append("\",\"recipient\":\"").append(escape(recipientNumber.get(i)))
             .append("\",\"message\":\"").append(escape(messages.get(i)))
             .append("\",\"messageHash\":\"").append(escape(msgHash.get(i)))
             .append("\",\"status\":\"").append(escape(messageStatus.get(i)))
             .append("\",\"sender\":\"").append(escape(messageSenders.get(i))).append("\"}");
        }
        b.append("\n]\n");
        try { Files.writeString(Path.of(file),b.toString(),StandardCharsets.UTF_8); }
        catch(IOException e) { throw new IllegalStateException("Could not save JSON: "+e.getMessage(),e); }
    }
    /** Loads a JSON array using a character-level parser (including escaped characters). */
    public void loadMessagesFromJSON(String file) {
        Path path=Path.of(file);
        if(!Files.exists(path)) return;
        try {
            String json=Files.readString(path,StandardCharsets.UTF_8);
            java.util.List<java.util.Map<String,String>> records = JsonMessageParser.parse(json);
            java.util.Set<String> incomingIds = new java.util.HashSet<>();
            for (java.util.Map<String,String> record : records) {
                String id = record.get("messageId");
                String recipient = record.get("recipient");
                String body = record.get("message");
                String status = record.get("status");
                if (!checkMessageID(id) || !incomingIds.add(id) ||
                    !"Cell phone number successfully captured.".equals(checkRecipientCell(recipient)) ||
                    body == null || body.isBlank() || body.length()>250 ||
                    !List.of("Sent", "Stored", "Disregarded").contains(status))
                    throw new IllegalArgumentException("Invalid message record in JSON");
            }
            for(java.util.Map<String,String> data : records) {
                String id=data.get("messageId");
                if(id==null || messageIdsList.contains(id)) continue;
                String recipient=data.get("recipient"), body=data.get("message"), status=data.get("status");
                if(!checkMessageID(id) || recipient==null || body==null || status==null) {
                    throw new IllegalArgumentException("Invalid message record in JSON");
                }
                String savedSender=data.getOrDefault("sender", sender);
                String savedHash=data.get("messageHash");
                String oldSender=sender;
                try {
                    sender=savedSender;
                    String computed=addMessage(id,recipient,body,status);
                    if(savedHash!=null && !savedHash.isBlank()) {
                        msgHash.set(msgHash.size()-1,savedHash);
                        String[] components = savedHash.split(":", 3);
                        if (components.length == 3) {
                            try { nextNumber = Math.max(nextNumber, Integer.parseInt(components[1])+1); }
                            catch (NumberFormatException ignored) { /* Retain sequential numbering */ }
                        }
                    }
                } finally { sender=oldSender; }
            }
        } catch(IOException e) {throw new IllegalStateException("Could not read JSON: "+e.getMessage(),e);}
    }
    public void storedMessagesMenu() {
        while(true) {
            System.out.println("\n===== STORED MESSAGES =====\n1) Sender and recipient\n2) Longest stored message\n3) Search message ID\n4) Search recipient\n5) Delete by hash\n6) Full report\n7) Load JSON\n8) Back");
            String choice=input.nextLine().trim();
            switch(choice) {
                case "1":System.out.println(displayStoredSendersAndRecipients());break;
                case "2":System.out.println(longestStoredMessage());break;
                case "3":System.out.print("Message ID: ");System.out.println(searchMessageId(input.nextLine().trim()));break;
                case "4":System.out.print("Recipient: ");System.out.println(searchRecipient(input.nextLine().trim()));break;
                case "5":System.out.print("Message hash: ");System.out.println(deleteByHash(input.nextLine().trim()));break;
                case "6":System.out.println(report());break;
                case "7":loadMessagesFromJSON(jsonPath.toString());System.out.println(report());break;
                case "8":return;
                default:System.out.println("Invalid option.");
            }
        }
    }
}
