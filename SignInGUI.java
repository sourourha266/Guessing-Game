import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class SignInGUI extends JFrame {
    private JTextField usernameField;
    private JButton signInButton;
    private JLabel statusLabel;
    private InterfaceGame server;
    private InterfacePlayer player;

    public SignInGUI() {
        initializeComponents();
        setupLayout();
        connectToServer();
    }

    private void initializeComponents() {
        setTitle("لعبة التخمين - تسجيل الدخول");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create components
        usernameField = new JTextField(20);
        signInButton = new JButton("تسجيل الدخول");
        statusLabel = new JLabel("أدخل اسم المستخدم للبدء", JLabel.CENTER);

        // Set fonts for Arabic support
        Font arabicFont = new Font("Arial Unicode MS", Font.PLAIN, 14);
        usernameField.setFont(arabicFont);
        signInButton.setFont(arabicFont);
        statusLabel.setFont(arabicFont);

        // Add action listeners
        signInButton.addActionListener(new SignInActionListener());
        usernameField.addActionListener(new SignInActionListener());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("🎮 لعبة التخمين 🎮", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial Unicode MS", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        titlePanel.add(titleLabel);

        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Username label
        JLabel usernameLabel = new JLabel("اسم المستخدم:");
        usernameLabel.setFont(new Font("Arial Unicode MS", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(usernameLabel, gbc);

        // Username field
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(usernameField, gbc);

        // Sign in button
        signInButton.setBackground(new Color(70, 130, 180));
        signInButton.setForeground(Color.WHITE);
        signInButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        signInButton.setFocusPainted(false);
        signInButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(signInButton, gbc);

        // Status label
        statusLabel.setForeground(new Color(100, 100, 100));
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(statusLabel, gbc);

        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void connectToServer() {
        try {
            server = (InterfaceGame) Naming.lookup("rmi://127.0.0.1:2000/chat");
            player = new Player();
            statusLabel.setText("متصل بالخادم - جاهز للبدء");
            statusLabel.setForeground(new Color(0, 150, 0));
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            statusLabel.setText("خطأ في الاتصال بالخادم");
            statusLabel.setForeground(Color.RED);
            signInButton.setEnabled(false);
        }
    }

    private class SignInActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();

            if (username.isEmpty()) {
                statusLabel.setText("يرجى إدخال اسم المستخدم");
                statusLabel.setForeground(Color.RED);
                return;
            }

            signInButton.setEnabled(false);
            statusLabel.setText("جاري التحقق من اسم المستخدم...");
            statusLabel.setForeground(new Color(100, 100, 100));

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return server.accept(username);
                }

                @Override
                protected void done() {
                    try {
                        boolean accepted = get();
                        if (accepted) {
                            server.connect(username, player);
                            statusLabel.setText("تم تسجيل الدخول بنجاح!");
                            statusLabel.setForeground(new Color(0, 150, 0));

                            // Wait a moment then open player selection GUI
                            Timer timer = new Timer(1500, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    openPlayerSelectionGUI(username);
                                }
                            });
                            timer.setRepeats(false);
                            timer.start();
                        } else {
                            statusLabel.setText("اسم المستخدم مستخدم بالفعل - جرب اسماً آخر");
                            statusLabel.setForeground(Color.RED);
                            signInButton.setEnabled(true);
                            usernameField.selectAll();
                            usernameField.requestFocus();
                        }
                    } catch (Exception ex) {
                        statusLabel.setText("خطأ في تسجيل الدخول");
                        statusLabel.setForeground(Color.RED);
                        signInButton.setEnabled(true);
                    }
                }
            };
            worker.execute();
        }
    }

    private void openPlayerSelectionGUI(String username) {
        SwingUtilities.invokeLater(() -> {
            new PlayerSelectionGUI(server, player, username).setVisible(true);
            dispose();
        });
    }

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new SignInGUI().setVisible(true);
        });
    }
}

