package application;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EditorController implements Initializable {

	@FXML
	private Canvas canvas;

	private EventDispatcher eventBus;
	
	private List<Point2D> points;
	
	private GraphicsContext g;
	
	@FXML
	public void ok(ActionEvent e){
		eventBus.publish(new CustomFilterEvent(calculateFunction()));
		((Stage)canvas.getScene().getWindow()).close();
	}

	private int[] calculateFunction() {
		int[] arr = new int[256];
		float y = 0;
		int x = 0;
		double rate;
		for(int i=0;i<points.size();i++){
			rate = (256-points.get(i).getY()-y)/(points.get(i).getX()-x);
			for(;x<points.get(i).getX();x++){
				arr[x] = (int) y;
				y+=rate;
			}
		}
		rate = (256-y)/(256-x);
		for(;x<256;x++){
			arr[x]= (int) y;
			y+=rate;
		}
		for(int i=0;i<256;i++)
			System.out.println(i+" : "+arr[i]);
		return arr;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		eventBus = EventDispatcher.getInstance();
		points = new ArrayList<Point2D>();
		g = canvas.getGraphicsContext2D();
		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent e) {
                                points.add(new Point2D(e.getX(),e.getY()));
                                drawFunction();
                        }
                });
	}
	
	public void drawFunction(){
		if(points!=null&&!points.isEmpty()){
			Collections.sort(points, new PointComparator());
			g.clearRect(0, 0, 256, 256);
			g.setStroke(Color.BLUE);
			g.beginPath();
			g.moveTo(0, 256);
			for(Point2D p : points){
				g.lineTo(p.getX(), p.getY());
				g.fillOval(p.getX(), p.getY(), 5, 5);
			}
			g.lineTo(256, 0);
			g.stroke();
		}
	}

}
