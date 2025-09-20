import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public class Player extends UnicastRemoteObject implements InterfacePlayer {
    private List<String> request = new ArrayList<>();
    private ConcurrentHashMap<String, Boolean> reponse = new ConcurrentHashMap<>();
    private String myplayer = "";
    private String cle;
    private boolean readyForNext = false;
    private String lastMessage = "";

    protected Player() throws RemoteException {
        super();
    }

    @Override
    public synchronized Boolean requestfind() throws RemoteException {
        return !request.isEmpty();
    }

    @Override
    public synchronized List<String> listofrequeat() throws RemoteException {
        return new ArrayList<>(request);
    }

    @Override
    public synchronized void SetReponse(String name) throws RemoteException {
        if (name.equals("0")) {
            for (String requesterName : request) {
                reponse.put(requesterName, false);
            }
            request.clear();
        } else {
            reponse.put(name, true);
            myplayer = name;
            for (String requesterName : request) {
                if (!requesterName.equals(name)) {
                    reponse.put(requesterName, false);
                }
            }
            request.clear();
        }
    }

    @Override
    public synchronized void NewRequest(String name) throws RemoteException {
        if (!request.contains(name)) {
            request.add(name);
        }
    }

    @Override
    public synchronized boolean ReponseFindFromUser(String name) throws RemoteException {
        return reponse.containsKey(name);
    }

    @Override
    public synchronized Boolean Reponse(String name) throws RemoteException {
        return reponse.get(name);
    }

    @Override
    public synchronized void setMyPlayer(String requester) throws RemoteException {
        myplayer = requester;
    }

    @Override
    public synchronized boolean cleisfind() throws RemoteException {
        return cle != null && !cle.isEmpty();
    }

    @Override
    public synchronized String giveplayer() throws RemoteException {
        return myplayer;
    }

    @Override
    public synchronized void SetCle(String pkey) throws RemoteException {
        cle = pkey;
    }

    @Override
    public synchronized String getmycle() throws RemoteException {
        return cle;
    }

    @Override
    public synchronized void deliverMessage(String s) throws RemoteException {
        lastMessage = s;
        // System.out.println("Message received: " + s); // Keep for debugging if needed
    }

    @Override
    public synchronized void clearKey() throws RemoteException {
        cle = null;
        readyForNext = false;
    }

    @Override
    public synchronized boolean isReadyForNext() throws RemoteException {
        return readyForNext;
    }

    @Override
    public synchronized void setReadyForNext(boolean ready) throws RemoteException {
        readyForNext = ready;
    }

    @Override
    public synchronized String getLastMessage() throws RemoteException {
        String messageToReturn = lastMessage;
        lastMessage = ""; // Clear message after it's read
        return messageToReturn;
    }
}

