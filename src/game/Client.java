package game;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;

public class Client
{

    private JFrame frame = new JFrame("Tic Tac Toe");
    private JLabel messageLabel = new JLabel("");

    private Tile[] board = new Tile[9];
    private Tile currentTile;

    private String player_type;
    private String opponent_type;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private Client(String serverAddress) throws Exception
    {
        //Making connection
        socket = new Socket(serverAddress, 8080);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        //Setting msg label...
        messageLabel.setForeground(Color.GREEN);
        frame.getContentPane().setBackground(Color.BLACK);
        messageLabel.setFont(new Font("sans-serif", Font.BOLD, 20));
        frame.getContentPane().add(messageLabel, "North");

        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        for (int i = 0; i < board.length; i++) //building 3x3 board
        {
            final int j = i;
            board[i] = new Tile();
            board[i].addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent e)
                { //adding action listener
                    currentTile = board[j];
                    out.println("MOVE " + j);}});
            boardPanel.add(board[i]);
        }
        frame.getContentPane().add(boardPanel, "Center");
    }

    private void play() throws Exception //main method to react to server commands
    {
        String response;
        try
        {
            response = in.readLine();
            if (response.startsWith("WELCOME"))
            {
                char mark = response.charAt(8);
                player_type = (mark == 'X' ? "X" : "O");
                opponent_type = (player_type.equals("X") ? "O" : "X");
                frame.setTitle("Tic Tac Toe - Player " + mark);
            }
            while (true)
            {
                response = in.readLine();
                if (response.startsWith("LEGAL_MOVE"))
                {
                    messageLabel.setText("Checking move, please wait");
                    currentTile.setMark(player_type);
                    currentTile.repaint();
                } else if (response.startsWith("OPPONENT_MOVED"))
                {
                    int loc = Integer.parseInt(response.substring(15));
                    board[loc].setMark(opponent_type);
                    board[loc].repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("WIN"))
                {
                    messageLabel.setText("You win");
                    break;
                } else if (response.startsWith("DEFEAT"))
                {
                    messageLabel.setText("You lose");
                    break;
                } else if (response.startsWith("TIE"))
                {
                    messageLabel.setText("You tied");
                    break;
                } else if (response.startsWith("M")) //when command starts with "M" it is plain message
                {
                    messageLabel.setText(response.substring(2));
                }
            }
            out.println("QUIT");
        }
        finally
        {
            socket.close();
        }
    }

    private boolean wantsToPlayAgain()
    {
        int response = JOptionPane.showConfirmDialog(frame, "Do you wanna to play again?", "AGAIN?", JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION; //when i click "yes" returns true (i wanna play again)
    }

    static class Tile extends JPanel // simple 1x1 tile
    {
        JLabel mark = new JLabel();
        Tile()
        {
            setBackground(Color.BLUE);
            mark.setFont(new Font("sans-serif", Font.BOLD, 110));
            mark.setForeground(Color.RED);
            add(mark);
        }
        void setMark(String m) //function sets X or O sign
        {
            mark.setText(m);
        }
    }

    public static void main(String ip) throws Exception
    {
        while (true)
        {
            String serverAddress = (ip.equals(" ")) ? "127.0.0.1" : ip; //when arg is empty ip became local_adress
            Client client = new Client(serverAddress);
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setSize(500, 500);
            client.frame.setVisible(true);
            client.frame.setResizable(false);
            client.play();
            if (!client.wantsToPlayAgain()) break;
        }
    }
}
