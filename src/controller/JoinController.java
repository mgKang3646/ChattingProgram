package controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class JoinController implements Initializable {
	@FXML TextField idField;
	@FXML Label idCheckLabel;
	@FXML Button joinButton;
	Socket socketToChat = null;
	Socket socketToImage = null;
	Stage primaryStage;

	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		idCheckLabel.setVisible(false); // 안 보이게 하기
	}
	
	public void settingSocket(Socket socketToChat, Socket socketToImage) {
		this.socketToChat = socketToChat;
		this.socketToImage = socketToImage;
		receive(); 
	}
	
	public void join() {
		String id = idField.getText();
		String data = "joinn:"+id;
		
		try {
			byte[] bytes = data.getBytes("UTF-8");
			OutputStream os = socketToChat.getOutputStream();
			os.write(bytes);
			os.flush();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void receive() {
		
		Thread idCheckReceiver = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						InputStream is = socketToChat.getInputStream();
						byte[] bytes = new byte[100];
						int readByteCount = is.read(bytes);
						////////////blocking/////////////
						
						if(readByteCount == -1) {
							throw new IOException();
						}
						
						String data = new String(bytes,0,readByteCount,"UTF-8");
						
						if(data.equals("true")) { // ID가 중복되지 않은 경우
							System.out.println("data : "+data);
							Platform.runLater(()->{
								try {
									FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/client.fxml"));
									Parent root = loader.load();
									ClientController cc = loader.getController();
									cc.settingClient(idField.getText(),socketToChat,socketToImage);
									Scene scene = new Scene(root);
									primaryStage = (Stage)joinButton.getScene().getWindow();
									primaryStage.setScene(scene);
									primaryStage.show();
									
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							});
							// + 소켓 객체랑 id 데이터 가지고 넘어가기
							break;
						}else { // ID가 중복된 경우
							System.out.println("data : "+ data);
							Platform.runLater(()->{
								idCheckLabel.setVisible(true);
							});
						}
					}		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}
		});
		
		idCheckReceiver.start();
	}
	
	

}
