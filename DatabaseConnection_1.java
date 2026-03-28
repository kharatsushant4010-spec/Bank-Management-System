import java.sql.*;
import java.util.Date;

public class DatabaseConnection_1 {
    private Connection connection;
    
    public DatabaseConnection_1() {
        try {
            // Make sure to add ojdbc14.jar to your classpath
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe", "system", "user"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Create new customer
    public int createCustomer(String name, String address, String phone, String email) {
        try {
            String query = "INSERT INTO customers_1 (customer_id, name, address, phone, email) " +
                          "VALUES (customer_seq.nextval, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query, 
                new String[] {"customer_id"});
            pstmt.setString(1, name);
            pstmt.setString(2, address);
            pstmt.setString(3, phone);
            pstmt.setString(4, email);
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // Create new account
    public boolean createAccount(int customerId, int typeId, double initialDeposit) {
        try {
            String query = "INSERT INTO accounts_1 (account_number, customer_id, type_id, " +
                          "balance, opening_date, status) VALUES " +
                          "(account_seq.nextval, ?, ?, ?, SYSDATE, 'ACTIVE')";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, typeId);
            pstmt.setDouble(3, initialDeposit);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Check balance
    public double checkBalance(int accountNumber) {
        try {
            String query = "SELECT balance FROM accounts_1 WHERE account_number = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // Deposit
    public boolean deposit(int accountNumber, double amount) {
        try {
            String query = "UPDATE accounts_1 SET balance = balance + ? " +
                          "WHERE account_number = ? AND status = 'ACTIVE'";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accountNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Withdraw
    public boolean withdraw(int accountNumber, double amount) {
        try {
            double currentBalance = checkBalance(accountNumber);
            if (currentBalance >= amount) {
                String query = "UPDATE accounts_1 SET balance = balance - ? " +
                              "WHERE account_number = ? AND status = 'ACTIVE'";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, accountNumber);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Apply for loan
    public boolean applyLoan(int customerId, double amount, double interestRate, 
                           int durationMonths) {
        try {
            String query = "INSERT INTO loans (loan_id, customer_id, loan_amount, " +
                          "interest_rate, start_date, end_date, status) VALUES " +
                          "(loan_seq.nextval, ?, ?, ?, SYSDATE, " +
                          "ADD_MONTHS(SYSDATE, ?), 'PENDING')";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, customerId);
            pstmt.setDouble(2, amount);
            pstmt.setDouble(3, interestRate);
            pstmt.setInt(4, durationMonths);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Create investment
    public boolean createInvestment(int customerId, double amount, String investmentType, 
                                  double interestRate, int durationMonths) {
        try {
            String query = "INSERT INTO investments (investment_id, customer_id, amount, " +
                          "investment_type, start_date, maturity_date, interest_rate) " +
                          "VALUES (investment_seq.nextval, ?, ?, ?, SYSDATE, " +
                          "ADD_MONTHS(SYSDATE, ?), ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, customerId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, investmentType);
            pstmt.setInt(4, durationMonths);
            pstmt.setDouble(5, interestRate);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}