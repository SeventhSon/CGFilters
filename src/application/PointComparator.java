package application;

import java.util.Comparator;

import javafx.geometry.Point2D;

public class PointComparator implements Comparator<Point2D>{

	@Override
	public int compare(Point2D o1, Point2D o2) {
		return (o1.getX()<o2.getX() ? -1 : (o1.getX()==o2.getX() ? 0 : 1));
	}
	
}
