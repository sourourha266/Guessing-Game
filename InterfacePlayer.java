import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface InterfacePlayer extends Remote {
    Boolean requestfind() throws RemoteException;
    List<String> listofrequeat() throws RemoteException;
    void SetReponse(String name) throws RemoteException;
    void NewRequest(String name) throws RemoteException;
    boolean ReponseFindFromUser(String name) throws RemoteException;
    Boolean Reponse(String name) throws RemoteException;
    void setMyPlayer(String requester) throws RemoteException;
    boolean cleisfind() throws RemoteException;
    String giveplayer() throws RemoteException;
    void SetCle(String pkey) throws RemoteException;
    String getmycle() throws RemoteException;
    void deliverMessage(String s) throws RemoteException;
    void clearKey() throws RemoteException;
    boolean isReadyForNext() throws RemoteException;
    void setReadyForNext(boolean ready) throws RemoteException;
    String getLastMessage() throws RemoteException;
}