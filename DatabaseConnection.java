import java.sql.*;

public class DatabaseConnection 
{    
private Connection connection;
    
public DatabaseConnection() 
{
	try 
	{            
	Class.forName("oracle.jdbc.driver.OracleDriver");            
	connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "system", "user");
        System.out.println("Database connected successfully");
        } 
	catch (Exception e) 
	{            
	e.printStackTrace();
        }
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    
	public boolean createAccount(int accountNumber, String accountHolder, double initialBalance) 
	{        
	try 
	{
            String query = "INSERT INTO bankaccount (account_number, account_holder_name, balance) VALUES (?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, accountNumber);
            pstmt.setString(2, accountHolder);
            pstmt.setInt(3, (int) initialBalance);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } 
	catch (SQLException e) 
	{
            e.printStackTrace();
            return false;
        }
        }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	public boolean logTransaction(int accountNumber, String transactionType, double amount) {
	try 
	{
            String query = "INSERT INTO transaction_history (account_number, transaction_type, amount, transaction_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, accountNumber);
            pstmt.setString(2, transactionType);
            pstmt.setDouble(3, amount);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } 
	catch (SQLException e) 
	{
            e.printStackTrace();
            return false;
        }
        }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	public double checkBalance(int accountNumber) 
	{
	double balance = 0;
	try 
	{
            String query = "SELECT balance FROM bankaccount WHERE account_number = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
            balance = rs.getDouble("balance");
            }
        } 
	catch (SQLException e) 
	{
        e.printStackTrace();
        }
        return balance;
    	}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


        public boolean withdraw(int accountNumber, double amount) 
	{
       	    try 
	    {
            double currentBalance = checkBalance(accountNumber);
            if (currentBalance >= amount) 
		{
                String query = "UPDATE bankaccount SET balance = balance - ? WHERE account_number = ?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, (int) amount);
                pstmt.setInt(2, accountNumber);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                logTransaction(accountNumber, "Withdraw", amount);
                return true;
                }
         	}
                return false;
         	} 
		catch (SQLException e) 
		{
                e.printStackTrace();
                return false;
        	} 
   		}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    public boolean deposit(int accountNumber, double amount) {
        try {
            String query = "UPDATE bankaccount SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, (int) amount);
            pstmt.setInt(2, accountNumber);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logTransaction(accountNumber, "Deposit", amount);
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    public boolean transfer(int fromAccount, int toAccount, double amount) {
        try {
            connection.setAutoCommit(false);
            if (withdraw(fromAccount, amount)) {
                if (deposit(toAccount, amount)) {
                    connection.commit();
                    return true;
                }
                connection.rollback();
            }
            connection.rollback();
            return false;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        public boolean hasExistingLoan(int accountNumber) 
	{
        try 
	{
            String query = "SELECT COUNT(*) as loan_count FROM loans_2 WHERE account_number = ? AND status != 'CLOSED'";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) 
	    {
                return rs.getInt("loan_count") > 0;
            }
            return false;
        } 
	catch (SQLException e) 
	{
            e.printStackTrace();
            return false;
        }
    	}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public boolean createLoan(int accountNumber, double amount, int term) {
        try 
	{
        if (hasExistingLoan(accountNumber)) 
	{
            return false;
        }
            
            String query = "INSERT INTO loans_2 (loan_id, account_number, loan_amount, term_months, status, creation_date) VALUES (loan_id_seq.NEXTVAL, ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, accountNumber);
            pstmt.setDouble(2, amount);
            pstmt.setInt(3, term);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } 
	catch (SQLException e) 
	{
            e.printStackTrace();
            return false;
        }
    	}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        public String checkLoans(int accountNumber) 
        {
        try 
	{
            String query = "SELECT loan_amount, term_months, status, creation_date FROM loans_2 WHERE account_number = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            StringBuilder result = new StringBuilder();
         while (rs.next()) 
	{
         result.append(String.format("Amount: $%.2f, Term: %d months, Status: %s, Date: %s\n",
         rs.getDouble("loan_amount"),
         rs.getInt("term_months"),
         rs.getString("status"),
         rs.getTimestamp("creation_date")));
         }
        return result.length() > 0 ? result.toString() : "No loans found";
        } 
	catch (SQLException e) 
	{
      	e.printStackTrace();
        return "Error retrieving loans";
        }
    	}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    	public boolean createInvestment(int accountNumber, String type, double amount, int term) {
    	try 
	{
            double currentBalance = checkBalance(accountNumber);
            if (currentBalance < amount) 
	    {
             System.err.println("Insufficient balance for investment");
             return false;
            }
            double interestRate = calculateInterestRate(type, term);
            connection.setAutoCommit(false);
            try {
                String updateQuery = "UPDATE bankaccount SET balance = balance - ? WHERE account_number = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setDouble(1, amount);
                updateStmt.setInt(2, accountNumber);
                updateStmt.executeUpdate();

                String query = "INSERT INTO investments_2 (account_number, investment_type, amount, term_months, interest_rate, creation_date) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, accountNumber);
                pstmt.setString(2, type);
                pstmt.setDouble(3, amount);
                pstmt.setInt(4, term);
                pstmt.setDouble(5, interestRate);
                int rowsAffected = pstmt.executeUpdate();

                connection.commit();
                return rowsAffected > 0;
                } 
		catch (SQLException e) 
		{
                connection.rollback();
                throw e;
            	} 
		finally 
		{
                connection.setAutoCommit(true);            
		}
        	} 
		catch (SQLException e) 	
		{            
		e.printStackTrace();
            	return false;
        	}
    		}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        private double calculateInterestRate(String investmentType, int term) 
	{
            switch (investmentType) {
            case "FIXED_DEPOSIT":
                if (term <= 6) return 5.5;
                else if (term <= 12) return 6.0;
                else return 6.5;
            case "MUTUAL_FUND":
                if (term <= 12) return 8.0;
                else if (term <= 24) return 9.0;
                else return 10.0;
            case "STOCKS":
                if (term <= 12) return 12.0;
                else if (term <= 24) return 13.0;
                else return 14.0;
            default:
                return 5.0; // Default rate
        }
    	}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        public String checkInvestments(int accountNumber) 
	{
        try 
	{
            String query = "SELECT investment_type, amount, term_months, interest_rate, creation_date FROM investments_2 WHERE account_number = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            StringBuilder result = new StringBuilder();
            while (rs.next()) {
            result.append(String.format("Type: %s, Amount: $%.2f, Term: %d months, Interest Rate: %.2f%%, Date: %s\n",
            rs.getString("investment_type"),
            rs.getDouble("amount"),
            rs.getInt("term_months"),
            rs.getDouble("interest_rate"),
            rs.getTimestamp("creation_date")));
         }
         return result.length() > 0 ? result.toString() : "No investments found";
         } 
	 catch (SQLException e) 
	 {            
	 e.printStackTrace();
         return "Error retrieving investments";
         }
    	 }
 	 }