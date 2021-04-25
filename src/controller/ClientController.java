package controller;


import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ClientController implements Initializable {
	
	@FXML Button connectButton;
	@FXML Button sendButton;
	@FXML TextField chatField;
	@FXML TextArea chatArea;
	@FXML Button captureButton;
	@FXML Button shareButton;
	@FXML TextField idField;
	@FXML Canvas canvas;
	@FXML ColorPicker cp;
	
	Socket socketToChat = null;
	Socket socketToImage = null;
	String id = null;
	GraphicsContext gc;
	WritableImage clientWi;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		idField.setEditable(false);
		
		gc = canvas.getGraphicsContext2D();
		canvasMouseEvent();
		//�� ���� �����ϱ�
		cp.setValue(Color.RED); // ����Ʈ ������
		gc.setStroke(Color.RED); // ����Ʈ ��Ʈ��ũ ������
		cp.setOnAction(e->{
			gc.setStroke(cp.getValue());
		});
		
	}
	// �ʱ� ����
	public void settingClient(String id,Socket socketToChat, Socket socketToImage) {
		this.id = id;
		this.socketToChat = socketToChat;
		this.socketToImage = socketToImage;
		idField.setText(id);
		receiveChat();
		receiveImage();
	}
	
	public void connectToServer() {
	
		if(connectButton.getText().equals("�������")) {
			startClient();
			connectButton.setText("��������");
		}else {
			stopClient();
			connectButton.setText("�������");
		}
	}
	//�翬�� �õ�
	public void startClient() {
		
		Thread thread = new Thread() {
			public void run() {
				try {
					socketToChat = new Socket();
					socketToImage = new Socket();
					socketToChat.connect(new InetSocketAddress("localhost",5001));
					socketToImage.connect(new InetSocketAddress("localhost",5001)); 
					//===================blocking========================
					
					String data = "login:"+id;
					send(data);
					
				} catch (IOException e) {
					Platform.runLater(()->displayText("[ ���� ��� ���� ]"));
					if(!socketToChat.isClosed()) {
						stopClient();
					}
					return;
				}
				
				receiveChat();
				receiveImage();
			}
		};
		
		thread.start();
	}
	
	public void stopClient() {
		
		try {	
			Platform.runLater(()->{
			connectButton.setText("�������");
			sendButton.setDisable(true);
			});
				
			if(socketToChat != null && !socketToChat.isClosed()) {
				socketToChat.close();
			}
			if(socketToImage != null && !socketToImage.isClosed()) {
				socketToImage.close();
			}
		} catch (IOException e) {}
	
	}
		
	public void receiveImage() {
		Thread threadImage = new Thread() {
			public void run() {
				while(true) {
					try {
						InputStream inputStream = socketToImage.getInputStream();
						
						//�̹��� �޴� ��Ʈ��
						ObjectInputStream ois = new ObjectInputStream(inputStream);
						byte[] bytes = (byte[])ois.readObject();
					
						//�̹��� �ޱ�
						BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
						Image image = SwingFXUtils.toFXImage(bufferedImage,null);
						gc.drawImage(image, 0, 0,canvas.getWidth(),canvas.getHeight());
						
					} catch (IOException | ClassNotFoundException e) {
						Platform.runLater(()->displayText("[ �̹��� ���� ���� ]"));
						stopClient(); 
						break;
					}
				}
			}
		};
		threadImage.start();
	}
	
	public void receiveChat() {
		
		Thread threadChat = new Thread() {
			public void run() {
			while(true) {
				//ä�� �޴� ��Ʈ��
				try {
					byte[] bytesArr = new byte[100];
					InputStream inputStream;
					inputStream = socketToChat.getInputStream();
					
					int byteReadCount = inputStream.read(bytesArr);
					//=================================================================
					
					if(byteReadCount == -1) {
						throw new IOException();
					}
					
					String data = new String(bytesArr,0,byteReadCount,"UTF-8");
					
					// �翬�� ������
					if(data.equals("true")) {
						Platform.runLater(()->{
							String message = "[ ���� �Ϸ� : " + socketToChat.getRemoteSocketAddress()+ " ]";
							displayText(message);
							connectButton.setText("��������");
							sendButton.setDisable(false);
						});
					}else if(data.equals("false")) {
						Platform.runLater(()->{
							String message = "[ �翬�ῡ �����Ͽ����ϴ�. ]";
							displayText(message);
						});
					}
					else {
						Platform.runLater(()->displayText(data));
					}
				} catch (IOException e) {
					Platform.runLater(()->displayText("[ ä�� ���� ���� ]"));
					stopClient(); 
					break;
				}
			}
			}
		};
		
		threadChat.start();
	}
	
	public void sendToServer() {
		
		String chat = chatField.getText();
		String data ="chatt:"+chat;
		send(data);
	}
	
	public void send(String data) {
			try {
				byte[] bytes = data.getBytes("UTF-8");
				OutputStream outputStream = socketToChat.getOutputStream();
				outputStream.write(bytes);
				outputStream.flush();
				chatField.setText("");
				
		} catch (IOException e) {
				Platform.runLater(()->displayText("[ ������ ���� ]"));
				stopClient();
		}		
	}
	
	public void displayText(String message) {
		chatArea.appendText(message+"\n");
	}

	public void doCapture() throws IOException, AWTException {
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/capturepage.fxml"));
		Parent root = loader.load();
		CaptureController cc = loader.getController();
		cc.setFullPage(canvas,gc);
		
		Scene scene = new Scene(root);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.show();
		
	}
	//1. ����ȭ TOARRAY
	//2. BASE 64 ���ڵ�
	//3. JSON���
	
	public void shareCapture() {
		// ĵ���� �׸��� �̹����� �����
		WritableImage wi = new WritableImage((int)canvas.getWidth(),(int)canvas.getHeight());
		canvas.snapshot(new SnapshotParameters(), wi);
		clientWi = wi;
		
		OutputStream outputStream;
				try {					
					outputStream = socketToImage.getOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(outputStream);
					
					BufferedImage bi = SwingFXUtils.fromFXImage(clientWi, null);
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(bi,"png", baos);
					byte[] byteImage = baos.toByteArray();
					
					oos.writeObject(byteImage);
					oos.flush();
					baos.flush();
				} catch (IOException e) {
					System.out.println("Ŭ���̾�Ʈ �̹��� �۽Ű��� �� ���� �߻�");
			} 
		}
	
	
	public void canvasMouseEvent() {
		
		canvas.setOnMousePressed(e->{
			gc.beginPath();
			gc.lineTo(e.getSceneX()-15, e.getSceneY()-70);
			gc.stroke();
			
		});
		canvas.setOnMouseDragged(e->{
			gc.lineTo(e.getSceneX()-15, e.getSceneY()-70);
			gc.stroke();
		});
		
	}
}
