import java.io.*;
import java.net.*;

public class BankServer 
{    
private ServerSocket serverSocket;    
private DatabaseConnection dbConnection;    
private static final int PORT = 12346;

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

public BankServer(int port) 
{      
try 
{
serverSocket = new ServerSocket(port);
dbConnection = new DatabaseConnection();           
System.out.println("Bank server started on port " + port);
} 
catch (IOException e) 
{
e.printStackTrace();       
}    
}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

public void start() 
{        
while (true) 
{            
try 
{                
Socket clientSocket = serverSocket.accept();               
System.out.println("New client connected");               
ClientHandler clientHandler = new ClientHandler(clientSocket, dbConnection);                
new Thread(clientHandler).start();            
} 
catch (IOException e) 
{                
e.printStackTrace();            
}        
}    
}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

public static void main(String[] args) 
{        
BankServer server = new BankServer(PORT);     
server.start();    
}
}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

class ClientHandler implements Runnable 
{
    private Socket clientSocket;
    private DatabaseConnection dbConnection;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket clientSocket, DatabaseConnection dbConnection) 
	{
        this.clientSocket = clientSocket;
        this.dbConnection = dbConnection;
        try 
	{
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } 
	catch (IOException e) 
	{
            e.printStackTrace();
        }
   	}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void run()
    {
        try 
	{
            String request;
            while ((request = in.readLine()) != null) 
	    {
                String[] parts = request.split(":");
                String command = parts[0];

                try {
                    switch (command) 
		    {
                        case "CreateAccount":
                            handleCreateAccount(parts);
                            break;
                        case "Withdraw":
                            handleWithdraw(parts);
                            break;
                        case "Deposit":
                            handleDeposit(parts);
                            break;
                        case "Transfer":
                            handleTransfer(parts);
                            break;
                        case "BalanceCheck":
                            handleBalanceCheck(parts);
                            break;
                        case "ApplyLoan":
                            handleLoanApplication(parts);
                            break;
                        case "CheckLoans":
                            handleCheckLoans(parts);
                            break;
                        case "CreateInvestment":
                            handleCreateInvestment(parts);
                            break;
                        case "CheckInvestments":
                            handleCheckInvestments(parts);
                            break;
                        default:
                            out.println("RESPONSE_START");
                            out.println("Unknown command");
                            out.println("RESPONSE_END");
                    }
                } 
		catch (Exception e) 
		{
                    out.println("RESPONSE_START");
                    out.println("Error processing request: " + e.getMessage());
                    out.println("RESPONSE_END");
                    e.printStackTrace();
                }
                }
        } 
	catch (IOException e) 
	{
         e.printStackTrace();
        } 

	finally 
	{
        try 
	{
         clientSocket.close();
         } 
	 catch (IOException e) 
	 {
          e.printStackTrace();
         }
         }
    	 }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    private void handleCreateAccount(String[] parts) 
	{
        out.println("RESPONSE_START");
        try 
	{
            int accountNumber = Integer.parseInt(parts[1]);
            String accountHolder = parts[2];
            double initialBalance = Double.parseDouble(parts[3]);
            boolean success = dbConnection.createAccount(accountNumber, accountHolder, initialBalance);
            out.println(success ? "Account created successfully" : "Failed to create account");
        } 
	catch (NumberFormatException e) 
	{
            out.println("Invalid account number or initial balance format");
        }
        out.println("RESPONSE_END");
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleWithdraw(String[] parts) 
	{
        out.println("RESPONSE_START");
        try 
	{
            int accountNumber = Integer.parseInt(parts[1]);
            double amount = Double.parseDouble(parts[2]);
            boolean success = dbConnection.withdraw(accountNumber, amount);
            out.println(success ? "Withdrawal successful" : "Withdrawal failed - Insufficient balance or account not found");
        } 
	catch (NumberFormatException e) 
	{
            out.println("Invalid account number or amount format");
        }
        out.println("RESPONSE_END");
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleDeposit(String[] parts) 
    {
        out.println("RESPONSE_START");
        try 
	{
            int accountNumber = Integer.parseInt(parts[1]);
            double amount = Double.parseDouble(parts[2]);
            boolean success = dbConnection.deposit(accountNumber, amount);
            out.println(success ? "Deposit successful" : "Deposit failed - Account not found");
        } 
	catch (NumberFormatException e) 
	{
            out.println("Invalid account number or amount format");
        }
        out.println("RESPONSE_END");
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleTransfer(String[] parts) 
	{
        out.println("RESPONSE_START");
        try 
	{
            int fromAccount = Integer.parseInt(parts[1]);
            int toAccount = Integer.parseInt(parts[2]);
            double amount = Double.parseDouble(parts[3]);
            boolean success = dbConnection.transfer(fromAccount, toAccount, amount);
            out.println(success ? "Transfer successful" : "Transfer failed");
        } catch (NumberFormatException e) 
	{
            out.println("Invalid account number or amount format");
        }
        out.println("RESPONSE_END");
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleBalanceCheck(String[] parts) 
    {
        out.println("RESPONSE_START");
        try 
	{
            int accountNumber = Integer.parseInt(parts[1]);
            double balance = dbConnection.checkBalance(accountNumber);
            out.println(String.format("Current balance: %.2f", balance));
        } 
	catch (NumberFormatException e) 
	{
            out.println("Invalid account number format");
        }
        out.println("RESPONSE_END");
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleLoanApplication(String[] parts) 
    {
        out.println("RESPONSE_START");
        try 
	{
            int accountNumber = Integer.parseInt(parts[1]);
            double amount = Double.parseDouble(parts[2]);
            int term = Integer.parseInt(parts[3]);

            if (dbConnection.hasExistingLoan(accountNumber)) 
	    {
                out.println("Not eligible: You already have an existing loan");
            }
	else 
	{
                boolean success = dbConnection.createLoan(accountNumber, amount, term);
                out.println(success ? "Loan application submitted successfully" : "Failed to submit loan application");
        }
        } 
	catch (NumberFormatException e) 
	{
            out.println("Invalid number format in loan details");
        }
        out.println("RESPONSE_END");
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleCheckLoans(String[] parts) 
    {
        out.println("RESPONSE_START");
        try 
	{
            int accountNumber = Integer.parseInt(parts[1]);
            String loans = dbConnection.checkLoans(accountNumber);
            out.println(loans);
        } 
	catch (NumberFormatException e) 
	{
            out.println("Invalid account number format");
        }
        out.println("RESPONSE_END");
    	}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleCreateInvestment(String[] parts) 
	{
        out.println("RESPONSE_START");
        try 
	{
            int accountNumber = Integer.parseInt(parts[1]);
            String type = parts[2];
            double amount = Double.parseDouble(parts[3]);
            int term = Integer.parseInt(parts[4]);
            boolean success = dbConnection.createInvestment(accountNumber, type, amount, term);
            if (success) 
	{
            String investments = dbConnection.checkInvestments(accountNumber);
            out.println("Investment created successfully");
            out.println(investments);
        } 
	else 
	{
            out.println("Failed to create investment");      
	}
        } 
	catch (NumberFormatException e) 
	{
            out.println("Invalid number format in investment details");
        }
        out.println("RESPONSE_END");
    	}

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleCheckInvestments(String[] parts) 
	{
        out.println("RESPONSE_START");
        try 
	{
            int accountNumber = Integer.parseInt(parts[1]);
            String investments = dbConnection.checkInvestments(accountNumber);
            out.println(investments);
        } 
	catch (NumberFormatException e) 
	{
            out.println("Invalid account number format");
        }
        out.println("RESPONSE_END");
        }
	}