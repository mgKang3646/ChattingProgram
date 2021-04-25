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
	ArrayList<String> ids= new ArrayList<String>(); // 총 회원
	Vector<Client> clients = new Vector<Client>(); // 현재 접속 중인 회원
	
	public static final int CUT = 6; // idCheck시 substring 자를 구간
	public static final int JOIN = 5; // idCheck시 substring 자를 구간
	
	
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
		ArrayList<String> idConnect = new ArrayList<String>(); // 현재 접속 중인 id
		ArrayList<String> idNonConnect = new ArrayList<String>(); // 현재 비접속 중인 id
		
		for(int i=0;i<clients.size();i++) {
			idConnect.add(clients.get(i).id);
		}
		
		for(int i=0; i<ids.size(); i++) {
			System.out.println();
			idConnect.add(ids.get(i)); //보초법 
			int j;
			for(j=0; j<idConnect.size();j++) {
				if(ids.get(i).equals(idConnect.get(j))) break;
			}
			
			if(j==idConnect.size()-1) {
				idNonConnect.add(ids.get(i));
			}
			idConnect.remove(idConnect.size()-1); // 보초상수 제거
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
		
		if(startButton.getText().equals("서버 시작" )) {
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
		
		//서버 - 클라이언트 연걸 작업
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				
				Platform.runLater(()->{
					displayText("[ 서버 시작 ]");
					startButton.setText("서버 중지");
				});
				
				int count=0; // 채팅 소켓, 이미지 소켓 구분용
				Socket tmpSocket = null;  // 채팅 소켓 임시 보관용
				while(true){	
					try {
							Socket socket = serverSocket.accept();
							//============blocking================
							
							System.out.println("카운트 : "+count);
							
							if(count%2==1) { //이미지 소켓까지 연결 완료되면 클라이언트 생성
								
								Platform.runLater(()->{
									String message = "[ 이미지 소켓 연결 성공 : " + socket.getRemoteSocketAddress() + " ]";
									displayText(message);
								});
								
								Client client = new Client(tmpSocket, socket); 
								clients.add(client); // 해당 클라이언트를 서버와 연결된 클라이언트 목록에 추가

								Platform.runLater(()->{
									String message = "[ 연결 개수 : " + clients.size() + " ]";
									displayText(message);
								});
								
							}else { // %2가 0인 경우 : 채팅용 소켓 임시저장
								tmpSocket = socket; 
								Platform.runLater(()->{
									String message = "[ 채팅 소켓 연결 성공 : " + socket.getRemoteSocketAddress() + " ]";
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
					System.out.println("클라이언트 소켓 종료과정에서 오류");
				}
			}
			
			//iterator.remove();
			
			try {
				if(serverSocket != null && !serverSocket.isClosed()) serverSocket.close(); // 서버소켓 닫기
				if(executorService != null && !executorService.isShutdown()) executorService.shutdown(); // 스레드풀 종료
				
				Platform.runLater(()->{
					displayText("[ 서버 멈춤 ]");
					startButton.setText("서버 시작");
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
								
								String message = "[ "+id+" 이미지 요청 처리 : " + socketToImage.getRemoteSocketAddress()+" ]";
								Platform.runLater(()-> displayText(message));
								
								for(Client client : clients) {
									client.imageSend(bytes);
								}
							} catch (Exception e) {
								try {
									clients.remove(Client.this);
									String message = "[ "+id+" 이미지 소켓 종료 : " + socketToImage.getRemoteSocketAddress() + " ]";
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
							System.out.println("들어온 채팅 :" + data);
							
							//로그인 시 idCheck 프로세스
							if(pivot.equals("login:")) {//id: id Check 요청
								boolean result = idCheck(data,CUT);
								// 결과 보내기
								if(result) {
									id = data.substring(CUT); // id속성에 id 추가 
									resultSend("true");
								}else {
									resultSend("false");
								}
								String message = "[ "+id+" 로그인 요청 처리 : " + socketToChat.getRemoteSocketAddress()+" ]";
								Platform.runLater(()-> displayText(message));
							}
							
							else if(pivot.equals("joinn:")) {
								boolean result = idCheck(data,CUT);
								// 결과 보내기
								if(result) { // id 중복되었음
									resultSend("false");
								}else { // id가 중복되지 않음
									id = data.substring(CUT); // id속성에 id 추가 
									ids.add(new String(id)); //Client가 사라지면 id변수는 사라지므로 새로운 String객체를 만들어 저장한다. 
									resultSend("true");
								}
								String message = "[ "+id+" 로그인 요청 처리 : " + socketToChat.getRemoteSocketAddress()+" ]";
								Platform.runLater(()-> displayText(message));
								
							}
							//클라이언트 receive 스레드 종료 명령
							else if(pivot.equals("EXITT:")) {
								resultSend("EXIT");
							}
							
							else {
								//회원가입 시 id 중복 Check 프로세스
								String message = "[ "+id+" 채팅 요청 처리 : " + socketToChat.getRemoteSocketAddress()+" ]";
								Platform.runLater(()-> displayText(message));
								
								String chat = "["+id+"] :" +data.substring(CUT);
								for(Client client : clients) client.send(chat);
							}
							
						} catch (IOException e) {
							try {
								clients.remove(Client.this);
								String message = "[ "+id+" 채팅 소켓 종료 : "+ socketToChat.getRemoteSocketAddress() +" ]";
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
							String message = "[ 통신 오류 : "+ socketToChat.getRemoteSocketAddress() +" ]";
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
		
		public boolean idCheck(String data,int cut) { // id가 존재하면 true, id가 없으면 false
			String id_request = data.substring(cut); //id 추출
			boolean result = false;
			//id 탐색
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
