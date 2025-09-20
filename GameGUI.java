import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class GameGUI extends JFrame {
    private InterfaceGame server;
    private InterfacePlayer player;
    private String username;
    private String opponent;

    // Score display
    private JLabel myScoreLabel;
    private JLabel opponentScoreLabel;
    private JLabel roundLabel;
    private JLabel statusLabel;

    // Secret code setting
    private JButton[] secretButtons = new JButton[4];
    private JButton setSecretButton;
    private JPanel secretPanel;

    // Guessing
    private JButton[] guessButtons = new JButton[4];
    private JButton submitGuessButton;
    private JPanel guessPanel;
    private JLabel resultLabel;
    private JTextArea guessHistoryArea;
    private JScrollPane guessHistoryScrollPane;

    // Game state
    private boolean secretSet = false;
    private boolean gameStarted = false;
    private int currentRound = 1;
    private int myScore = 0;
    private int opponentScore = 0;
    private int guessNumber = 1;
    private boolean gameEnded = false;
    private boolean waitingForOpponent = false;

    // Colors for buttons (0-9)
    private Color[] digitColors = {
            new Color(255, 99, 99),   // 0 - Light Red
            new Color(99, 255, 99),   // 1 - Light Green
            new Color(99, 99, 255),   // 2 - Light Blue
            new Color(255, 255, 99),  // 3 - Light Yellow
            new Color(255, 99, 255),  // 4 - Light Magenta
            new Color(99, 255, 255),  // 5 - Light Cyan
            new Color(255, 165, 0),   // 6 - Orange
            new Color(147, 112, 219), // 7 - Medium Slate Blue
            new Color(255, 192, 203), // 8 - Pink
            new Color(144, 238, 144)  // 9 - Light Green
    };

    private Timer gameCheckTimer;
    private Timer messageCheckTimer;

    public GameGUI(InterfaceGame server, InterfacePlayer player, String username, String opponent) {
        this.server = server;
        this.player = player;
        this.username = username;
        this.opponent = opponent;

        initializeComponents();
        setupLayout();
        startGameLoop();

        // Add window listener for graceful exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleGameExit();
            }
        });
    }

    private void initializeComponents() {
        setTitle("لعبة التخمين - " + username + " ضد " + opponent);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(700, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        Font arabicFont = new Font("Arial Unicode MS", Font.PLAIN, 14);
        Font arabicBoldFont = new Font("Arial Unicode MS", Font.BOLD, 16);

        // Score labels
        myScoreLabel = new JLabel(username + ": 0", JLabel.CENTER);
        myScoreLabel.setFont(arabicBoldFont);
        myScoreLabel.setForeground(new Color(0, 100, 0));

        opponentScoreLabel = new JLabel(opponent + ": 0", JLabel.CENTER);
        opponentScoreLabel.setFont(arabicBoldFont);
        opponentScoreLabel.setForeground(new Color(100, 0, 0));

        roundLabel = new JLabel("الجولة: 1", JLabel.CENTER);
        roundLabel.setFont(arabicBoldFont);
        roundLabel.setForeground(new Color(70, 130, 180));

        statusLabel = new JLabel("اختر كلمة السر المكونة من 4 أرقام", JLabel.CENTER);
        statusLabel.setFont(arabicFont);

        // Secret code buttons
        for (int i = 0; i < 4; i++) {
            secretButtons[i] = new JButton("?");
            secretButtons[i].setFont(new Font("Arial", Font.BOLD, 20));
            secretButtons[i].setPreferredSize(new Dimension(60, 60));
            secretButtons[i].setBackground(Color.LIGHT_GRAY);
            secretButtons[i].setFocusPainted(false);
            secretButtons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));

            final int index = i;
            secretButtons[i].addActionListener(e -> selectDigitForSecret(index));
        }

        setSecretButton = new JButton("تأكيد كلمة السر");
        setSecretButton.setFont(arabicBoldFont);
        setSecretButton.setBackground(new Color(70, 130, 180));
        setSecretButton.setForeground(Color.WHITE);
        setSecretButton.setFocusPainted(false);
        setSecretButton.setEnabled(false);
        setSecretButton.addActionListener(new SetSecretActionListener());

        // Guess buttons
        for (int i = 0; i < 4; i++) {
            guessButtons[i] = new JButton("?");
            guessButtons[i].setFont(new Font("Arial", Font.BOLD, 20));
            guessButtons[i].setPreferredSize(new Dimension(60, 60));
            guessButtons[i].setBackground(Color.LIGHT_GRAY);
            guessButtons[i].setFocusPainted(false);
            guessButtons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            guessButtons[i].setEnabled(false);

            final int index = i;
            guessButtons[i].addActionListener(e -> selectDigitForGuess(index));
        }

        submitGuessButton = new JButton("إرسال التخمين");
        submitGuessButton.setFont(arabicBoldFont);
        submitGuessButton.setBackground(new Color(34, 139, 34));
        submitGuessButton.setForeground(Color.WHITE);
        submitGuessButton.setFocusPainted(false);
        submitGuessButton.setEnabled(false);
        submitGuessButton.addActionListener(new SubmitGuessActionListener());

        resultLabel = new JLabel("", JLabel.CENTER);
        resultLabel.setFont(arabicBoldFont);

        // Guess History Area
        guessHistoryArea = new JTextArea(5, 20);
        guessHistoryArea.setEditable(false);
        guessHistoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        guessHistoryArea.setLineWrap(true);
        guessHistoryArea.setWrapStyleWord(true);
        guessHistoryScrollPane = new JScrollPane(guessHistoryArea);
        guessHistoryScrollPane.setBorder(BorderFactory.createTitledBorder("سجل التخمينات"));

        // Initialize history with header
        guessHistoryArea.setText("الجولة " + currentRound + " - تخمين كلمة سر " + opponent + ":\n" +
                "-----------------------------------\n");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title and score panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(70, 130, 180));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("🎮 لعبة التخمين 🎮", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial Unicode MS", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JPanel scorePanel = new JPanel(new GridLayout(1, 3, 20, 0));
        scorePanel.setOpaque(false);
        scorePanel.add(myScoreLabel);
        scorePanel.add(roundLabel);
        scorePanel.add(opponentScoreLabel);

        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(scorePanel, BorderLayout.SOUTH);

        // Main game panel
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Secret code panel
        secretPanel = new JPanel(new BorderLayout());
        TitledBorder secretBorder = BorderFactory.createTitledBorder("كلمة السر الخاصة بك");
        secretBorder.setTitleFont(new Font("Arial Unicode MS", Font.BOLD, 14));
        secretPanel.setBorder(secretBorder);

        JPanel secretButtonsPanel = new JPanel(new FlowLayout());
        for (JButton button : secretButtons) {
            secretButtonsPanel.add(button);
        }

        JPanel secretControlPanel = new JPanel(new FlowLayout());
        secretControlPanel.add(setSecretButton);

        secretPanel.add(secretButtonsPanel, BorderLayout.CENTER);
        secretPanel.add(secretControlPanel, BorderLayout.SOUTH);

        // Guess panel
        guessPanel = new JPanel(new BorderLayout());
        TitledBorder guessBorder = BorderFactory.createTitledBorder("تخمين كلمة سر الخصم");
        guessBorder.setTitleFont(new Font("Arial Unicode MS", Font.BOLD, 14));
        guessPanel.setBorder(guessBorder);

        JPanel guessButtonsPanel = new JPanel(new FlowLayout());
        for (JButton button : guessButtons) {
            guessButtonsPanel.add(button);
        }

        JPanel guessControlPanel = new JPanel(new FlowLayout());
        guessControlPanel.add(submitGuessButton);
        guessControlPanel.add(resultLabel);

        guessPanel.add(guessButtonsPanel, BorderLayout.CENTER);
        guessPanel.add(guessControlPanel, BorderLayout.SOUTH);

        mainPanel.add(secretPanel);
        mainPanel.add(guessPanel);

        // Status and History panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(guessHistoryScrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void selectDigitForSecret(int position) {
        if (secretSet || gameEnded) return;

        String[] options = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String selected = (String) JOptionPane.showInputDialog(
                this,
                "اختر الرقم للموضع " + (position + 1),
                "اختيار الرقم",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                "0"
        );

        if (selected != null) {
            int digit = Integer.parseInt(selected);
            secretButtons[position].setText(selected);
            secretButtons[position].setBackground(digitColors[digit]);
            secretButtons[position].setForeground(Color.BLACK);

            checkSecretComplete();
        }
    }

    private void selectDigitForGuess(int position) {
        if (!gameStarted || gameEnded || waitingForOpponent) return;

        String[] options = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String selected = (String) JOptionPane.showInputDialog(
                this,
                "اختر الرقم للموضع " + (position + 1),
                "تخمين الرقم",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                "0"
        );

        if (selected != null) {
            int digit = Integer.parseInt(selected);
            guessButtons[position].setText(selected);
            guessButtons[position].setBackground(digitColors[digit]);
            guessButtons[position].setForeground(Color.BLACK);

            checkGuessComplete();
        }
    }

    private void checkSecretComplete() {
        boolean complete = true;
        for (JButton button : secretButtons) {
            if (button.getText().equals("?")) {
                complete = false;
                break;
            }
        }
        setSecretButton.setEnabled(complete && !secretSet);
    }

    private void checkGuessComplete() {
        boolean complete = true;
        for (JButton button : guessButtons) {
            if (button.getText().equals("?")) {
                complete = false;
                break;
            }
        }
        submitGuessButton.setEnabled(complete && gameStarted && !waitingForOpponent);
    }

    private boolean validateGuess(String guess) {
        // Check if guess is exactly 4 digits
        if (guess.length() != 4) {
            return false;
        }

        // Check if all characters are digits
        for (char c : guess.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    private class SetSecretActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            StringBuilder secret = new StringBuilder();
            for (JButton button : secretButtons) {
                secret.append(button.getText());
            }

            String secretCode = secret.toString();

            // Validate secret code
            if (!validateGuess(secretCode)) {
                JOptionPane.showMessageDialog(
                        GameGUI.this,
                        "كلمة السر يجب أن تكون 4 أرقام فقط",
                        "خطأ",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            setSecretButton.setEnabled(false);
            for (JButton button : secretButtons) {
                button.setEnabled(false);
            }

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Send secret code to server
                    // This should store the secret for THIS player (username)
                    server.sendkeyToOtherPlayer(username, secretCode);
                    return null;
                }

                @Override
                protected void done() {
                    secretSet = true;
                    statusLabel.setText("تم تعيين كلمة السر - انتظار الخصم...");
                    statusLabel.setForeground(new Color(0, 150, 0));

                    // Start checking if game can begin
                    checkGameStart();
                }
            };
            worker.execute();
        }
    }

    private void checkGameStart() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Wait until both players have set their secrets
                while (!server.start(username)) {
                    Thread.sleep(500);
                }
                return true;
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        gameStarted = true;
                        statusLabel.setText("بدأت اللعبة! ابدأ بتخمين كلمة سر الخصم");
                        statusLabel.setForeground(new Color(70, 130, 180));

                        for (JButton button : guessButtons) {
                            button.setEnabled(true);
                        }

                        startGameCheckTimer();
                        startMessageCheckTimer();
                    }
                } catch (Exception ex) {
                    statusLabel.setText("خطأ في بدء اللعبة: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    private void startGameLoop() {
        // Initialize game state
        gameStarted = false;
        secretSet = false;
        gameEnded = false;
        currentRound = 1;
        myScore = 0;
        opponentScore = 0;
        guessNumber = 1;
        waitingForOpponent = false;

        // Update UI
        updateScores();
    }

    private void handleGameExit() {
        if (gameCheckTimer != null) {
            gameCheckTimer.stop();
        }
        if (messageCheckTimer != null) {
            messageCheckTimer.stop();
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "هل أنت متأكد من أنك تريد الخروج؟",
                "تأكيد الخروج",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                // Notify server and opponent about exit
                server.notifyOpponentOfExit(username, opponent);
                server.disconnect(username);
                dispose();
                System.exit(0);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "خطأ في قطع الاتصال بالخادم: " + ex.getMessage(),
                        "خطأ",
                        JOptionPane.ERROR_MESSAGE);
                dispose();
                System.exit(0);
            }
        }
    }

    private void startGameCheckTimer() {
        gameCheckTimer = new Timer(2000, e -> {
            if (gameEnded) return;

            // فقط تحديث النتائج بشكل دوري
            updateScores();
        });
        gameCheckTimer.start();
    }

    private void startMessageCheckTimer() {
        messageCheckTimer = new Timer(1000, e -> {
            if (gameEnded) return;

            try {
                String message = player.getLastMessage();
                if (message != null && !message.isEmpty()) {
                    if (message.startsWith("OPPONENT_EXITED:")) {
                        String opponentName = message.substring("OPPONENT_EXITED:".length());
                        gameEnded = true;

                        if (gameCheckTimer != null) gameCheckTimer.stop();
                        if (messageCheckTimer != null) messageCheckTimer.stop();

                        JOptionPane.showMessageDialog(
                                GameGUI.this,
                                "الخصم " + opponentName + " خرج من اللعبة. فزت بالافتراض!",
                                "لاعب خرج",
                                JOptionPane.INFORMATION_MESSAGE);

                        returnToLobby();
                    } else if (message.startsWith("ROUND_END:OPPONENT_WON:")) {
                        // الخصم فاز بالجولة
                        String[] parts = message.split(":");
                        if (parts.length >= 3) {
                            String winnerName = parts[2];

                            // تحديث النتائج
                            updateScores();

                            // إضافة رسالة في سجل التخمينات
                            guessHistoryArea.append("*** " + winnerName + " فاز بالجولة! ***\n");

                            // تعطيل أزرار التخمين
                            for (JButton button : guessButtons) {
                                button.setEnabled(false);
                            }
                            submitGuessButton.setEnabled(false);

                            // تحديث الحالة
                            statusLabel.setText("الخصم فاز بالجولة - انتظار انتقال للجولة التالية...");
                            statusLabel.setForeground(Color.RED);

                            // إبلاغ الخادم أن هذا اللاعب خسر الجولة
                            SwingWorker<Void, Void> lostWorker = new SwingWorker<Void, Void>() {
                                @Override
                                protected Void doInBackground() throws Exception {
                                    server.playerLostRound(username);
                                    return null;
                                }
                            };
                            lostWorker.execute();
                        }
                    } else if (message.startsWith("MATCH_WON:")) {
                        // انتهت المباراة
                        String winner = message.substring("MATCH_WON:".length());
                        gameEnded = true;

                        if (gameCheckTimer != null) gameCheckTimer.stop();
                        if (messageCheckTimer != null) messageCheckTimer.stop();

                        if (winner.equals(username)) {
                            endGame(true);
                        } else {
                            endGame(false);
                        }
                    } else if (message.equals("READY_FOR_NEXT_ROUND")) {
                        // كلا اللاعبين جاهز للجولة التالية
                        boolean wonLastRound = false;

                        // تحديد من فاز بالجولة السابقة
                        try {
                            int myCurrentScore = server.getMyScore(username);
                            if (myCurrentScore > myScore) {
                                wonLastRound = true;
                            }
                        } catch (Exception ex) {
                            // في حالة الخطأ، افتراض أننا لم نفز
                            wonLastRound = false;
                        }

                        nextRound(wonLastRound);
                    }
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });
        messageCheckTimer.start();
    }


    private void updateScores() {
        try {
            myScore = server.getMyScore(username);
            opponentScore = server.getOpponentScore(username);

            myScoreLabel.setText(username + ": " + myScore);
            opponentScoreLabel.setText(opponent + ": " + opponentScore);
            roundLabel.setText("الجولة: " + currentRound);
        } catch (RemoteException ex) {
            statusLabel.setText("خطأ في تحديث النتائج: " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void endGame(boolean won) {
        gameEnded = true;

        if (gameCheckTimer != null) {
            gameCheckTimer.stop();
        }
        if (messageCheckTimer != null) {
            messageCheckTimer.stop();
        }

        String message = won ? "🎉 فزت بالمباراة! 🎉" : "😔 خسرت المباراة";

        try {
            // Get opponent's secret for losing player
            if (!won) {
                String opponentSecret = server.getOpponentSecret(username);
                if (opponentSecret != null && !opponentSecret.isEmpty()) {
                    message += "\nكلمة سر الخصم كانت: " + opponentSecret;
                }
            }
        } catch (RemoteException ex) {
            // Continue without showing secret if error occurs
        }

        JOptionPane.showMessageDialog(
                this,
                message,
                "نهاية المباراة",
                JOptionPane.INFORMATION_MESSAGE);

        returnToLobby();
    }

    private void returnToLobby() {
        try {
            server.wintotal(username);
            dispose();
            // Here you should return to the lobby/player selection screen
            // You might need to create a new instance of your lobby GUI
        } catch (RemoteException ex) {
            ex.printStackTrace();
            dispose();
        }
    }

    private void nextRound(boolean wonLastRound) {
        try {
            server.nextLevelForPlayer(username);
            currentRound++;
            guessNumber = 1;
            waitingForOpponent = false;

            // Reset UI for new round
            secretSet = false;
            for (JButton button : secretButtons) {
                button.setEnabled(true);
                button.setText("?");
                button.setBackground(Color.LIGHT_GRAY);
            }
            setSecretButton.setEnabled(false);

            for (JButton button : guessButtons) {
                button.setEnabled(false);
                button.setText("?");
                button.setBackground(Color.LIGHT_GRAY);
            }
            submitGuessButton.setEnabled(false);

            //resultLabel.setText(wonLastRound ? "فزت بالجولة السابقة!" : "خسرت الجولة السابقة");
            resultLabel.setForeground(wonLastRound ? new Color(0, 150, 0) : Color.RED);

            // Clear and reinitialize guess history for new round
            guessHistoryArea.setText("الجولة " + currentRound + " - تخمين كلمة سر " + opponent + ":\n" +
                    "-----------------------------------\n");

            statusLabel.setText("اختر كلمة السر للجولة الجديدة");
            statusLabel.setForeground(new Color(70, 130, 180));

            updateScores();
        } catch (RemoteException ex) {
            statusLabel.setText("خطأ في بدء الجولة الجديدة: " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private class SubmitGuessActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (waitingForOpponent || gameEnded) return;

            StringBuilder guess = new StringBuilder();
            for (JButton button : guessButtons) {
                guess.append(button.getText());
            }
            String currentGuess = guess.toString();

            // Validate guess
            if (!validateGuess(currentGuess)) {
                JOptionPane.showMessageDialog(
                        GameGUI.this,
                        "التخمين يجب أن يكون 4 أرقام فقط",
                        "خطأ",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            submitGuessButton.setEnabled(false);
            waitingForOpponent = true;

            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    server.sendGuessToOpponent(username, currentGuess);
                    String result = server.getGuessResult(username);

                    if (result != null && result.toLowerCase().contains("error")) {
                        throw new Exception(result);
                    }

                    return result;
                }

                @Override
                protected void done() {
                    try {
                        String result = get();
                        waitingForOpponent = false;

                        int correctDigits = 0;
                        if (result != null && !result.isEmpty()) {
                            try {
                                correctDigits = Integer.parseInt(result.trim());
                            } catch (NumberFormatException ex) {
                                throw new Exception("نتيجة غير صحيحة من الخادم: " + result);
                            }
                        }

                        // Add to guess history
                        String historyEntry = "محاولة " + guessNumber + ": " + currentGuess +
                                " - صحيح: " + correctDigits + "/4\n";
                        guessHistoryArea.append(historyEntry);
                        guessHistoryArea.setCaretPosition(guessHistoryArea.getDocument().getLength());

                        // Check if guess was completely correct
                        if (correctDigits == 4) {
                            resultLabel.setText("🎉 خمنت كلمة السر! 🎉");
                            resultLabel.setForeground(new Color(0, 150, 0));

                            guessHistoryArea.append("*** فزت بالجولة! ***\n");

                            // Disable guess buttons
                            for (JButton button : guessButtons) {
                                button.setEnabled(false);
                            }

                            // Process the win
                            server.win(username);

                            // إضافة رسالة انتظار للخصم
                            statusLabel.setText("انتظار الخصم للانتقال للجولة التالية...");
                            statusLabel.setForeground(new Color(0, 150, 0));

                            // التحقق من حالة المباراة
                            checkGameProgress();

                        } else {
                            resultLabel.setText("أرقام صحيحة: " + correctDigits + "/4");
                            resultLabel.setForeground(new Color(180, 100, 0));

                            // Reset guess buttons for next attempt
                            for (JButton button : guessButtons) {
                                button.setText("?");
                                button.setBackground(Color.LIGHT_GRAY);
                                button.setEnabled(true);
                            }

                            guessNumber++;
                            submitGuessButton.setEnabled(false);
                        }

                    } catch (Exception ex) {
                        waitingForOpponent = false;
                        statusLabel.setText("خطأ في معالجة التخمين: " + ex.getMessage());
                        statusLabel.setForeground(Color.RED);

                        for (JButton button : guessButtons) {
                            button.setEnabled(true);
                        }
                        checkGuessComplete();
                    }
                }
            };
            worker.execute();
        }
    }
    private void checkGameProgress() {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // التحقق من حالة المباراة
                return server.checkMatchStatus(username);
            }

            @Override
            protected void done() {
                try {
                    String matchStatus = get();

                    if (matchStatus.startsWith("MATCH_WON:")) {
                        String winner = matchStatus.substring("MATCH_WON:".length());
                        gameEnded = true;

                        if (gameCheckTimer != null) gameCheckTimer.stop();
                        if (messageCheckTimer != null) messageCheckTimer.stop();

                        if (winner.equals(username)) {
                            endGame(true); // فزنا بالمباراة
                        } else {
                            endGame(false); // خسرنا المباراة
                        }
                    } else if (matchStatus.startsWith("MATCH_ONGOING:")) {
                        // المباراة مستمرة، تحديث النتائج وانتظار
                        updateScores();
                        statusLabel.setText("انتظار الخصم لإنهاء الجولة...");
                        statusLabel.setForeground(new Color(0, 150, 150));
                    } else if (matchStatus.equals("READY_FOR_NEXT_ROUND")) {
                        // الانتقال للجولة التالية
                        nextRound(true); // فزنا بالجولة السابقة
                    }

                } catch (Exception ex) {
                    statusLabel.setText("خطأ في التحقق من حالة المباراة: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }


    private void waitForOpponentToFinishRound() {
        // انتظار الخصم لإنهاء الجولة
        Timer waitTimer = new Timer(1000, null);
        waitTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String matchStatus = server.checkMatchStatus(username);

                    if (matchStatus.startsWith("MATCH_WON:")) {
                        waitTimer.stop();
                        String winner = matchStatus.substring("MATCH_WON:".length());
                        if (winner.equals(username)) {
                            endGame(true);
                        } else {
                            endGame(false);
                        }
                    } else if (matchStatus.equals("READY_FOR_NEXT_ROUND")) {
                        waitTimer.stop();
                        updateScores();
                        nextRound(true); // فزنا بالجولة السابقة
                    }

                } catch (RemoteException ex) {
                    waitTimer.stop();
                    statusLabel.setText("خطأ في الاتصال: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        });
        waitTimer.start();
    }

}