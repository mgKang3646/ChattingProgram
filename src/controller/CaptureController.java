package controller;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;

public class CaptureController implements Initializable {
	
	@FXML ImageView imageview;
	@FXML AnchorPane parent;
	private Canvas canvas;
	private GraphicsContext gc;
	double x1;
	double y1;
	double x2;
	double y2;
	double x3;
	double y3;
	double leftX; //좌상단 x
	double leftY; //좌상단 y
	double width;
	double height;
	float linewidth = 0.5f;
	Line line1;
	Line line2;
	Line line3;
	Line line4;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		line1 = new Line(); // y는 시작지점과 끝지점이 같음
		line2 = new Line(); // 
		line3 = new Line(); // x는 시작지점과 끝지점이 같음
		line4 = new Line(); // 
		
		line1.setStrokeWidth(linewidth);
        line1.setStrokeType(StrokeType.OUTSIDE);
        line1.setStroke(Color.RED);
        line2.setStrokeWidth(linewidth);
        line2.setStrokeType(StrokeType.OUTSIDE);
        line2.setStroke(Color.RED);
        line3.setStrokeWidth(linewidth);
        line3.setStrokeType(StrokeType.OUTSIDE);
        line3.setStroke(Color.RED);
        line4.setStrokeWidth(linewidth);
        line4.setStrokeType(StrokeType.OUTSIDE);
        line4.setStroke(Color.RED);
        
		getCaptureSize();
	}
	
	public void setFullPage(Canvas canvas,GraphicsContext gc) throws AWTException, IOException {
	
		 this.canvas = canvas;
		 this.gc = gc;
		 
		 imageview.setFitWidth(Toolkit.getDefaultToolkit().getScreenSize().width);
		 imageview.setFitHeight(Toolkit.getDefaultToolkit().getScreenSize().height);
		 Robot robot = new Robot();
         Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
         // 전체 화면 사이즈를 상자안에 저장
         // 가로 세로를 지정해줄수 있음.
         //Rectangle (int x, int y, int width, int height)
         BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
         Image image = SwingFXUtils.toFXImage(bufferedImage, null); // AWT => JAVAFX
         imageview.setImage(image);
         parent.setSnapToPixel(true);
         parent.getChildren().addAll(line1,line2,line3,line4);
	}
	
	private void getCaptureSize() {
		
		parent.setOnMousePressed((event)->{
			x1= (int)event.getSceneX();
			y1= (int)event.getSceneY();
			System.out.println("마우스 시작");
			System.out.println("x1 : " + x1);
			System.out.println("y1 : " + y1);
			//다시 클릭되면 보이게 만들기
			line1.setVisible(true);
			line2.setVisible(true);
			line3.setVisible(true);
			line4.setVisible(true);
			
			line1.setStartX(x1); 
			line1.setStartY(y1); 
			line1.setEndY(y1);
			
			line2.setStartY(y1);
			
			line3.setStartX(x1); 
			line3.setStartY(y1); 
			line3.setEndX(x1);
			
			line4.setStartX(x1);
			
		});
		
		parent.setOnMouseDragged((event)->{
			x2= event.getSceneX();
			y2= event.getSceneY();
			
			line1.setEndX(x2);
			
			line2.setStartX(x2);
			line2.setEndX(x2);
			line2.setEndY(y2);
			
			line3.setEndY(y2);
			
			line4.setEndX(x2);
			line4.setStartY(y2);
			line4.setEndY(y2);
		});
		
		parent.setOnMouseReleased((event)->{
			
			try {
				x3 = event.getSceneX();
				y3 = event.getSceneY();
				System.out.println("드래그 끝");
				System.out.println("x3 : "+x3);
				System.out.println("y3 : "+ y3);
				
				if(x2>=x1) {
					leftX = x1;
					leftY = y1;
					width = x3 - x1;
					height = y3 - y1;
					System.out.println("width : " + width);
					System.out.println("height : " + height);
					this.setCapturedPage();
				}else {
					leftX = x3;
					leftY = y3;
					width = x1 - x3;
					height = y1 - y3;
					System.out.println("width : " + width);
					System.out.println("height : " + height);
					this.setCapturedPage();
				}
				//캡처 끝나면 안 보이게 만들기
				line1.setVisible(false);
				line2.setVisible(false);
				line3.setVisible(false);
				line4.setVisible(false);

				
				
				
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		});
		
		
	}
	
	private void setCapturedPage() throws AWTException {
	
		Robot robot = new Robot();
		Rectangle rc = new Rectangle((int)leftX+8,(int)leftY+8,(int)(width-8),(int)(height-8));
		BufferedImage bi = robot.createScreenCapture(rc);
		Image image = SwingFXUtils.toFXImage(bi, null);
		gc.drawImage(image,0,0,canvas.getWidth(),canvas.getHeight());
		
		Stage stage = (Stage)(imageview.getScene().getWindow());
		stage.close();
		
		
	}
	
	

	

}
