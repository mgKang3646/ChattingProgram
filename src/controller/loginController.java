package controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class loginController implements Initializable{
	
	@FXML TextField idField;
	@FXML Button loginButton;
	@FXML Label joinLabel;
	@FXML Label idLabel;
	Socket socketToChat = null;
	Socket socketToImage = null;
	Stage primaryStage;
	
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		idLabel.setVisible(false); // 안 보이게 하기
		joinLabelMouseEvent();
		
		try {
			socketToChat = new Socket();
			socketToImage = new Socket();
			socketToChat.connect(new InetSocketAddress("localhost",5001));
			socketToImage.connect(new InetSocketAddress("localhost",5001)); 
			/////////////////////////blocking////////////////////////
			System.out.println("로그인 소켓 연결 완료");
			
			receive(); 
			
		} catch (IOException e) {
			try {
				System.out.println("오류로 인한 로그인 소켓 종료");
				socketToChat.close();
				socketToImage.close();
			} catch (IOException e1) {}	
		}
	}
	
	public void login() {
		String id = idField.getText();
		String data = "login:"+id;
		
		send(data);
	}
	
	public void send(String data) {
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
						
						if(data.equals("true")) { // 스레드 종료 후 클라이언트 페이지로 넘어가기 
							System.out.println("data : "+data);
							Platform.runLater(()->{
								try {
									FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/client.fxml"));
									Parent root = loader.load();
									ClientController cc = loader.getController();
									cc.settingClient(idField.getText(),socketToChat,socketToImage);
									Scene scene = new Scene(root);
									primaryStage = (Stage)loginButton.getScene().getWindow();
									primaryStage.setScene(scene);
									primaryStage.show();
									
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							});
							
							// + 소켓 객체랑 id 데이터 가지고 넘어가기
							break;
						}
						else if(data.equals("false")) {
							System.out.println("data : "+ data);
							Platform.runLater(()->{
								idLabel.setVisible(true);
							});
						}
						//스레드 반복종료
						else {
							break;
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
	
	public void joinLabelMouseEvent() {
	
		joinLabel.setOnMouseEntered((event)->{
			joinLabel.setTextFill(Color.WHITE);
		});
		
		joinLabel.setOnMouseExited((event)->{
			joinLabel.setTextFill(Color.BLACK);
		});
		joinLabel.setOnMouseClicked((event)->{
			// receive 스레드 종료 시키기
			String data = "EXITT:";
			send(data);
			
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/join.fxml"));
				Parent root = loader.load();
				JoinController jc = loader.getController();
				jc.settingSocket(socketToChat, socketToImage);
				Scene scene = new Scene(root);
				primaryStage = (Stage)joinLabel.getScene().getWindow();
				primaryStage.setScene(scene);
				primaryStage.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	

}
