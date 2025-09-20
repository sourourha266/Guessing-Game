import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class Register {
    public static void main(String[] args) {

        try {
            int port=2000;
            System.out.println("IP:"+ InetAddress.getLocalHost());
            LocateRegistry.createRegistry(port);
            (new Scanner(System.in)).nextLine();
        } catch (UnknownHostException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
