# Distributed Number Guessing Game

A multiplayer number guessing game built with Java RMI (Remote Method Invocation) that allows players to challenge each other in real-time number guessing battles.

## 🎮 Game Overview

This is a distributed multiplayer guessing game where:
- Two players connect through a central server
- Each player sets a secret 4-digit number
- Players take turns guessing their opponent's secret number
- The game tracks how many digits are correctly placed
- First player to correctly guess wins the round
- Best of 3 rounds wins the match

## 🏗️ Architecture

The project uses Java RMI for distributed communication with a client-server architecture:

- **Server**: Manages player connections, game sessions, and guess validation
- **Client**: Provides GUI interface for players to interact with the game
- **RMI Registry**: Handles remote object registration and lookup

## 📁 Project Structure

```
├── Game.java                  # Server implementation with game logic
├── GameGUI.java              # Main game interface for playing
├── GameLauncher.java         # Application entry point
├── GamePlay.java             # Console-based gameplay (alternative)
├── InterfaceGame.java        # Server remote interface
├── InterfacePlayer.java      # Client remote interface
├── Player.java               # Client implementation
├── PlayerSelectionGUI.java   # Lobby for selecting opponents
├── SignInGUI.java           # Login interface
└── Register.java            # RMI registry setup
```

## 🚀 Getting Started

### Prerequisites

- Java 8 or higher
- Support for Arabic text display (GUI uses Arabic labels)
- Basic understanding of Java RMI

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/distributed-guessing-game.git
   cd distributed-guessing-game
   ```

2. **Compile all Java files**
   ```bash
   javac *.java
   ```

3. **Start the RMI Registry**
   ```bash
   java Register
   ```
   This will show your IP address and create the registry on port 2000.

4. **Start the Server**
   ```bash
   java Game
   ```
   You should see "Game server is running..." message.

5. **Start Client(s)**
   ```bash
   java GameLauncher
   ```
   Run this command for each player (minimum 2 players needed for a game).

## 🎯 How to Play

1. **Sign In**: Enter a unique username and click the Arabic sign-in button
2. **Player Selection**: View online players and send/receive game invitations
3. **Accept Challenge**: Choose to challenge another player or wait for invitations
4. **Set Secret Number**: Enter your 4-digit secret number (no repeated digits recommended)
5. **Guessing Phase**: 
   - Take turns guessing your opponent's 4-digit number
   - See how many digits are in the correct position
   - Continue until someone guesses correctly
6. **Match Progression**:
   - Win rounds by guessing correctly first
   - First to win 2 rounds wins the match
   - New rounds start automatically after each round completion

## 🎮 Game Features

### Core Gameplay
- **4-Digit Secret Numbers**: Each player sets a secret 4-digit code
- **Position-Based Scoring**: Get feedback on how many digits are in correct positions
- **Round-Based Matches**: Best of 3 rounds determines the winner
- **Real-time Updates**: Live score tracking and game state synchronization

### User Interface
- **Arabic GUI Support**: Interface includes Arabic text for better localization
- **Color-Coded Digits**: Visual representation of numbers with distinct colors
- **Guess History**: Track all your previous guesses and their results
- **Status Updates**: Real-time feedback on game progress

### Multiplayer Features
- **Player Lobby**: See all online players available for challenges
- **Game Invitations**: Send and receive challenge requests
- **Opponent Notifications**: Get notified when opponents make moves
- **Graceful Disconnection**: Handle player exits cleanly

## 🔧 Technical Details

### RMI Communication
- **Registry Port**: 2000
- **Server URL**: `rmi://127.0.0.1:2000/chat`
- **Remote Interfaces**: Separate interfaces for game server and player clients

### Game Logic
- **Secret Validation**: Ensures 4-digit numbers only
- **Guess Processing**: Server validates guesses against opponent secrets
- **Match Management**: Tracks rounds, scores, and game progression
- **Concurrency**: Thread-safe operations using `ConcurrentHashMap`

### GUI Components
- **Swing-Based Interface**: Modern Java Swing components
- **Arabic Font Support**: Uses "Arial Unicode MS" for proper text rendering
- **Responsive Design**: Handles window events and user interactions
- **Timer-Based Updates**: Automatic refresh of game state and messages

## 🛠️ Development Notes

### Key Classes

- **Game**: Core server logic managing all game sessions and player interactions
- **GameGUI**: Main game interface handling secret setting and guessing
- **PlayerSelectionGUI**: Lobby system for finding and challenging opponents  
- **SignInGUI**: User authentication and server connection
- **Player**: Client-side implementation handling server communications

### Design Patterns Used
- **Remote Proxy**: RMI interfaces abstract network communication
- **Observer**: Clients receive notifications about game events
- **State Machine**: Game progresses through defined states (lobby, playing, waiting, etc.)
- **MVC**: Separation of game logic, UI, and data management

### Thread Safety
- Uses `ConcurrentHashMap` for player management
- `SwingWorker` for background server communications
- Synchronized methods for critical game state changes
- Timer-based polling for real-time updates

## 🐛 Known Issues & Limitations

- Server must be running before any clients can connect
- Limited to localhost/LAN connections (hardcoded IP: 127.0.0.1)
- No persistent storage - game data lost on server restart
- Arabic text may not display properly on systems without proper font support
- No spectator mode - only active players can view games
- Limited error recovery if network connection is lost mid-game

## 🎯 Game Rules & Strategy

### Winning Conditions
- **Round Win**: First player to correctly guess opponent's 4-digit number
- **Match Win**: First player to win 2 rounds out of 3
- **Forfeit**: Player leaving the game forfeits the entire match

### Strategy Tips
- Choose numbers without repeated digits for easier tracking
- Use systematic guessing patterns to narrow down possibilities
- Pay attention to position feedback to optimize next guesses
- Balance defensive (hard to guess) and offensive (easy to remember) number choices

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/NewFeature`)
3. Commit your changes (`git commit -m 'Add NewFeature'`)
4. Push to the branch (`git push origin feature/NewFeature`)
5. Open a Pull Request


## 🚀 Future Enhancements

- [ ] Network configuration for WAN play
- [ ] Player statistics and win/loss records  
- [ ] Tournament bracket system for multiple players
- [ ] Difficulty levels (3-digit, 5-digit, etc.)
- [ ] Spectator mode for watching ongoing games
- [ ] Chat system between players
- [ ] AI opponents for single-player practice
- [ ] Game replay system
- [ ] Persistent player accounts and rankings
- [ ] Mobile client version

## 🔧 Troubleshooting

### Common Issues

**"Connection refused" error**
- Ensure Register.java is running first
- Verify Game.java server is started
- Check that port 2000 is available

**Arabic text not displaying**
- Install Arabic language support on your system
- Ensure "Arial Unicode MS" font is available
- Try running with `-Dfile.encoding=UTF-8` JVM parameter

**Players can't see each other**
- Confirm all clients connect to same server IP
- Check firewall settings for port 2000
- Verify RMI registry is accessible

**Game freezes or hangs**
- Check console for RMI timeout errors
- Restart server and reconnect clients
- Ensure stable network connection

## 📞 Support

If you encounter issues:
1. Check console output for error messages
2. Verify all components (Registry, Server, Clients) are running
3. Test with localhost connections first
4. Create an issue on GitHub with detailed error information and steps to reproduce

---

**Enjoy the Number Guessing Challenge!** 🔢🎯
