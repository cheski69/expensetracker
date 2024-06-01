package expensetracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Expensetracker {

    // Variables for the main frame and UI components
    private JFrame frame;

    private JPanel titleBar;
    private JLabel closeLabel;
    private JLabel minimizeLabel;
    private JLabel titleLabel;
    private JPanel dashboardPanel;
    private JPanel buttonsPanel;
    private JButton addTransactionButton;
    private JButton removeTransactionButton;
    private JTable transactionTable;
    private DefaultTableModel tableModel;

    // Variables to store the total amount
    private double totalAmount = 0.0;
    private double totalExpense = 0.0;
    private double totalIncome = 0.0;

    private JPanel expensePanel;
    private JPanel incomePanel;
    private JPanel totalPanel;

    // Constructor
    public Expensetracker() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);

        // Remove form border and default close and minimize buttons
        frame.setUndecorated(true);

        // Set custom border to the frame
        frame.getRootPane().setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(52, 73, 94)));

        // Create and set up the title bar
        titleBar = new JPanel();
        titleBar.setLayout(null);
        titleBar.setBackground(Color.BLUE);
        titleBar.setPreferredSize(new Dimension(frame.getWidth(), 30));
        frame.add(titleBar, BorderLayout.NORTH);

        // Create and set up the title label
        titleLabel = new JLabel("Expense and Income Tracker");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 17));
        titleLabel.setBounds(10, 0, 250, 30);
        titleBar.add(titleLabel);

        // Create and set up the close label
        closeLabel = new JLabel("x");
        closeLabel.setForeground(Color.WHITE);
        closeLabel.setFont(new Font("Arial", Font.BOLD, 17));
        closeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        closeLabel.setBounds(frame.getWidth() - 50, 0, 30, 30);
        closeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add mouse listeners for close label interactions
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                closeLabel.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeLabel.setForeground(Color.WHITE);
            }
        });
        titleBar.add(closeLabel);

        // Create and set up the minimize label
        minimizeLabel = new JLabel("-");
        minimizeLabel.setForeground(Color.WHITE);
        minimizeLabel.setFont(new Font("Arial", Font.BOLD, 17));
        minimizeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        minimizeLabel.setBounds(frame.getWidth() - 80, 0, 30, 30);
        minimizeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add mouse listeners for minimize label interactions
        minimizeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frame.setState(JFrame.ICONIFIED);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                minimizeLabel.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                minimizeLabel.setForeground(Color.WHITE);
            }
        });
        titleBar.add(minimizeLabel);

        // Set up form dragging functionality
        titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isDragging = true;
                mouseOffset = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
            }
        });

        titleBar.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    // When the mouse is dragged, this event is triggered
                    Point newLocation = e.getLocationOnScreen();
                    newLocation.translate(-mouseOffset.x, -mouseOffset.y);
                    frame.setLocation(newLocation);
                }
            }
        });

        // Create and set up the dashboard panel
        dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        dashboardPanel.setBackground(new Color(236, 240, 241));
        frame.add(dashboardPanel, BorderLayout.CENTER);

        // Add data panels for expense, income, and total
        expensePanel = addDataPanel("Expense", 0);
        incomePanel = addDataPanel("Income", 1);
        totalPanel = addDataPanel("Total", 2);

        // Create and set up button panel
        addTransactionButton = new JButton("Add transaction");
        addTransactionButton.setBackground(new Color(41, 128, 185));
        addTransactionButton.setForeground(Color.WHITE);
        addTransactionButton.setFocusPainted(false);
        addTransactionButton.setBorderPainted(false);
        addTransactionButton.setFont(new Font("Arial", Font.BOLD, 14));
        addTransactionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addTransactionButton.addActionListener((e) -> showAddTransactionDialog());

        removeTransactionButton = new JButton("Remove transaction");
        removeTransactionButton.setBackground(new Color(231, 76, 60));
        removeTransactionButton.setForeground(Color.WHITE);
        removeTransactionButton.setFocusPainted(false);
        removeTransactionButton.setBorderPainted(false);
        removeTransactionButton.setFont(new Font("Arial", Font.BOLD, 14));
        removeTransactionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeTransactionButton.addActionListener((e) -> removeTransaction());

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(2, 1, 10, 5));
        buttonsPanel.add(addTransactionButton);
        buttonsPanel.add(removeTransactionButton);
        dashboardPanel.add(buttonsPanel);

        // Set up the transaction table
        String[] columnNames = {"ID", "Type", "Description", "Amount"};
        tableModel = new DefaultTableModel(columnNames, 0);
        transactionTable = new JTable(tableModel);
        configureTransactionTable();

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        configureScrollPane(scrollPane);
        dashboardPanel.add(scrollPane);

        frame.setVisible(true);
    }

    // Configure the appearance and behavior of the transaction table
    private void configureTransactionTable() {
        transactionTable.setBackground(new Color(236, 240, 241));
        transactionTable.setRowHeight(25);
        transactionTable.setShowGrid(false);
        transactionTable.setBorder(null);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader tableHeader = transactionTable.getTableHeader();
        tableHeader.setForeground(Color.BLACK);
        tableHeader.setFont(new Font("Arial", Font.BOLD, 15));
        tableHeader.setDefaultRenderer(new GradientHeaderRenderer());
    }

    // Configure the appearance of the scroll pane
    private void configureScrollPane(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    // Displays the dialog for adding a new transaction
    private void showAddTransactionDialog() {
        JDialog dialog = new JDialog(frame, "Add Transaction", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(frame);

        JPanel dialogPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialogPanel.setBackground(Color.LIGHT_GRAY);

        JLabel idLabel = new JLabel("ID:");
        JTextField idField = new JTextField();

        JLabel typeLabel = new JLabel("Type:");
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Expense", "Income"});

        JLabel descriptionLabel = new JLabel("Description:");
        JTextField descriptionField = new JTextField();

        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField();

        JButton addButton = new JButton("Add");
        addButton.setBackground(new Color(41, 128, 185));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> {
            
            String id = idField.getText();
            String type = (String) typeComboBox.getSelectedItem();
            String description = descriptionField.getText();
            double amount = Double.parseDouble(amountField.getText());
            addTransaction(id, type, description, amount);
            dialog.dispose();

        });

        
        //ad components to the dialog panel
        dialogPanel.add(idLabel);
        dialogPanel.add(idField);
        dialogPanel.add(typeLabel);
        dialogPanel.add(typeComboBox);
        dialogPanel.add(descriptionLabel);
        dialogPanel.add(descriptionField);
        dialogPanel.add(amountLabel);
        dialogPanel.add(amountField);
        dialogPanel.add(new JLabel());
        dialogPanel.add(addButton);

        dialog.add(dialogPanel);
        dialog.setVisible(true);
    }

    // Add a new transaction to the database
    private void addTransaction(JComboBox<String> typeComboBox, JTextField descriptionField, JTextField amountField) {
        
        
        // Retrieve transaction details from the input fields
        String type = (String) typeComboBox.getSelectedItem();
        String description = descriptionField.getText();
        String amount = amountField.getText();

        try {
            Connection connection = DatabaseConnection.getConnection();
            String insertQuery = "INSERT INTO `transaction_table`( `transaction_type`, `description`, `amount`) VALUES (?,?,?)";
            PreparedStatement ps = connection.prepareStatement(insertQuery);
            ps.setString(1, type);
            ps.setString(2, description);
            ps.setDouble(3, Double.parseDouble(amount));
            ps.executeUpdate();
            System.out.println("Data inserted successfully");

        } catch (SQLException ex) {
            System.out.println("Error !! Data  not inserted ");
            
        }
    }

    // Add a new transaction to the table and update totals
    private void addTransaction(String id, String type, String description, double amount) {
        tableModel.addRow(new Object[]{id, type, description, amount});
        updateTotalAmount(type, amount);
    }

    // Remove the selected transaction from the table and update totals
    private void removeTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow != -1) {
            String type = (String) transactionTable.getValueAt(selectedRow, 1);
            double amount = (double) transactionTable.getValueAt(selectedRow, 3);
            tableModel.removeRow(selectedRow);
            updateTotalAmount(type.equals("Income") ? "Expense" : "Income", amount);
        }
    }

    // Update the total amount, income, and expense based on the transaction type
    private void updateTotalAmount(String type, double amount) {
        if (type.equals("Income")) {
            totalIncome += amount;
        } else {
            totalExpense += amount;
        }
        totalAmount = totalIncome - totalExpense;

        // Update the display panels with new values
        updatePanel(expensePanel, "Expense", totalExpense);
        updatePanel(incomePanel, "Income", totalIncome);
        updatePanel(totalPanel, "Total", totalAmount);
    }

    // Create and return a data panel for displaying totals
    private JPanel addDataPanel(String title, int index) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(200, 100));
        panel.setBorder(new LineBorder(Color.BLACK, 1, true));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.NORTH);

        JLabel amountLabel = new JLabel("$0.00");
        amountLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        amountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(amountLabel, BorderLayout.CENTER);

        dashboardPanel.add(panel, index);
        return panel;
    }

    // Update the amount displayed in a data panel
    private void updatePanel(JPanel panel, String title, double amount) {
        JLabel label = (JLabel) panel.getComponent(1);
        label.setText(String.format("$%.2f", amount));
    }

    // Custom header renderer with gradient background
    private static class GradientHeaderRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel headerLabel = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            headerLabel.setBackground(new Color(41, 128, 185));
            headerLabel.setForeground(Color.WHITE);
            headerLabel.setFont(new Font("Arial", Font.BOLD, 15));
            headerLabel.setBorder(new LineBorder(new Color(41, 128, 185)));
            return headerLabel;
        }
    }

    // Custom scroll bar UI
    private static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(127, 140, 141);
            trackColor = new Color(236, 240, 241);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }

    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Expensetracker::new);
    }

    // Variables for form dragging functionality
    private boolean isDragging = false;
    private Point mouseOffset;
}
