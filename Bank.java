import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Bank {
    private static final String USERS_FILE = "users.txt";
    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String TRANSACTIONS_FILE = "transactions.txt";
    public static final Scanner sc = new Scanner(System.in);

    // Static block to ensure files exist
    static {
        try {
            new File(USERS_FILE).createNewFile();
            new File(ACCOUNTS_FILE).createNewFile();
            new File(TRANSACTIONS_FILE).createNewFile();
        } catch (IOException e) {
            System.out.println("Initialization Error: Could not create data files.");
        }
    }

    // --- User Related ---
    public boolean userExists(String email) {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",");
                if (data.length > 1 && data[1].equals(email)) return true;
            }
        } catch (IOException e) { /* ignore */ }
        return false;
    }

    public void register(String name, String email, String pass) {
        if (!email.contains("@") || !email.contains(".")) {
            System.out.println("Invalid email format!");
            return;
        }
        if (userExists(email)) {
            System.out.println("User already exists!");
            return;
        }
        try (FileWriter fw = new FileWriter(USERS_FILE, true)) {
            fw.write(name + "," + email + "," + pass + "\n");
            System.out.println("Registration Successful!");
        } catch (IOException e) {
            System.out.println("File Error: Could not save user data.");
        }
    }

    public String login(String email, String pass) {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",");
                if (data.length > 2 && data[1].equals(email) && data[2].equals(pass)) {
                    return email;
                }
            }
        } catch (IOException e) { /* ignore */ }
        return null;
    }

    // --- Account Operations ---
    public void createAccount(String email, String name, BigDecimal balance, String pin) {
        if (pin.length() != 4) {
            System.out.println("PIN must be 4 digits!");
            return;
        }

        long accNo = System.currentTimeMillis() + new Random().nextInt(1000);
        Account account = new Account(accNo, name, email, balance, pin);
        
        try (FileWriter fw = new FileWriter(ACCOUNTS_FILE, true)) {
            fw.write(account.toString() + "\n");
            logTransaction(accNo, "Account Created with Balance: " + balance);
            System.out.println("Account Created! Number: " + accNo);
        } catch (IOException e) {
            System.out.println("File Error: Could not save account data.");
        }
    }

    public List<Account> loadUserAccounts(String email) {
        List<Account> userAccounts = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] d = line.split(",");
                if (d.length > 4 && d[2].equals(email)) {
                    userAccounts.add(new Account(Long.parseLong(d[0]), d[1], d[2], new BigDecimal(d[3]), d[4]));
                }
            }
        } catch (IOException e) { /* ignore */ }
        return userAccounts;
    }

    public void debit(long accNo, BigDecimal amt) {
        List<Account> all = loadAllAccounts();
        boolean found = false;
        for (Account a : all) {
            if (a.getAccountNo() == accNo) {
                if (amt.compareTo(a.getBalance()) > 0) {
                    System.out.println("Insufficient Balance!");
                    return;
                }
                a.setBalance(a.getBalance().subtract(amt).setScale(2, RoundingMode.HALF_UP));
                found = true;
                System.out.println("Money Debited! New Balance: ₹" + a.getBalance());
                break;
            }
        }
        if (found) {
            rewriteAccounts(all);
            logTransaction(accNo, "Debited " + amt);
        }
    }

    public void credit(long accNo, BigDecimal amt) {
        List<Account> all = loadAllAccounts();
        boolean found = false;
        for (Account a : all) {
            if (a.getAccountNo() == accNo) {
                a.setBalance(a.getBalance().add(amt).setScale(2, RoundingMode.HALF_UP));
                found = true;
                System.out.println("Money Credited! New Balance: ₹" + a.getBalance());
                break;
            }
        }
        if (found) {
            rewriteAccounts(all);
            logTransaction(accNo, "Credited " + amt);
        }
    }

    public void transfer(long sender, long receiver, BigDecimal amt) {
        List<Account> all = loadAllAccounts();
        Account sAcc = null, rAcc = null;
        for (Account a : all) {
            if (a.getAccountNo() == sender) sAcc = a;
            if (a.getAccountNo() == receiver) rAcc = a;
        }

        // Fix: CRITICAL BUG (sAcc could be null)
        if (sAcc == null) {
            System.out.println("Sender account not found!");
            return;
        }
        if (rAcc == null) {
            System.out.println("Receiver account not found!");
            return;
        }
        if (amt.compareTo(sAcc.getBalance()) > 0) {
            System.out.println("Insufficient Balance!");
            return;
        }

        sAcc.setBalance(sAcc.getBalance().subtract(amt).setScale(2, RoundingMode.HALF_UP));
        rAcc.setBalance(rAcc.getBalance().add(amt).setScale(2, RoundingMode.HALF_UP));

        rewriteAccounts(all);
        logTransaction(sender, "Transferred ₹" + amt + " to " + receiver);
        logTransaction(receiver, "Received ₹" + amt + " from " + sender);
        System.out.println("Transfer Successful! Your New Balance: ₹" + sAcc.getBalance());
    }

    public void showHistory(long accNo) {
        System.out.println("\n--- TRANSACTION HISTORY ---");
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (line.startsWith(accNo + " |")) {
                    System.out.println(line);
                    found = true;
                }
            }
        } catch (IOException e) { /* ignore */ }
        if (!found) System.out.println("No history found.");
        System.out.println("---------------------------");
    }

    // Getters for Viva
    public static String getUsersFile() { return USERS_FILE; }
    public static String getAccountsFile() { return ACCOUNTS_FILE; }
    public static String getTransactionsFile() { return TRANSACTIONS_FILE; }

    // --- Support Logic ---
    private List<Account> loadAllAccounts() {
        List<Account> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] d = line.split(",");
                if (d.length > 4) {
                    list.add(new Account(Long.parseLong(d[0]), d[1], d[2], new BigDecimal(d[3]), d[4]));
                }
            }
        } catch (IOException e) { /* ignore */ }
        return list;
    }

    private void rewriteAccounts(List<Account> list) {
        try (FileWriter fw = new FileWriter(ACCOUNTS_FILE)) {
            for (Account a : list) fw.write(a.toString() + "\n");
        } catch (IOException e) {
            System.out.println("File Error: Operations could not be finalized.");
        }
    }

    public boolean verifyPin(long accNo) {
        for (int i = 0; i < 3; i++) {
            System.out.print("Enter PIN: ");
            String pinInput = sc.nextLine().trim();
            try (BufferedReader br = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] d = line.split(",");
                    if (d.length > 4 && Long.parseLong(d[0]) == accNo && d[4].equals(pinInput)) {
                        return true;
                    }
                }
            } catch (IOException e) { /* ignore */ }
            System.out.println("Invalid PIN! Attempts left: " + (2 - i));
        }
        return false;
    }

    private void logTransaction(long accNo, String detail) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (FileWriter fw = new FileWriter(TRANSACTIONS_FILE, true)) {
            fw.write(accNo + " | " + date + " | " + detail + "\n");
        } catch (IOException e) { /* ignore */ }
    }
}
