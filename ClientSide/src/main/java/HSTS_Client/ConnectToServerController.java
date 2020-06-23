package HSTS_Client;

import java.io.IOException;

import org.greenrobot.eventbus.EventBus;

import HSTS_Entities.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ConnectToServerController {
	private AppsClient client;
	
    @FXML
    private TextField host_text;

    @FXML
    private Button connect_btn;

    @FXML
    private AnchorPane logo;

    @FXML
    private Text logo_text;

    @FXML
    void connect(ActionEvent event) 
    {
		client = AppsClient.getClient();
		client.setHost(host_text.getText());
		try {
			client.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Stage stage = (Stage) connect_btn.getScene().getWindow();
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/HSTS_Client/Login.fxml"));
			stage.setTitle("High School Test System");
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.show();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
