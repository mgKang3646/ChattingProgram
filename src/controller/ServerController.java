package controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ServerController implements Initializable{
	
	@FXML Button startButton;
	@FXML TextArea textArea;
	@FXML Button stateConnect;
	
	ServerSocket serverSocket;
	ExecutorService executorService;
	ArrayList<String> ids= new ArrayList<String>(); // �� ȸ��
	Vector<Client> clients = new Vector<Client>(); // ���� ���� ���� ȸ��
	
	public static final int CUT = 6; // idCheck�� substring �ڸ� ����
	public static final int JOIN = 5; // idCheck�� substring �ڸ� ����
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		ids.add("Alice");
		ids.add("Bob");
		ids.add("Carol");
		ids.add("Steve");
		textArea.setEditable(false);
	}
	public void viewStateConnect() {
		ArrayList<String> idConnect = new ArrayList<String>(); // ���� ���� ���� id
		ArrayList<String> idNonConnect = new ArrayList<String>(); // ���� ������ ���� id
		
		for(int i=0;i<clients.size();i++) {
			idConnect.add(clients.get(i).id);
		}
		
		for(int i=0; i<ids.size(); i++) {
			System.out.println();
			idConnect.add(ids.get(i)); //���ʹ� 
			int j;
			for(j=0; j<idConnect.size();j++) {
				if(ids.get(i).equals(idConnect.get(j))) break;
			}
			
			if(j==idConnect.size()-1) {
				idNonConnect.add(ids.get(i));
			}
			idConnect.remove(idConnect.size()-1); // ���ʻ�� ����
		}
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/connectstate.fxml"));
			Parent root = loader.load();
			ConnectStateController csc = loader.getController();
			csc.settingArrayList(idConnect, idNonConnect);
			Scene scene = new Scene(root);
			Stage primaryStage = (Stage)textArea.getScene().getWindow();
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.setX(primaryStage.getX()+50);
			stage.setY(primaryStage.getY()+50);
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	public void actionServer() {
		
		if(startButton.getText().equals("���� ����" )) {
			startServer();
		}else {
			stopServer();
		}
		
	}
	
	public void startServer() {
		executorService = Executors.newCachedThreadPool();
		
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("localhost",5001));
		} catch (IOException e) {
			if(!serverSocket.isClosed()) {
				//stopServer();
			}
			return;
		}
		
		//���� - Ŭ���̾�Ʈ ���� �۾�
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				
				Platform.runLater(()->{
					displayText("[ ���� ���� ]");
					startButton.setText("���� ����");
				});
				
				int count=0; // ä�� ����, �̹��� ���� ���п�
				Socket tmpSocket = null;  // ä�� ���� �ӽ� ������
				while(true){	
					try {
							Socket socket = serverSocket.accept();
							//============blocking================
							
							System.out.println("ī��Ʈ : "+count);
							
							if(count%2==1) { //�̹��� ���ϱ��� ���� �Ϸ�Ǹ� Ŭ���̾�Ʈ ����
								
								Platform.runLater(()->{
									String message = "[ �̹��� ���� ���� ���� : " + socket.getRemoteSocketAddress() + " ]";
									displayText(message);
								});
								
								Client client = new Client(tmpSocket, socket); 
								clients.add(client); // �ش� Ŭ���̾�Ʈ�� ������ ����� Ŭ���̾�Ʈ ��Ͽ� �߰�

								Platform.runLater(()->{
									String message = "[ ���� ���� : " + clients.size() + " ]";
									displayText(message);
								});
								
							}else { // %2�� 0�� ��� : ä�ÿ� ���� �ӽ�����
								tmpSocket = socket; 
								Platform.runLater(()->{
									String message = "[ ä�� ���� ���� ���� : " + socket.getRemoteSocketAddress() + " ]";
									displayText(message);
								});
							}
							
							count++;
						
					} catch (IOException e) {
						if(!serverSocket.isClosed()) stopServer();
						break;
					}
				}
			}
		};
		
		executorService.submit(runnable);
	}
	
	public void stopServer() {
		
			Iterator<Client> iterator = clients.iterator();
			
			while(iterator.hasNext()) {
				try {
					iterator.next().socketToChat.close();
					iterator.next().socketToImage.close();
				} catch (IOException e) {
					System.out.println("Ŭ���̾�Ʈ ���� ����������� ����");
				}
			}
			
			//iterator.remove();
			
			try {
				if(serverSocket != null && !serverSocket.isClosed()) serverSocket.close(); // �������� �ݱ�
				if(executorService != null && !executorService.isShutdown()) executorService.shutdown(); // ������Ǯ ����
				
				Platform.runLater(()->{
					displayText("[ ���� ���� ]");
					startButton.setText("���� ����");
				});
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void displayText(String message) {
		textArea.appendText(message+"\n");
	}
	
	
	class Client {
		Socket socketToChat;
		Socket socketToImage;
		String id;
		
		public Client(Socket socketToChat,Socket socketToImage) {
			this.socketToChat = socketToChat;
			this.socketToImage = socketToImage;
			
			receiveChat();
			receiveImage();
		}
		
		public void receiveImage() {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						while(true) {
							//byte[] byteArr = new byte[100];
							try {
								InputStream inputStream = socketToImage.getInputStream();
								ObjectInputStream ois = new ObjectInputStream(inputStream);
								
								byte[] bytes = (byte[])ois.readObject();								
								//=================================================================
								
								String message = "[ "+id+" �̹��� ��û ó�� : " + socketToImage.getRemoteSocketAddress()+" ]";
								Platform.runLater(()-> displayText(message));
								
								for(Client client : clients) {
									client.imageSend(bytes);
								}
							} catch (Exception e) {
								try {
									clients.remove(Client.this);
									String message = "[ "+id+" �̹��� ���� ���� : " + socketToImage.getRemoteSocketAddress() + " ]";
									Platform.runLater(()->displayText(message));
									socketToImage.close();
									break;
								} catch (IOException e1) {}
							}
					}
				}
			};
				
				executorService.submit(runnable);
		}
		
		public void receiveChat() {
			
			Runnable runnable = new Runnable() {
				public void run() {
					while(true) {
						
						try {
							byte[] byteArr = new byte[100];
							InputStream inputStream = socketToChat.getInputStream();
							int readByteCount = inputStream.read(byteArr);
							//=================blocking=========================
							
							if(readByteCount==-1) {
								throw new IOException();
							}
							
							String data = new String(byteArr,0,readByteCount,"UTF-8");
							String pivot = data.substring(0,CUT);
							System.out.println("���� ä�� :" + data);
							
							//�α��� �� idCheck ���μ���
							if(pivot.equals("login:")) {//id: id Check ��û
								boolean result = idCheck(data,CUT);
								// ��� ������
								if(result) {
									id = data.substring(CUT); // id�Ӽ��� id �߰� 
									resultSend("true");
								}else {
									resultSend("false");
								}
								String message = "[ "+id+" �α��� ��û ó�� : " + socketToChat.getRemoteSocketAddress()+" ]";
								Platform.runLater(()-> displayText(message));
							}
							
							else if(pivot.equals("joinn:")) {
								boolean result = idCheck(data,CUT);
								// ��� ������
								if(result) { // id �ߺ��Ǿ���
									resultSend("false");
								}else { // id�� �ߺ����� ����
									id = data.substring(CUT); // id�Ӽ��� id �߰� 
									ids.add(new String(id)); //Client�� ������� id������ ������Ƿ� ���ο� String��ü�� ����� �����Ѵ�. 
									resultSend("true");
								}
								String message = "[ "+id+" �α��� ��û ó�� : " + socketToChat.getRemoteSocketAddress()+" ]";
								Platform.runLater(()-> displayText(message));
								
							}
							//Ŭ���̾�Ʈ receive ������ ���� ���
							else if(pivot.equals("EXITT:")) {
								resultSend("EXIT");
							}
							
							else {
								//ȸ������ �� id �ߺ� Check ���μ���
								String message = "[ "+id+" ä�� ��û ó�� : " + socketToChat.getRemoteSocketAddress()+" ]";
								Platform.runLater(()-> displayText(message));
								
								String chat = "["+id+"] :" +data.substring(CUT);
								for(Client client : clients) client.send(chat);
							}
							
						} catch (IOException e) {
							try {
								clients.remove(Client.this);
								String message = "[ "+id+" ä�� ���� ���� : "+ socketToChat.getRemoteSocketAddress() +" ]";
								Platform.runLater(()->displayText(message));
								socketToChat.close();
								break;
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								}
							}
						}
				}
			};
			
			executorService.submit(runnable);
		}
		
		public void send(String data) {
			
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					
					try {
						byte[] bytes = data.getBytes("UTF-8");
						OutputStream outputStream = socketToChat.getOutputStream();
						outputStream.write(bytes);
						outputStream.flush();
						
					} catch (IOException e) {
						try {
							clients.remove(Client.this);
							String message = "[ ��� ���� : "+ socketToChat.getRemoteSocketAddress() +" ]";
							Platform.runLater(()->displayText(message));
							socketToChat.close();
						} catch (IOException e1) {}
					}	
				}
			};
			
			executorService.submit(runnable);
		}
		
		public void imageSend(byte[] byteImage) {
			Runnable runnable = new Runnable() {
				public void run() {
					
					OutputStream outputStream;
					try {
						outputStream = socketToImage.getOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(outputStream);
						oos.writeObject(byteImage);
						oos.flush();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			};
			
			executorService.submit(runnable);
		}
		
		public boolean idCheck(String data,int cut) { // id�� �����ϸ� true, id�� ������ false
			String id_request = data.substring(cut); //id ����
			boolean result = false;
			//id Ž��
			for(int i =0;i<ids.size();i++) {
				if(ids.get(i).equals(id_request)){
					result = true;
					break;
				}
			}
			return result;
		}
		
		public void resultSend(String result) {
			OutputStream os;
			
			try {
				byte[] bytes = result.getBytes("UTF-8");
				os = socketToChat.getOutputStream();
				os.write(bytes);
				os.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	
}
