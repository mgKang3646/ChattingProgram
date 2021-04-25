package controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class IndexController {
	
	@FXML RadioButton serverButton;
	@FXML RadioButton clientButton;
	@FXML Button startButton;
	@FXML HBox center;
	boolean serverFlag = false;
	boolean clientFlag = false;
	
	public void onServer() throws IOException {
		clientButton.setSelected(false);
		
		serverFlag = true;
		clientFlag = false;
	}
	
	public void onClient() throws IOException {
		serverButton.setSelected(false);
		
		clientFlag=true;
		serverFlag=false;
	}
	
	public void start() throws IOException {
		
		Stage primaryStage = (Stage)startButton.getScene().getWindow();
		
		if(serverFlag) {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/server.fxml"));
			Scene scene = new Scene(loader.load());
			primaryStage.setScene(scene);
			primaryStage.show();
		}
		
		if(clientFlag) {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.show();
		}
	}

}
