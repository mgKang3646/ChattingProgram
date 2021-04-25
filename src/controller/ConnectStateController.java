package controller;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ConnectStateController {
	
	@FXML VBox connectVbox;
	@FXML VBox nonConnectVbox;
	
	ArrayList<String> connect;
	ArrayList<String> nonConnect;
	
	public void settingArrayList(ArrayList<String> connect, ArrayList<String> nonConnect) {
		this.connect = connect;
		this.nonConnect = nonConnect;
		
		visibleID();
	}
	
	public void visibleID() {
		for(int i=0; i<connect.size();i++) {
			Label idConnectLabel = new Label(connect.get(i));
			System.out.println("scs 연결된 id : "+ connect.get(i));
			connectVbox.getChildren().add(idConnectLabel);
		}
		
		for(int i=0; i<nonConnect.size();i++) {
			Label idLabel = new Label(nonConnect.get(i));
			System.out.println("scs 연결되지 않은 id : "+ nonConnect.get(i));
			nonConnectVbox.getChildren().add(idLabel);
		}
	}
}
