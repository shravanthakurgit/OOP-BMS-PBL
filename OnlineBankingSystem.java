import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class OnlineBankingSystem {
    // Fixed: Input Issue (Two scanners conflicting)
    // Use the unified scanner from Bank.java
    private static final Bank bank = new Bank();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n===== ONLINE BANKING SYSTEM =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    registerFlow();
                    break;
                case 2:
                    loginFlow();
                    break;
                case 3:
                    System.out.println("Thank you for using Online Banking!");
                    return;
                default:
                    System.out.println("Invalid Choice!");
                    break;
            }
        }
    }

    private static void registerFlow() {
        System.out.print("Name: ");
        String name = Bank.sc.nextLine().replace(",", "");
        System.out.print("Email: ");
        String email = Bank.sc.nextLine().replace(",", "");
        System.out.print("Password: ");
        String pass = Bank.sc.nextLine().replace(",", "");
        bank.register(name, email, pass);
    }

    private static void loginFlow() {
        System.out.print("Email: ");
        String email = Bank.sc.nextLine().replace(",", "");
        System.out.print("Password: ");
        String pass = Bank.sc.nextLine().replace(",", "");
        String loggedInEmail = bank.login(email, pass);
        if (loggedInEmail != null) {
            userDashboard(loggedInEmail);
        } else {
            System.out.println("Invalid Login Credentials!");
        }
    }

    private static void userDashboard(String email) {
        while (true) {
            System.out.println("\n--- USER DASHBOARD (" + email + ") ---");
            System.out.println("1. Open New Bank Account");
            System.out.println("2. Access My Accounts");
            System.out.println("3. Logout");
            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    System.out.print("Name on Account: ");
                    String name = Bank.sc.nextLine().replace(",", "");
                    BigDecimal balance = getBigDecimalInput("Initial Balance: ");
                    System.out.print("Set 4-digit PIN: ");
                    String pin = Bank.sc.nextLine().replace(",", "");
                    bank.createAccount(email, name, balance, pin);
                    break;
                case 2:
                    accessAccountsFlow(email);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid Choice!");
                    break;
            }
        }
    }

    private static void accessAccountsFlow(String email) {
        List<Account> accounts = bank.loadUserAccounts(email);
        if (accounts.isEmpty()) {
            System.out.println("No accounts found!");
            return;
        }

        System.out.println("\n--- Your Accounts ---");
        for (int i = 0; i < accounts.size(); i++) {
            Account a = accounts.get(i);
            System.out.println((i + 1) + ". No: " + a.getAccountNo() + " | Name: " + a.getName() + " | Bal: ₹" + a.getBalance());
        }

        int idx = getIntInput("Select account (1-" + accounts.size() + "): ") - 1;
        if (idx >= 0 && idx < accounts.size()) {
            long accNo = accounts.get(idx).getAccountNo();
            if (bank.verifyPin(accNo)) {
                accountMenu(accNo);
            }
        } else {
            System.out.println("Invalid Selection!");
        }
    }

    private static void accountMenu(long accNo) {
        while (true) {
            System.out.println("\n--- ACCOUNT MENU (" + accNo + ") ---");
            System.out.println("1. Debit");
            System.out.println("2. Credit");
            System.out.println("3. Transfer");
            System.out.println("4. Balance & History");
            System.out.println("5. Back to Dashboard");
            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    BigDecimal dAmt = getBigDecimalInput("Amount to Debit: ");
                    bank.debit(accNo, dAmt);
                    break;
                case 2:
                    BigDecimal cAmt = getBigDecimalInput("Amount to Credit: ");
                    bank.credit(accNo, cAmt);
                    break;
                case 3:
                    long receiver = getLongInput("Receiver Account Number: ");
                    BigDecimal tAmt = getBigDecimalInput("Amount to Transfer: ");
                    bank.transfer(accNo, receiver, tAmt);
                    break;
                case 4:
                    bank.showHistory(accNo);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid Choice!");
                    break;
            }
        }
    }

    // --- Input Helpers ---
    private static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(Bank.sc.nextLine().trim()); }
            catch (Exception e) { System.out.println("Invalid number!"); }
        }
    }

    private static long getLongInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Long.parseLong(Bank.sc.nextLine().trim()); }
            catch (Exception e) { System.out.println("Invalid number!"); }
        }
    }

    private static BigDecimal getBigDecimalInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return new BigDecimal(Bank.sc.nextLine().trim()).setScale(2, RoundingMode.HALF_UP); }
            catch (Exception e) { System.out.println("Invalid amount!"); }
        }
    }
}
