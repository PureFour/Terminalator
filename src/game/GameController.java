package game;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GameController
{
    public Pane pane = new Pane();
    public TextField ip_field = new TextField();
    private Thread server_thread;
    @FXML
    void initialize()
    {
        ip_field.setPromptText("Please input id...");
    }
    @FXML
    void startClient() //method calls another program (Client.class) with input id
    {
        Thread client_thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    game.Client.main(ip_field.getText());
                    System.out.println("Client Started!");
                }catch (Exception e) {e.getMessage();}
            }
        });
        client_thread.start();
    }
    @FXML
    void startServer() //method calls another program (Server.class) in background
    {
        server_thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String[] args = {};
                    game.Server.main(args);
                    System.out.println("Server Started!");
                }catch (Exception e) {e.getMessage();}
            }
        });
        server_thread.start();
    }
    @FXML
    void exit() throws Exception //exit method
    {
        end_window();
        Stage stage = (Stage)pane.getScene().getWindow();
        server_thread.interrupt(); //stopping server thread
        stage.close(); //closing main stage
    }

    private void end_window() throws Exception
    {
        Parent parent = FXMLLoader.load(getClass().getResource("end_window.fxml"));
        Stage stage = new Stage();
        stage.setTitle("END WINDOW");
        stage.setScene(new Scene(parent));
        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);  // exits whole application
            }
        });
    }
}
