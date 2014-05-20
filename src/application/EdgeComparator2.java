package application;

import java.util.Comparator;

public class EdgeComparator2 implements Comparator<Edge>{
 
    @Override
    public int compare(Edge o1, Edge o2) {
        return (o1.x<o2.x ? -1 : (o1.x==o2.x ? 0 : 1));
    }
} 