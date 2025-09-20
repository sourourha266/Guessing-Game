import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

interface InterfaceGame extends Remote {
    boolean accept(String name) throws RemoteException;
    void connect(String name, InterfacePlayer me) throws RemoteException;
    void disconnect(String name) throws RemoteException;
    List<String> listofplayeron() throws RemoteException;
    boolean findrequest(String name) throws RemoteException;
    List<String> NameOfRequestPlayer(String name) throws RemoteException;
    void AllIsNot(String name) throws RemoteException;
    void NameOfRequestAccept(String name, String requester) throws RemoteException;
    void startgamewith(String name, String requester) throws RemoteException;
    void SendRequestTo(String name, String target) throws RemoteException;
    boolean FindReponse(String name, String target) throws RemoteException;
    Boolean GetReponse(String name, String target) throws RemoteException;
    int getlevel(String name) throws RemoteException;
    int getPlayerLevel(String name) throws RemoteException; // NEW
    boolean start(String name) throws RemoteException;
    void sendkeyToOtherPlayer(String name, String pkey) throws RemoteException;
    String givemycle(String name) throws RemoteException;
    int count(String n, String mycle) throws RemoteException;
    void win(String name) throws RemoteException;
    String score(String name) throws RemoteException;
    void wintotal(String name) throws RemoteException;
    boolean OtherPlayerIsWin(String name) throws RemoteException;
    void nextLevel() throws RemoteException;
    void nextLevelForPlayer(String name) throws RemoteException; // NEW
    boolean isReadyForNextLevel(String name) throws RemoteException;
    void setReadyForNext(String name) throws RemoteException;
    int getMyScore(String name) throws RemoteException; // NEW
    int getOpponentScore(String name) throws RemoteException; // NEW
    void setPlayerInGame(String name, boolean inGame) throws RemoteException; // NEW
    boolean isPlayerInGame(String name) throws RemoteException; // NEW
    void notifyOpponentOfExit(String exitingPlayerName, String opponentName) throws RemoteException; // NEW
    void sendGuessToOpponent(String username, String currentGuess)throws RemoteException;
    String getGuessResult(String username)throws RemoteException;
    public String checkMatchStatus(String playerName) throws RemoteException;

    void playerLostRound(String loserName) throws RemoteException;

    String getOpponentSecret(String username)throws RemoteException;
}


