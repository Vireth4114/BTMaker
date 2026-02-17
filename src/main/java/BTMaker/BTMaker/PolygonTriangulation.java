package BTMaker.BTMaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PolygonTriangulation {
    static class Point {
        double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
    }

    static class Triangle {
        Point a, b, c;
        Triangle(Point a, Point b, Point c) {
            this.a = a; this.b = b; this.c = c;
        }
    }

    private static double orient(Point a, Point b, Point c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    }

    private static double polygonArea(List<Point> poly) {
        double s = 0;
        int n = poly.size();
        for (int i = 0; i < n; i++) {
            Point p1 = poly.get(i);
            Point p2 = poly.get((i+1) % n);
            s += p1.x * p2.y - p2.x * p1.y;
        }
        return s / 2.0;
    }

    private static boolean pointInTriangle(Point p, Point a, Point b, Point c) {
        double o1 = orient(p, a, b);
        double o2 = orient(p, b, c);
        double o3 = orient(p, c, a);
        boolean hasNeg = (o1 < 0) || (o2 < 0) || (o3 < 0);
        boolean hasPos = (o1 > 0) || (o2 > 0) || (o3 > 0);
        return !(hasNeg && hasPos);
    }

    public static int[] triangulate(int[] xPoints, int[] yPoints) {
    	List<Point> poly = new ArrayList<>();
    	for (int i = 0; i < xPoints.length; i++) {
    		poly.add(new Point(xPoints[i], yPoints[i]));
    	}
    	List<Triangle> triangles = triangulate(poly);
    	int[] result = new int[triangles.size()*3];
    	for (int i = 0; i < triangles.size(); i++) {
    		result[3*i] = poly.indexOf(triangles.get(i).a);
    		result[3*i+1] = poly.indexOf(triangles.get(i).b);
    		result[3*i+2] = poly.indexOf(triangles.get(i).c);
    	}
    	return result;
    }

    private static List<Triangle> triangulate(List<Point> poly) {
        int n = poly.size();
        if (n < 3) return Collections.emptyList();

        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < n; i++) idx.add(i);

        if (polygonArea(poly) > 0) {
            Collections.reverse(idx);
        }

        List<Triangle> triangles = new ArrayList<>();

        while (idx.size() > 3) {
            boolean earFound = false;

            for (int k = 0; k < idx.size(); k++) {
                int iPrev = idx.get((k - 1 + idx.size()) % idx.size());
                int i = idx.get(k);
                int iNext = idx.get((k + 1) % idx.size());

                Point a = poly.get(iPrev);
                Point b = poly.get(i);
                Point c = poly.get(iNext);
                if (orient(a, b, c) >= -1e-12) continue;

                boolean inside = false;
                for (int j : idx) {
                    if (j == iPrev || j == i || j == iNext) continue;
                    if (pointInTriangle(poly.get(j), a, b, c)) {
                        inside = true;
                        break;
                    }
                }
                if (inside) continue;

                triangles.add(new Triangle(a, b, c));
                idx.remove(k);
                earFound = true;
                break;
            }

            if (!earFound) {
                break;
            }
        }

        if (idx.size() == 3) {
            triangles.add(new Triangle(poly.get(idx.get(0)),
                    poly.get(idx.get(1)),
                    poly.get(idx.get(2))));
        }

        return triangles;
    }
}
