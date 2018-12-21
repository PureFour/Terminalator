package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{

    public static void main(String[] args) throws Exception
    {
        ServerSocket serverSocket = new ServerSocket(8080); //setting server on 8080
        System.out.println("Server is Running...");
        try
        {
            while(true)
            {
                Game game = new Game(); //creating game instance
                Game.Player playerX = game.new Player(serverSocket.accept(), 'X'); //first player became "X"
                Game.Player playerO = game.new Player(serverSocket.accept(), 'O'); //second "O"
                playerX.setOpponent(playerO); //setting opponents
                playerO.setOpponent(playerX);
                game.currentPlayer = playerX; // "X" player starting...
                playerX.start();
                playerO.start();
            }
        } finally
        {
            serverSocket.close();
        }
    }
}
class Game
{
    private Player[] board =
            {
            null, null, null,
            null, null, null,
            null, null, null};

    Player currentPlayer;

    private boolean isWinner() //this method checks when is winner
    {
        return
                (board[0] != null && board[0] == board[1] && board[0] == board[2])
                        ||(board[3] != null && board[3] == board[4] && board[3] == board[5])
                        ||(board[6] != null && board[6] == board[7] && board[6] == board[8])
                        ||(board[0] != null && board[0] == board[3] && board[0] == board[6])
                        ||(board[1] != null && board[1] == board[4] && board[1] == board[7])
                        ||(board[2] != null && board[2] == board[5] && board[2] == board[8])
                        ||(board[0] != null && board[0] == board[4] && board[0] == board[8])
                        ||(board[2] != null && board[2] == board[4] && board[2] == board[6]);
    }

    private boolean isBoardFull() // this method returns true when board is full (when its TIE)
    {
        for(int i = 0; i < board.length; i++)
            if(board[i] == null) return false;
        return true;
    }

    private synchronized boolean isLegal(int location, Player player) //this method checks player move
    {                                                               //its synchronised because only one thread can have access to it in time!
        if (player == currentPlayer && board[location] == null) //player can move when board tile is empty and its player turn!
        {
            board[location] = currentPlayer;
            currentPlayer = currentPlayer.opponent;
            currentPlayer.otherPlayerMoved(location);
            return true;
        }
        return false;
    }

    class Player extends Thread
    {
        char mark;
        Player opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;

        public Player(Socket socket, char mark)
        {
            this.socket = socket;
            this.mark = mark;
            try
            {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("WELCOME " + mark);
                output.println("M Waiting for opponent to connect");
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            }
        }

        void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        void otherPlayerMoved(int location)
        {
            output.println("OPPONENT_MOVED " + location);
            output.println(isWinner() ? "DEFEAT" : isBoardFull() ? "TIE" : "");
        }

        public void run()
        {
            try
            {
                output.println("M All players connected");

                if (mark == 'X')
                {
                    output.println("M Your move");
                }
                while(true)
                {
                    String command = input.readLine();
                    if(command.startsWith("MOVE"))
                    {
                        int location = Integer.parseInt(command.substring(5));
                        if (isLegal(location, this))
                        {
                            output.println("LEGAL_MOVE");
                            output.println(isWinner() ? "WIN" : isBoardFull() ? "TIE" : "");
                        } else output.println("M Try Again");
                    } else if (command.startsWith("QUIT")) return;
                }
            } catch (IOException e)
            {
                e.getMessage();
            } finally {
                try {socket.close();} catch (IOException e) {}
            }
        }
    }
}
