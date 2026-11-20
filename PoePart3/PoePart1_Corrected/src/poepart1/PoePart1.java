package poepart1;
import java.util.Scanner;
/** QuickChat entry point. */
public class PoePart1 {
    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        Login login=new Login(); Message messages=new Message(scanner);
        System.out.println("===== Register =====");
        System.out.print("First name: ");String first=scanner.nextLine();
        System.out.print("Last name: ");String last=scanner.nextLine();
        login.setFullName(first,last);
        boolean registered=false;
        while(!registered) {
            System.out.print("Username (underscore, maximum 5 characters): ");String u=scanner.nextLine();
            System.out.print("Password: ");String p=scanner.nextLine();
            System.out.print("Cell number (+27...): ");String c=scanner.nextLine();
            System.out.println(login.registerUser(u,p,c));registered=login.registerStatus(u,p,c);
        }
        boolean authenticated=false;
        for(int attempts=0;attempts<3 && !authenticated;attempts++) {
            System.out.print("Login username: ");String u=scanner.nextLine();
            System.out.print("Login password: ");String p=scanner.nextLine();
            authenticated=login.loginUser(u,p);System.out.println(login.returnLoginStatus(authenticated));
        }
        if(!authenticated)return;
        messages.setSender(first+" "+last);
        messages.loadMessagesFromJSON("messages.json");
        System.out.println("Welcome to QuickChat.");
        while(true) {
            System.out.println("\n1) Send Messages\n2) Show recently sent messages\n3) Quit\n4) Stored Messages");
            switch(scanner.nextLine().trim()) {
                case "1":messages.sendingMessages();break;
                case "2":System.out.println("Coming Soon.");break;
                case "3":System.out.println("Goodbye!");return;
                case "4":messages.storedMessagesMenu();break;
                default:System.out.println("Invalid option.");
            }
        }
    }
}
