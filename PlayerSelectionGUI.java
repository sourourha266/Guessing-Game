import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.List;

public class PlayerSelectionGUI extends JFrame {
    private InterfaceGame server;
    private InterfacePlayer player;
    private String username;

    private JList<String> onlinePlayersList;
    private DefaultListModel<String> playersListModel;
    private JList<String> requestsList;
    private DefaultListModel<String> requestsListModel;

    private JButton challengeButton;
    private JButton acceptButton;
    private JButton rejectButton;
    private JButton refreshButton;
    private JButton quitButton;

    private JLabel statusLabel;
    private Timer refreshTimer;
    private Timer requestCheckTimer;

    public PlayerSelectionGUI(InterfaceGame server, InterfacePlayer player, String username) {
        this.server = server;
        this.player = player;
        this.username = username;

        initializeComponents();
        setupLayout();
        startTimers();
        refreshPlayersList();

        // Add window listener for graceful exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnectAndExit();
            }
        });
    }

    private void initializeComponents() {
        setTitle("لعبة التخمين - اختيار اللاعب");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Handle closing manually
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        // Set Arabic font
        Font arabicFont = new Font("Arial Unicode MS", Font.PLAIN, 14);
        Font arabicBoldFont = new Font("Arial Unicode MS", Font.BOLD, 14);

        // Initialize list models
        playersListModel = new DefaultListModel<>();
        requestsListModel = new DefaultListModel<>();

        // Create lists
        onlinePlayersList = new JList<>(playersListModel);
        onlinePlayersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        onlinePlayersList.setFont(arabicFont);
        onlinePlayersList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        requestsList = new JList<>(requestsListModel);
        requestsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestsList.setFont(arabicFont);
        requestsList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create buttons
        challengeButton = new JButton("تحدي اللاعب");
        acceptButton = new JButton("قبول الطلب");
        rejectButton = new JButton("رفض الطلب");
        refreshButton = new JButton("تحديث");
        quitButton = new JButton("خروج");

        // Set button fonts and styles
        JButton[] buttons = {challengeButton, acceptButton, rejectButton, refreshButton, quitButton};
        for (JButton button : buttons) {
            button.setFont(arabicBoldFont);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        // Style buttons
        challengeButton.setBackground(new Color(70, 130, 180));
        challengeButton.setForeground(Color.WHITE);

        acceptButton.setBackground(new Color(34, 139, 34));
        acceptButton.setForeground(Color.WHITE);

        rejectButton.setBackground(new Color(220, 20, 60));
        rejectButton.setForeground(Color.WHITE);

        refreshButton.setBackground(new Color(255, 165, 0));
        refreshButton.setForeground(Color.WHITE);

        quitButton.setBackground(new Color(128, 128, 128));
        quitButton.setForeground(Color.WHITE);

        // Status label
        statusLabel = new JLabel("مرحباً " + username + " - اختر لاعباً للتحدي أو انتظر طلبات التحدي", JLabel.CENTER);
        statusLabel.setFont(arabicFont);
        statusLabel.setForeground(new Color(70, 130, 180));

        // Add action listeners
        challengeButton.addActionListener(new ChallengeActionListener());
        acceptButton.addActionListener(new AcceptActionListener());
        rejectButton.addActionListener(new RejectActionListener());
        refreshButton.addActionListener(e -> refreshPlayersList());
        quitButton.addActionListener(e -> disconnectAndExit());

        // Initially disable buttons
        challengeButton.setEnabled(false);
        acceptButton.setEnabled(false);
        rejectButton.setEnabled(false);

        // Add list selection listeners
        onlinePlayersList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                challengeButton.setEnabled(onlinePlayersList.getSelectedValue() != null);
            }
        });

        requestsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = requestsList.getSelectedValue() != null;
                acceptButton.setEnabled(hasSelection);
                rejectButton.setEnabled(hasSelection);
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("🎮 صالة اللعب 🎮", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial Unicode MS", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        titlePanel.add(titleLabel);

        // Main panel
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        // Online players panel
        JPanel playersPanel = new JPanel(new BorderLayout());
        TitledBorder playersBorder = BorderFactory.createTitledBorder("اللاعبون المتصلون");
        playersBorder.setTitleFont(new Font("Arial Unicode MS", Font.BOLD, 14));
        playersPanel.setBorder(playersBorder);

        JScrollPane playersScrollPane = new JScrollPane(onlinePlayersList);
        playersScrollPane.setPreferredSize(new Dimension(250, 200));
        playersPanel.add(playersScrollPane, BorderLayout.CENTER);

        JPanel playersButtonPanel = new JPanel(new FlowLayout());
        playersButtonPanel.add(challengeButton);
        playersButtonPanel.add(refreshButton);
        playersPanel.add(playersButtonPanel, BorderLayout.SOUTH);

        // Requests panel
        JPanel requestsPanel = new JPanel(new BorderLayout());
        TitledBorder requestsBorder = BorderFactory.createTitledBorder("طلبات التحدي");
        requestsBorder.setTitleFont(new Font("Arial Unicode MS", Font.BOLD, 14));
        requestsPanel.setBorder(requestsBorder);

        JScrollPane requestsScrollPane = new JScrollPane(requestsList);
        requestsScrollPane.setPreferredSize(new Dimension(250, 200));
        requestsPanel.add(requestsScrollPane, BorderLayout.CENTER);

        JPanel requestsButtonPanel = new JPanel(new FlowLayout());
        requestsButtonPanel.add(acceptButton);
        requestsButtonPanel.add(rejectButton);
        requestsPanel.add(requestsButtonPanel, BorderLayout.SOUTH);

        mainPanel.add(playersPanel);
        mainPanel.add(requestsPanel);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        JPanel quitPanel = new JPanel(new FlowLayout());
        quitPanel.add(quitButton);
        bottomPanel.add(quitPanel, BorderLayout.SOUTH);

        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void startTimers() {
        // Refresh players list every 3 seconds
        refreshTimer = new Timer(3000, e -> refreshPlayersList());
        refreshTimer.start();

        // Check for requests every 2 seconds
        requestCheckTimer = new Timer(2000, e -> checkForRequests());
        requestCheckTimer.start();
    }

    private void refreshPlayersList() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return server.listofplayeron();
            }

            @Override
            protected void done() {
                try {
                    List<String> players = get();
                    playersListModel.clear();
                    for (String player : players) {
                        if (!player.equals(username)) {
                            playersListModel.addElement(player);
                        }
                    }
                } catch (Exception e) {
                    statusLabel.setText("خطأ في تحديث قائمة اللاعبين");
                    statusLabel.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    private void checkForRequests() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (server.findrequest(username)) {
                    List<String> requests = server.NameOfRequestPlayer(username);
                    SwingUtilities.invokeLater(() -> {
                        requestsListModel.clear();
                        for (String requester : requests) {
                            requestsListModel.addElement(requester);
                        }
                    });
                }
                return null;
            }
        };
        worker.execute();
    }

    private class ChallengeActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedPlayer = onlinePlayersList.getSelectedValue();
            if (selectedPlayer == null) return;

            challengeButton.setEnabled(false);
            statusLabel.setText("إرسال طلب تحدي إلى " + selectedPlayer + "...");
            statusLabel.setForeground(new Color(100, 100, 100));

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    server.SendRequestTo(username, selectedPlayer);

                    // Wait for response
                    int timeout = 0;
                    while (!server.FindReponse(username, selectedPlayer) && timeout < 30) {
                        Thread.sleep(1000);
                        timeout++;
                    }

                    if (timeout >= 30) {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("انتهت مهلة الانتظار - لم يرد " + selectedPlayer);
                            statusLabel.setForeground(Color.RED);
                            challengeButton.setEnabled(true);
                        });
                        return null;
                    }

                    Boolean response = server.GetReponse(username, selectedPlayer);
                    SwingUtilities.invokeLater(() -> {
                        if (response != null && response) {
                            statusLabel.setText("تم قبول التحدي! بدء اللعبة...");
                            statusLabel.setForeground(new Color(0, 150, 0));

                            // Set players in game status to true
                            try {
                                server.setPlayerInGame(username, true);
                                server.setPlayerInGame(selectedPlayer, true);
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }

                            Timer timer = new Timer(2000, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    openGameGUI(selectedPlayer);
                                }
                            });
                            timer.setRepeats(false);
                            timer.start();
                        } else {
                            statusLabel.setText("تم رفض التحدي من " + selectedPlayer);
                            statusLabel.setForeground(Color.RED);
                            challengeButton.setEnabled(true);
                        }
                    });

                    return null;
                }
            };
            worker.execute();
        }
    }

    private class AcceptActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedRequester = requestsList.getSelectedValue();
            if (selectedRequester == null) return;

            acceptButton.setEnabled(false);
            rejectButton.setEnabled(false);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    server.NameOfRequestAccept(username, selectedRequester);
                    server.startgamewith(username, selectedRequester);

                    // Set players in game status to true
                    server.setPlayerInGame(username, true);
                    server.setPlayerInGame(selectedRequester, true);
                    return null;
                }

                @Override
                protected void done() {
                    statusLabel.setText("تم قبول التحدي! بدء اللعبة مع " + selectedRequester + "...");
                    statusLabel.setForeground(new Color(0, 150, 0));

                    Timer timer = new Timer(2000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            openGameGUI(selectedRequester);
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            };
            worker.execute();
        }
    }
    private void openGameGUI(String opponent) {
        SwingUtilities.invokeLater(() -> {
            new GameGUI(server, player, username, opponent).setVisible(true);
            dispose();
        });
    }
    private class RejectActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    server.AllIsNot(username);
                    return null;
                }

                @Override
                protected void done() {
                    requestsListModel.clear();
                    statusLabel.setText("تم رفض جميع الطلبات");
                    statusLabel.setForeground(new Color(100, 100, 100));
                    acceptButton.setEnabled(false);
                    rejectButton.setEnabled(false);
                }
            };
            worker.execute();
        }
    }

    private void disconnectAndExit() {
        int result = JOptionPane.showConfirmDialog(
                PlayerSelectionGUI.this,
                "هل أنت متأكد من أنك تريد الخروج؟",
                "تأكيد الخروج", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                // Notify opponent if in game
                String opponent = player.giveplayer();
                if (opponent != null && !opponent.isEmpty()) {
                    server.notifyOpponentOfExit(username, opponent);
                }

                server.disconnect(username);
                System.exit(0);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(
                        PlayerSelectionGUI.this,
                        "خطأ في قطع الاتصال بالخادم: " + ex.getMessage(),
                        "خطأ",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}