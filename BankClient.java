import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class BankClient extends JFrame implements ActionListener 
{
    private JTextField accountNumberField, amountField, accountHolderField, initialBalanceField;
    private JTextField loanAmountField, loanTermField, investmentAmountField;
    private JButton withdrawButton, depositButton, transferButton, balanceCheckButton, createAccountButton;
    private JButton applyLoanButton, checkLoansButton, createInvestmentButton, checkInvestmentsButton;
    private JComboBox<String> investmentTypeCombo;
    
    private PrintWriter out;
    private BufferedReader in;
    private Socket clientSocket;
    
    private JDialog createAccountDialog;
    private JTextField newAccountNameField, newAccountNumberField, newInitialBalanceField;
    
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 500;
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12346;

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public BankClient() 
    {
        initializeFrame();
        initializeNetworkConnection();
        createMainPanel();
        createAccountDialog();
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void initializeFrame() 
    {
        setTitle("Bank Management System");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void initializeNetworkConnection() 
    {
        try 
        {
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } 
        catch (IOException e) 
        {
            showError("Failed to connect to server: " + e.getMessage());
            System.exit(1);
        }
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

   private void createMainPanel() 
{
    JPanel mainPanel = new JPanel();
    mainPanel.setBackground(new Color(30, 144, 255)); // Light blue background
    mainPanel.setLayout(null);

    JLabel titleLabel = new JLabel("Bank Management System");
    titleLabel.setForeground(Color.WHITE);

    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
    titleLabel.setBounds(250, 10, 500, 30);
    mainPanel.add(titleLabel);

    createAccountSection(mainPanel);
    createTransactionSection(mainPanel);        
    createLoanSection(mainPanel);        
    createInvestmentSection(mainPanel);

    add(mainPanel);
}


//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void createAccountSection(JPanel panel) 
    {
        addLabel(panel, "Account Number:", 50, 50);
        accountNumberField = addTextField(panel, 200, 50);

        addLabel(panel, "Account Holder:", 50, 100);
        accountHolderField = addTextField(panel, 200, 100);

        addLabel(panel, "Amount:", 50, 150);
        amountField = addTextField(panel, 200, 150);

        createAccountButton = addButton(panel, "Create Account", 50, 250, 150, 30);
        balanceCheckButton = addButton(panel, "Check Balance", 210, 250, 150, 30);
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void createTransactionSection(JPanel panel) 
    {
        withdrawButton = addButton(panel, "Withdraw", 50, 200, 120, 30);
        depositButton = addButton(panel, "Deposit", 180, 200, 120, 30);
        transferButton = addButton(panel, "Transfer", 310, 200, 120, 30);
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void createLoanSection(JPanel panel) 
    {
        addLabel(panel, "Loan Amount:", 50, 300);
        loanAmountField = addTextField(panel, 200, 300);

        addLabel(panel, "Loan Term (months):", 50, 330);
        loanTermField = addTextField(panel, 200, 330);

        applyLoanButton = addButton(panel, "Apply for Loan", 50, 360, 150, 30);
        checkLoansButton = addButton(panel, "Check Loans", 210, 360, 150, 30);
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void createInvestmentSection(JPanel panel) 
    {
        addLabel(panel, "Investment Type:", 380, 300);
        investmentTypeCombo = new JComboBox<>(new String[]{"FIXED_DEPOSIT", "MUTUAL_FUND", "STOCKS"});
        investmentTypeCombo.setBounds(530, 300, 150, 25);
        panel.add(investmentTypeCombo);

        addLabel(panel, "Investment Amount:", 380, 330);
        investmentAmountField = addTextField(panel, 530, 330);

        createInvestmentButton = addButton(panel, "Create Investment", 380, 360, 150, 30);
        checkInvestmentsButton = addButton(panel, "Check Investments", 540, 360, 150, 30);
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void createAccountDialog() 
    {
        createAccountDialog = new JDialog(this, "Create New Account", true);
        createAccountDialog.setSize(400, 300);
        createAccountDialog.setLayout(null);
        createAccountDialog.setLocationRelativeTo(this);
	createAccountDialog.setBackground(new Color(30, 144, 255));		
        addLabel(createAccountDialog, "Account Name:", 30, 30);
        newAccountNameField = addTextField(createAccountDialog, 150, 30);

        addLabel(createAccountDialog, "Account Number:", 30, 80);
        newAccountNumberField = addTextField(createAccountDialog, 150, 80);

        addLabel(createAccountDialog, "Initial Balance:", 30, 130);
        newInitialBalanceField = addTextField(createAccountDialog, 150, 130);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 180, 100, 30);
        submitButton.addActionListener(e -> handleCreateAccount());
        createAccountDialog.add(submitButton);
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private JLabel addLabel(Container container, String text, int x, int y) 
    {
        JLabel label = new JLabel(text);
        label.setForeground(Color.BLACK);
        label.setBounds(x, y, 150, 25);
        container.add(label);
        return label;
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private JTextField addTextField(Container container, int x, int y) 
    {
        JTextField field = new JTextField();
        field.setBounds(x, y, 150, 25);
        container.add(field);
        return field;
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private JButton addButton(Container container, String text, int x, int y, int width, int height) 
    {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        button.addActionListener(this);
        container.add(button);
        return button;
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void showError(String message) 
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void showMessage(String message) 
    {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void clearCreateAccountFields() 
    {
        newAccountNameField.setText("");
        newAccountNumberField.setText("");
        newInitialBalanceField.setText("");
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void clearMainFields() 
    {
        accountNumberField.setText("");
        accountHolderField.setText("");
        amountField.setText("");
        loanAmountField.setText("");
        loanTermField.setText("");
        investmentAmountField.setText("");
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        try 
        {
            String accountNumber = accountNumberField.getText().trim();
            
            if (e.getSource() == createAccountButton) 
            {
                createAccountDialog.setVisible(true);
                return;
            }

            if (accountNumber.isEmpty() && e.getSource() != createAccountButton) 
            {
                showError("Please enter an account number");
                return;
            }

            handleAction(e, accountNumber);

        } 
        catch (Exception ex) 
        {
            showError("Error: " + ex.getMessage());
        }
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleAction(ActionEvent e, String accountNumber) 
    {
        try 
        {
            if (e.getSource() == withdrawButton) 
            {
                handleWithdraw(accountNumber);
            } 
            else if (e.getSource() == depositButton) 
            {
                handleDeposit(accountNumber);            
            } 
            else if (e.getSource() == transferButton) 
            {
                handleTransfer(accountNumber);            
            } 
            else if (e.getSource() == balanceCheckButton)     
            {
                handleBalanceCheck(accountNumber);
            } 
            else if (e.getSource() == applyLoanButton) 
            {
                handleLoanApplication(accountNumber);
            } 
            else if (e.getSource() == checkLoansButton) 
            {
                handleCheckLoans(accountNumber);
            } 
            else if (e.getSource() == createInvestmentButton) 
            {
                handleCreateInvestment(accountNumber);
            } 
            else if (e.getSource() == checkInvestmentsButton) 
            {
                handleCheckInvestments(accountNumber);
            }
        } 
        catch (Exception ex) 
        {
            showError("Error: " + ex.getMessage());
        }
    }
    
//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleCreateAccount() 
    {
        String name = newAccountNameField.getText().trim();
        String accountNum = newAccountNumberField.getText().trim();
        String initialBalance = newInitialBalanceField.getText().trim();

        if (name.isEmpty() || accountNum.isEmpty() || initialBalance.isEmpty()) 
        {
            showError("Please fill in all fields");
            return;
        }

        out.println("CREATE_ACCOUNT," + name + "," + accountNum + "," + initialBalance);
        clearCreateAccountFields();
        createAccountDialog.dispose();
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

 


//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleWithdraw(String accountNumber) throws IOException {
        validateAmount();
        out.println("Withdraw:" + accountNumber + ":" + amountField.getText());
        showResponse();
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleDeposit(String accountNumber) throws IOException {
        validateAmount();
        out.println("Deposit:" + accountNumber + ":" + amountField.getText());
        showResponse();
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleTransfer(String accountNumber) throws IOException {
        validateAmount();
        String toAccount = JOptionPane.showInputDialog(this, "Enter account number to transfer to:");
        if (toAccount != null && !toAccount.trim().isEmpty()) {
            out.println("Transfer:" + accountNumber + ":" + toAccount + ":" + amountField.getText());
            showResponse();
        }
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleBalanceCheck(String accountNumber) throws IOException {
        out.println("BalanceCheck:" + accountNumber);
        showResponse();
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleLoanApplication(String accountNumber) throws IOException {
        String loanAmount = loanAmountField.getText().trim();
        String loanTerm = loanTermField.getText().trim();
        
        if (loanAmount.isEmpty() || loanTerm.isEmpty()) {
            showError("Please enter loan amount and term");
            return;
        }

        try {
            double amount = Double.parseDouble(loanAmount);
            int term = Integer.parseInt(loanTerm);
            
            if (amount <= 0 || term <= 0) {
                showError("Amount and term must be greater than 0");
                return;
            }

            out.println("ApplyLoan:" + accountNumber + ":" + loanAmount + ":" + loanTerm);
            String response = in.readLine();
            
            if (response.startsWith("Not eligible")) {
                showError(response);
            } else {
                showMessage(response);
            }
        } catch (NumberFormatException e) {
            showError("Invalid amount or term format");
        }
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

     private void handleCheckLoans(String accountNumber) throws IOException {
        out.println("CheckLoans:" + accountNumber);
        showResponse();
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleCreateInvestment(String accountNumber) throws IOException {
        String investmentAmount = investmentAmountField.getText().trim();
        if (investmentAmount.isEmpty()) {
            showError("Please enter investment amount");
            return;
        }

        try {
            double amount = Double.parseDouble(investmentAmount);
            if (amount <= 0) {
                showError("Investment amount must be greater than 0");
                return;
            }

            String investmentType = (String) investmentTypeCombo.getSelectedItem();
            String investmentTerm = JOptionPane.showInputDialog(this, 
                "Enter investment term (months):\n\n" +
                "Interest Rates:\n" +
                "Fixed Deposit: 5.5-6.5%\n" +
                "(Rates vary based on term length)");
            
            if (investmentTerm != null && !investmentTerm.trim().isEmpty()) {
                out.println("CreateInvestment:" + accountNumber + ":" + investmentType + ":" + 
                          investmentAmount + ":" + investmentTerm);
                showResponse();
            }
        } catch (NumberFormatException e) {
            showError("Invalid amount format");
        }
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void handleCheckInvestments(String accountNumber) throws IOException {
        out.println("CheckInvestments:" + accountNumber);
        showResponse();
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void validateAmount() {
        String amount = amountField.getText().trim();
        if (amount.isEmpty()) {
            throw new IllegalArgumentException("Please enter an amount");
        }
        try {
            double value = Double.parseDouble(amount);
            if (value <= 0) {
                throw new IllegalArgumentException("Amount must be greater than 0");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format");
        }
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void showResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        boolean started = false;
        
        while ((line = in.readLine()) != null) {
            if (line.equals("RESPONSE_START")) {
                started = true;
                continue;
            }
            if (line.equals("RESPONSE_END")) {
                break;
            }
            if (started) {
                if (response.length() > 0) {
                    response.append("\n");
                }
                response.append(line);
            }
        }
        
        showMessage(response.toString());
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new BankClient().setVisible(true);
        });
    }
}