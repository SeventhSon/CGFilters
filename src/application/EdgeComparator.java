package application;

import java.util.Comparator;

public class EdgeComparator implements Comparator<Edge>{
 
    @Override
    public int compare(Edge o1, Edge o2) {
        return (o1.ymin>o2.ymin ? -1 : (o1.ymin==o2.ymin ? 0 : 1));
    }
} 