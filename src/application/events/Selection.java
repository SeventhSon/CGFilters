package application.events;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public class Selection {
    public enum SelectionType {SET, CLEAR}

    private Rectangle2D area;
    private Point2D start;
    private Point2D stop;
    private SelectionType type;

    public Selection(){
        this.type = SelectionType.CLEAR;
    }

    public Selection(Point2D start, Point2D stop) {
        this.type = SelectionType.SET;
        this.start = start;
        this.stop = stop;

        this.area = getRect(start, stop);
    }

    public SelectionType getType() {
        return type;
    }

    public Rectangle2D getArea() {
        return area;
    }

    public Point2D getStart() {
        return start;
    }

    public Point2D getStop() {
        return stop;
    }

    private Rectangle2D getRect(Point2D p1, Point2D p2) {
        return new Rectangle2D(
                Math.min(p1.getX(), p2.getX()),
                Math.min(p1.getY(), p2.getY()),
                Math.abs(p1.getX() - p2.getX()),
                Math.abs(p1.getY() - p2.getY())
        );
    }

}
