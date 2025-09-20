import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

public class GamePlay {
    public static void main(String[] args) {
        try {
            InterfaceGame server = (InterfaceGame) Naming.lookup("rmi://127.0.0.1:2000/chat");
            InterfacePlayer me = new Player();
            Scanner key = new Scanner(System.in);

            System.out.println("Sign in with your username:");
            String name = key.nextLine();
            while (!server.accept(name)) {
                System.out.println("This name is already used. Try another one:");
                name = key.nextLine();
            }

            server.connect(name, me);
            System.out.println("Signed in successfully!");

            boolean stayConnected = true;
            while (stayConnected) {
                boolean gameStarted = false;
                while (!gameStarted) {
                    System.out.println("=== GAME LOBBY ===");
                    System.out.println("Online players: ");
                    List<String> list = server.listofplayeron();
                    for (int i = 0; i < list.size(); i++) {
                        if (!list.get(i).equals(name))
                            System.out.println("- " + list.get(i));
                    }

                    System.out.println("Options:");
                    System.out.println("1. 'choose' - Challenge a player");
                    System.out.println("2. 'wait' - Wait for requests");
                    System.out.println("3. 'quit' - Exit game");
                    System.out.print("Your choice: ");
                    String choose = key.nextLine().toLowerCase().trim();

                    if (choose.equals("wait")) {
                        System.out.println("Waiting for game requests...");
                        while (!server.findrequest(name)) {
                            Thread.sleep(1000);
                        }

                        List<String> requesters = server.NameOfRequestPlayer(name);
                        System.out.println("Game requests from: " + requesters);
                        System.out.println("Enter player name to accept, or '0' to reject all:");
                        String requester = key.nextLine();

                        if (requester.equals("0")) {
                            server.AllIsNot(name);
                            System.out.println("All requests rejected.");
                        } else if (requesters.contains(requester)) {
                            server.NameOfRequestAccept(name, requester);
                            server.startgamewith(name, requester);
                            System.out.println("Game started with " + requester + "!");
                            gameStarted = true;
                        } else {
                            System.out.println("Invalid player name. Try again.");
                        }

                    } else if (choose.equals("choose")) {
                        System.out.println("Enter the name of the player you want to challenge:");
                        String target = key.nextLine();

                        list = server.listofplayeron();
                        if (!list.contains(target) || target.equals(name)) {
                            System.out.println("Invalid or offline player. Try again.");
                            continue;
                        }

                        server.SendRequestTo(name, target);
                        System.out.println("Request sent to " + target + ". Waiting for response...");

                        int timeoutCounter = 0;
                        while (!server.FindReponse(name, target)) {
                            Thread.sleep(1000);
                            timeoutCounter++;

                            if (timeoutCounter % 5 == 0) {
                                System.out.println("Still waiting for " + target + "'s response... (" + timeoutCounter + "s)");
                            }

                            if (timeoutCounter >= 30) {
                                System.out.println("Request timed out. " + target + " didn't respond.");
                                break;
                            }
                        }

                        if (timeoutCounter >= 30) {
                            continue;
                        }

                        Boolean response = server.GetReponse(name, target);
                        if (response != null && response) {
                            server.startgamewith(name, target);
                            System.out.println("Your request was accepted! Starting game with " + target + "...");
                            gameStarted = true;
                        } else {
                            System.out.println("Request rejected by " + target + ".");
                        }

                    } else if (choose.equals("quit")) {
                        System.out.println("Goodbye!");
                        server.disconnect(name);
                        stayConnected = false;
                        break;
                    } else {
                        System.out.println("Please enter 'choose', 'wait', or 'quit'");
                    }
                }

                if (gameStarted) {
                    System.out.println("GAME STARTED!");
                    System.out.println("Rules: Guess your opponent's 4-digit number. Win 2 rounds to win the match!");

                    boolean matchEnded = false;
                    while (!matchEnded) {
                        int level = server.getlevel(name);
                        if (level > 3) {
                            break;
                        }

                        System.out.println(" ROUND " + level);
                        System.out.println("Enter your 4-digit secret number:");
                        String pkey;
                        do {
                            pkey = key.nextLine();
                            if (pkey.length() != 4 || !pkey.matches("\\d{4}")) {
                                System.out.println("Please enter exactly 4 digits:");
                            }
                        } while (pkey.length() != 4 || !pkey.matches("\\d{4}"));

                        server.sendkeyToOtherPlayer(name, pkey);
                        System.out.println(" Your number is set. Waiting for opponent...");

                        while (!server.start(name)) {
                            System.out.println(" Waiting for opponent to set their number...");
                            Thread.sleep(2000);
                        }

                        String opponentKey = server.givemycle(name);
                        System.out.println("Both players ready! Start guessing!");

                        boolean roundEnded = false;
                        int attempts = 0;
                        while (!roundEnded) {
                            attempts++;
                            System.out.println(" Attempt " + attempts + " - Enter your guess (4 digits):");
                            String guess = key.nextLine();

                            int correctDigits = server.count(guess, opponentKey);
                            if (correctDigits == -1) {
                                System.out.println("Invalid input! Please enter exactly 4 digits.");
                                attempts--;
                                continue;
                            }

                            if (correctDigits == 4) {
                                System.out.println(" PERFECT! You guessed it correctly!");
                                server.win(name);

                                String matchResult = server.score(name);
                                if (matchResult.equals("win")) {
                                    System.out.println("YOU WON THE MATCH!");
                                    server.wintotal(name);
                                    matchEnded = true;
                                } else {
                                    System.out.println(" You won this round!");
                                    server.setReadyForNext(name);
                                    System.out.println(" Waiting for next round...");
                                    while (!server.isReadyForNextLevel(name)) {
                                        Thread.sleep(1000);
                                    }
                                    server.nextLevel();
                                }
                                roundEnded = true;
                            } else {
                                System.out.println( correctDigits + " digits correct!");
                                if (server.OtherPlayerIsWin(name)) {
                                    System.out.println("Your opponent won this round!");

                                    String matchResult = server.score(name);
                                    if (!matchResult.equals("win")) {
                                        server.setReadyForNext(name);
                                        System.out.println(" Preparing for next round...");

                                        while (!server.isReadyForNextLevel(name)) {
                                            Thread.sleep(1000);
                                        }
                                        server.nextLevel();
                                    }
                                    roundEnded = true;
                                }
                            }
                        }

                        String finalResult = server.score(name);
                        if (finalResult.equals("win")) {
                            matchEnded = true;
                        }
                    }

                    System.out.println("Match completed! Returning to lobby...");
                    Thread.sleep(3000);
                }
            }

        } catch (NotBoundException | MalformedURLException | RemoteException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}