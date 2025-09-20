import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Game extends UnicastRemoteObject implements InterfaceGame {
    private ConcurrentHashMap<String, InterfacePlayer> connected = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> scoreofplayer = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Boolean> winplayer = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> playerLevel = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> gamePartners = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Boolean> inGameStatus = new ConcurrentHashMap<>();
    private Map<String, String> playerGuesses = new HashMap<>();
    private Map<String, Integer> guessResults = new HashMap<>();
    private Map<String, String> playerSecrets = new HashMap<>();
    private Map<String, Boolean> roundCompleted = new HashMap<>();
    private Map<String, Boolean> readyForNextRound = new HashMap<>();
    private Map<String, Boolean> waitingForNextRound = new HashMap<>();
    protected Game() throws RemoteException {
    }

    @Override
    public boolean accept(String name) throws RemoteException {
        return !connected.containsKey(name);
    }

    @Override
    public void connect(String name, InterfacePlayer me) throws RemoteException {
        connected.put(name, me);
        scoreofplayer.put(name, 0);
        winplayer.put(name, false);
        playerLevel.put(name, 1);
        inGameStatus.put(name, false);
        readyForNextRound.put(name, false);
    }

    @Override
    public void disconnect(String name) throws RemoteException {
        connected.remove(name);
        scoreofplayer.remove(name);
        winplayer.remove(name);
        playerLevel.remove(name);
        gamePartners.remove(name);
        inGameStatus.remove(name);
        readyForNextRound.remove(name);
        playerSecrets.remove(name);
        roundCompleted.remove(name);
    }

    @Override
    public List<String> listofplayeron() throws RemoteException {
        List<String> onlinePlayers = new ArrayList<>();
        for (String player : connected.keySet()) {
            if (!inGameStatus.getOrDefault(player, false)) {
                onlinePlayers.add(player);
            }
        }
        return onlinePlayers;
    }

    @Override
    public boolean findrequest(String name) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        if (player == null) return false;
        return player.requestfind();
    }

    @Override
    public List<String> NameOfRequestPlayer(String name) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        if (player == null) return new ArrayList<>();
        return player.listofrequeat();
    }

    @Override
    public void AllIsNot(String name) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        if (player != null) {
            player.SetReponse("0");
        }
    }

    @Override
    public void NameOfRequestAccept(String name, String requester) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        if (player != null) {
            player.SetReponse(requester);
        }
    }

    @Override
    public void startgamewith(String name, String requester) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        InterfacePlayer requesterPlayer = connected.get(requester);

        if (player != null && requesterPlayer != null) {
            // Clear any previous game state
            player.setMyPlayer(requester);
            requesterPlayer.setMyPlayer(name);

            // Reset game state for both players
            resetGameState(name, requester);

            // Clear any previous keys
            player.clearKey();
            requesterPlayer.clearKey();

            // Set both players as ready for the new game
            player.setReadyForNext(false);
            requesterPlayer.setReadyForNext(false);

            // Update game partners
            gamePartners.put(name, requester);
            gamePartners.put(requester, name);

            // Set players in game status to true
            inGameStatus.put(name, true);
            inGameStatus.put(requester, true);

            // Clear any previous secrets and round status
            playerSecrets.remove(name);
            playerSecrets.remove(requester);
            roundCompleted.put(name, false);
            roundCompleted.put(requester, false);
            readyForNextRound.put(name, false);
            readyForNextRound.put(requester, false);

            // Notify both players that the game has started
            player.deliverMessage("GAME_START:" + requester);
            requesterPlayer.deliverMessage("GAME_START:" + name);
        }
    }

    private void resetGameState(String player1, String player2) {
        playerLevel.put(player1, 1);
        playerLevel.put(player2, 1);
        winplayer.put(player1, false);
        winplayer.put(player2, false);
        scoreofplayer.put(player1, 0);
        scoreofplayer.put(player2, 0);
    }

    @Override
    public void SendRequestTo(String name, String target) throws RemoteException {
        InterfacePlayer targetPlayer = connected.get(target);
        if (targetPlayer != null) {
            targetPlayer.NewRequest(name);
        }
    }

    @Override
    public boolean FindReponse(String name, String target) throws RemoteException {
        InterfacePlayer targetPlayer = connected.get(target);
        if (targetPlayer == null) return false;
        return targetPlayer.ReponseFindFromUser(name);
    }

    @Override
    public Boolean GetReponse(String name, String target) throws RemoteException {
        InterfacePlayer targetPlayer = connected.get(target);
        if (targetPlayer == null) return false;
        return targetPlayer.Reponse(name);
    }

    @Override
    public int getlevel(String name) throws RemoteException {
        return playerLevel.get(name);
    }

    @Override
    public int getPlayerLevel(String name) throws RemoteException {
        return playerLevel.getOrDefault(name, 1);
    }

    @Override
    public boolean start(String name) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        if (player == null) return false;

        String playerName = player.giveplayer();
        InterfacePlayer otherPlayer = connected.get(playerName);
        if (otherPlayer == null) return false;

        return player.cleisfind() && otherPlayer.cleisfind();
    }

    @Override
    public void sendkeyToOtherPlayer(String name, String pkey) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        if (player != null) {
            player.SetCle(pkey);
            playerSecrets.put(name, pkey);

            System.out.println("Player " + name + " set secret: " + pkey);
        }
    }

    @Override
    public String givemycle(String name) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        if (player == null) return "";
        return player.getmycle();
    }

    @Override
    public int count(String n, String mycle) throws RemoteException {
        if (n == null || n.length() != 4 || mycle == null || mycle.length() != 4) {
            return -1;
        }

        for (char c : n.toCharArray()) {
            if (!Character.isDigit(c)) return -1;
        }

        int c = 0;
        for (int i = 0; i < 4; i++) {
            if (n.charAt(i) == mycle.charAt(i)) {
                c++;
            }
        }
        return c;
    }

    @Override

    public void win(String name) throws RemoteException {
        Integer currentScore = scoreofplayer.get(name);
        if (currentScore == null) currentScore = 0;

        scoreofplayer.put(name, currentScore + 1);
        winplayer.put(name, true);

        roundCompleted.put(name, true);

        waitingForNextRound.put(name, true);

        String opponentName = gamePartners.get(name);
        InterfacePlayer opponentPlayer = connected.get(opponentName);

        if (opponentPlayer != null) {

            opponentPlayer.deliverMessage("ROUND_END:OPPONENT_WON:" + name);
            System.out.println("Player " + name + " won the round. Notifying opponent " + opponentName);
        }


        Integer winnerScore = scoreofplayer.get(name);
        if (winnerScore >= 2) {

            InterfacePlayer winnerPlayer = connected.get(name);
            if (winnerPlayer != null) {
                winnerPlayer.deliverMessage("MATCH_WON:" + name);
            }
            if (opponentPlayer != null) {
                opponentPlayer.deliverMessage("MATCH_WON:" + name);
            }
        }
    }


    @Override
    public String score(String name) throws RemoteException {
        Integer score = scoreofplayer.get(name);
        if (score == null) score = 0;
        return score >= 2 ? "win" : "no";
    }

    @Override
    public void wintotal(String name) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        if (player == null) return;

        String otherPlayerName = player.giveplayer();
        Integer myScore = scoreofplayer.get(name);
        Integer otherScore = scoreofplayer.get(otherPlayerName);

        if (myScore == null) myScore = 0;
        if (otherScore == null) otherScore = 0;

        scoreofplayer.put(name, 0);
        scoreofplayer.put(otherPlayerName, 0);
        playerLevel.put(name, 1);
        playerLevel.put(otherPlayerName, 1);
        roundCompleted.put(name, false);
        roundCompleted.put(otherPlayerName, false);
        readyForNextRound.put(name, false);
        readyForNextRound.put(otherPlayerName, false);

        inGameStatus.put(name, false);
        inGameStatus.put(otherPlayerName, false);
    }

    @Override
    public boolean OtherPlayerIsWin(String name) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        if (player == null) return false;

        String player2 = player.giveplayer();
        if (player2 == null || player2.isEmpty()) return false;

        Boolean hasWon = winplayer.get(player2);
        return hasWon != null && hasWon;
    }

    @Override
    public void nextLevel() throws RemoteException {
        // This will be handled per player pair
    }

    @Override
    public void nextLevelForPlayer(String name) throws RemoteException {
        Integer currentLevel = playerLevel.get(name);
        if (currentLevel == null) currentLevel = 1;

        playerLevel.put(name, currentLevel + 1);
        winplayer.put(name, false);

        String partner = gamePartners.get(name);
        if (partner != null) {
            playerLevel.put(partner, currentLevel + 1);
            winplayer.put(partner, false);
        }

        InterfacePlayer player = connected.get(name);
        InterfacePlayer partnerPlayer = connected.get(partner);

        if (player != null) player.clearKey();
        if (partnerPlayer != null) partnerPlayer.clearKey();

        playerSecrets.remove(name);
        playerSecrets.remove(partner);

        roundCompleted.put(name, false);
        roundCompleted.put(partner, false);
        waitingForNextRound.put(name, false);
        waitingForNextRound.put(partner, false);
    }

    @Override
    public boolean isReadyForNextLevel(String name) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        if (player == null) return false;

        String otherPlayerName = player.giveplayer();
        InterfacePlayer otherPlayer = connected.get(otherPlayerName);
        if (otherPlayer == null) return false;

        return player.isReadyForNext() && otherPlayer.isReadyForNext();
    }

    @Override
    public void setReadyForNext(String name) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        if (player != null) {
            player.setReadyForNext(true);
        }

        readyForNextRound.put(name, true);

        String opponent = gamePartners.get(name);
        if (opponent != null && readyForNextRound.getOrDefault(opponent, false)) {

            InterfacePlayer playerObj = connected.get(name);
            InterfacePlayer opponentObj = connected.get(opponent);

            if (playerObj != null) playerObj.deliverMessage("READY_FOR_NEXT_ROUND");
            if (opponentObj != null) opponentObj.deliverMessage("READY_FOR_NEXT_ROUND");
        }
    }

    @Override
    public int getMyScore(String name) throws RemoteException {
        return scoreofplayer.getOrDefault(name, 0);
    }

    @Override
    public int getOpponentScore(String name) throws RemoteException {
        InterfacePlayer player = connected.get(name);
        if (player == null) return 0;

        String opponent = player.giveplayer();
        return scoreofplayer.getOrDefault(opponent, 0);
    }

    @Override
    public void setPlayerInGame(String name, boolean inGame) throws RemoteException {
        inGameStatus.put(name, inGame);
    }

    @Override
    public boolean isPlayerInGame(String name) throws RemoteException {
        return inGameStatus.getOrDefault(name, false);
    }

    @Override
    public void notifyOpponentOfExit(String exitingPlayerName, String opponentName) throws RemoteException {
        InterfacePlayer opponentPlayer = connected.get(opponentName);
        if (opponentPlayer != null) {
            opponentPlayer.deliverMessage("OPPONENT_EXITED:" + exitingPlayerName);
            inGameStatus.put(opponentName, false);
        }
        inGameStatus.put(exitingPlayerName, false);
    }

    @Override
    public void sendGuessToOpponent(String username, String guess) throws RemoteException {
        try {
            playerGuesses.put(username, guess);

            String opponent = gamePartners.get(username);

            if (opponent == null) {
                System.err.println("No opponent found for " + username);
                guessResults.put(username, 0);
                return;
            }

            String opponentSecret = playerSecrets.get(opponent);

            if (opponentSecret == null || opponentSecret.isEmpty()) {
                System.err.println("No secret found for opponent " + opponent);
                guessResults.put(username, 0);
                return;
            }

            int correctDigits = count(guess, opponentSecret);
            guessResults.put(username, correctDigits);

            System.out.println("Player " + username + " guessed: " + guess +
                    " against " + opponent + "'s secret: " + opponentSecret +
                    " - Correct digits: " + correctDigits);

        } catch (Exception e) {
            System.err.println("Error in sendGuessToOpponent: " + e.getMessage());
            e.printStackTrace();
            guessResults.put(username, 0);
        }
    }

    @Override
    public String getGuessResult(String username) throws RemoteException {
        try {
            Integer result = guessResults.get(username);
            if (result == null) {
                return "0";
            }

            guessResults.remove(username);
            return result.toString();
        } catch (Exception e) {
            System.err.println("Error in getGuessResult: " + e.getMessage());
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    public String checkMatchStatus(String playerName) throws RemoteException {
        try {
            InterfacePlayer player = connected.get(playerName);
            if (player == null) return "ERROR:PLAYER_NOT_FOUND";

            String opponent = player.giveplayer();
            if (opponent == null || opponent.isEmpty()) return "ERROR:NO_OPPONENT";

            Integer myScore = scoreofplayer.get(playerName);
            Integer opponentScore = scoreofplayer.get(opponent);

            if (myScore == null) myScore = 0;
            if (opponentScore == null) opponentScore = 0;

            if (myScore >= 2) {
                return "MATCH_WON:" + playerName;
            } else if (opponentScore >= 2) {
                return "MATCH_WON:" + opponent;
            }

            Boolean playerWaiting = waitingForNextRound.get(playerName);
            Boolean opponentWaiting = waitingForNextRound.get(opponent);

            Boolean playerCompleted = roundCompleted.get(playerName);
            Boolean opponentCompleted = roundCompleted.get(opponent);

            if ((playerWaiting != null && playerWaiting) &&
                    (opponentCompleted == null || !opponentCompleted)) {
                return "MATCH_ONGOING:" + myScore + ":" + opponentScore;
            }

            if ((opponentWaiting != null && opponentWaiting) &&
                    (playerCompleted == null || !playerCompleted)) {
                return "MATCH_ONGOING:" + myScore + ":" + opponentScore;
            }

            if (playerCompleted != null && playerCompleted &&
                    opponentCompleted != null && opponentCompleted) {

                roundCompleted.put(playerName, false);
                roundCompleted.put(opponent, false);
                waitingForNextRound.put(playerName, false);
                waitingForNextRound.put(opponent, false);

                return "READY_FOR_NEXT_ROUND";
            }

            return "MATCH_ONGOING:" + myScore + ":" + opponentScore;

        } catch (Exception e) {
            System.err.println("Error in checkMatchStatus: " + e.getMessage());
            return "ERROR:UNKNOWN";
        }
    }
    @Override
    public void playerLostRound(String loserName) throws RemoteException {
        roundCompleted.put(loserName, true);

        String opponent = gamePartners.get(loserName);

        Boolean opponentCompleted = roundCompleted.get(opponent);
        if (opponentCompleted != null && opponentCompleted) {

            InterfacePlayer loserPlayer = connected.get(loserName);
            InterfacePlayer opponentPlayer = connected.get(opponent);

            if (loserPlayer != null) loserPlayer.deliverMessage("READY_FOR_NEXT_ROUND");
            if (opponentPlayer != null) opponentPlayer.deliverMessage("READY_FOR_NEXT_ROUND");

            roundCompleted.put(loserName, false);
            roundCompleted.put(opponent, false);
            waitingForNextRound.put(loserName, false);
            waitingForNextRound.put(opponent, false);
        }
    }


    @Override
    public String getOpponentSecret(String username) throws RemoteException {
        try {
            String opponentName = gamePartners.get(username);
            if (opponentName == null) {
                return null;
            }
            return playerSecrets.get(opponentName);
        } catch (Exception e) {
            System.err.println("Error getting opponent secret for " + username + ": " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            Naming.rebind("rmi://127.0.0.1:2000/chat", new Game());
            System.out.println("Game server is running...");
        } catch (RemoteException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}