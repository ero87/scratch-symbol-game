Scratch Game

Prerequisites
Java 21 (or higher)
Apache Maven 3.9.9 (or higher)

Installation
Clone the repository:
git clone https://github.com/ero87/scratch-symbol-game.git

cd scratch-symbol-game
Build the project:
mvn clean install

Running the Game
Execute the game with your desired configuration:
bash
java -jar target/scratch-game-1.0-SNAPSHOT.jar --config config.json --betting-amount 100
Command line arguments:
--config: Path to configuration file (required)
--betting-amount: Player's bet amount (required)

                      # Unit tests
Testing
Run all tests:
bash
mvn test
