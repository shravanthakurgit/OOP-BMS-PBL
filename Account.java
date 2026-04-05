import java.math.BigDecimal;

public class Account {
    private long accountNo;
    private String name;
    private String email;
    private BigDecimal balance;
    private String pin;

    public Account(long accountNo, String name, String email, BigDecimal balance, String pin) {
        this.accountNo = accountNo;
        this.name = name;
        this.email = email;
        this.balance = balance;
        this.pin = pin;
    }

    // Getters and Setters
    public long getAccountNo() { return accountNo; }
    public void setAccountNo(long accountNo) { this.accountNo = accountNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    @Override
    public String toString() {
        return accountNo + "," + name + "," + email + "," + balance + "," + pin;
    }
}
