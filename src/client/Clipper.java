package client;

import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

public class Clipper {

    private double near;
    private double far;
    private double top;
    private double bottom;
    private double left;
    private double right;
    private Point3DH p0 = new Point3DH(0,0,0);

    public Clipper(double xlow, double ylow, double xhigh, double yhigh, double hither, double yon) {
        near = hither;
        far = yon;
        top = yhigh;
        bottom = ylow;
        left = xlow;
        right = xhigh;
    }

    public Polygon clip3D(Polygon polygon) {
        Polygon output = polygon;
        output = clipFar(output);
        output = clipNear(output);
        output = clipTop(output);
        output = clipLeft(output);
        output = clipBottom(output);
        output = clipRight(output);
        return output;
    }

    private Polygon clipTop(Polygon output) {
        Polygon input = output;
        output = Polygon.makeEmpty();
        Point3DH p1 = new Point3DH(left, top, -1);
        Point3DH p2 = new Point3DH(right, top, -1);
        for(int i = 0; i < input.length(); i++) {
            Vertex3D first = input.get(i);
            Vertex3D second = input.get(i + 1);
            if(second.getY() <= -(top * second.getZ())) {
                if(first.getY() > -(top * first.getZ()))
                    output.add(intersection(first, second, p1, p2));
                output.add(second);
            }
            else if(first.getY() <= -(top * first.getZ()))
                output.add(intersection(first, second, p1, p2));
        }
        return output;
    }

    private Polygon clipBottom(Polygon output) {
        Polygon input = output;
        output = Polygon.makeEmpty();
        Point3DH p1 = new Point3DH(left, bottom, -1);
        Point3DH p2 = new Point3DH(right, bottom, -1);
        for (int i = 0; i < input.length(); i++) {
            Vertex3D first = input.get(i);
            Vertex3D second = input.get(i + 1);
            if (second.getY() >= -(bottom * second.getZ())) {
                if (first.getY() < -(bottom * first.getZ()))
                    output.add(intersection(first, second, p1, p2));
                output.add(second);
            }
            else if(first.getY() >= -(bottom * first.getZ()))
                output.add(intersection(first, second, p1, p2));
        }
        return output;
    }

    private Polygon clipLeft(Polygon output) {
        Polygon input = output;
        output = Polygon.makeEmpty();
        Point3DH p1 = new Point3DH(left, top, -1);
        Point3DH p2 = new Point3DH(left, bottom, -1);
        for (int i = 0; i < input.length(); i++) {
            Vertex3D first = input.get(i);
            Vertex3D second = input.get(i + 1);
            if (second.getX() >= -(left * second.getZ())) {
                if (first.getX() < -(left * first.getZ()))
                    output.add(intersection(first, second, p1, p2));
                output.add(second);
            }
            else if(first.getX() >= -(left * first.getZ()))
                output.add(intersection(first, second, p1, p2));
        }
        return output;
    }

    private Polygon clipRight(Polygon output) {
        Polygon input = output;
        output = Polygon.makeEmpty();
        Point3DH p1 = new Point3DH(right, top, -1);
        Point3DH p2 = new Point3DH(right, bottom, -1);
        for (int i = 0; i < input.length(); i++) {
            Vertex3D first = input.get(i);
            Vertex3D second = input.get(i + 1);
            if (second.getX() <= -(right * second.getZ())) {
                if (first.getX() > -(right * first.getZ()))
                    output.add(intersection(first, second, p1, p2));
                output.add(second);
            }
            else if(first.getX() <= -(right * first.getZ()))
                output.add(intersection(first, second, p1, p2));
        }
        return output;
    }

    private Vertex3D intersection(Vertex3D first, Vertex3D second, Point3DH p1, Point3DH p2) {
        Point3DH vector = second.subtract(first).getPoint3D();
        Point3DH p01 = p1.subtract(p0);
        Point3DH p02 = p2.subtract(p0);
        Point3DH normal = new Point3DH(p01.getY()*p02.getZ() - p01.getZ()*p02.getY(),
                p01.getZ()*p02.getX() - p01.getX()*p02.getZ(),
                p01.getX()*p02.getY() - p01.getY()*p02.getX());
        Point3DH a = first.getPoint3D().subtract(p0);
        double t = (normal.getX()*a.getX() + normal.getY()*a.getY() +  normal.getZ()*a.getZ()) /
                -(vector.getX()*normal.getX() + vector.getY()*normal.getY() + vector.getZ()*normal.getZ());
        Point3DH intersection = first.getPoint3D().add(vector.scale(t));
        Vertex3D newVertex = new Vertex3D(intersection, first.getColor());
        if (first.hasNormal)
            newVertex.setNormal(first.getNormal());
        return newVertex;
    }

    private Polygon clipFar(Polygon output) {
        Polygon input = output;
        output = Polygon.makeEmpty();
        for(int i = 0; i < input.length(); i++) {
            Vertex3D first = input.get(i);
            Vertex3D second = input.get(i+1);
            if(second.getZ() >= far) {
                if(first.getZ() < far)
                    output.add(intersectionWithZ(first, second, far));
                output.add(second);
            }
            else if(first.getZ() >= far)
                output.add(intersectionWithZ(first, second, far));
        }
        return output;
    }

    private Polygon clipNear(Polygon output) {
        Polygon input = output;
        output = Polygon.makeEmpty();
        for(int i = 0; i < input.length(); i++) {
            Vertex3D first = input.get(i);
            Vertex3D second = input.get(i+1);
            if(second.getZ() <= near) {
                if(first.getZ() > near)
                    output.add(intersectionWithZ(first, second, near));
                output.add(second);
            }
            else if(first.getZ() <= near)
                output.add(intersectionWithZ(first, second, near));
        }
        return output;
    }

    private Vertex3D intersectionWithZ(Vertex3D first, Vertex3D second, double face) {
        double dz_vertex = second.getZ() - first.getZ();
        double dx_vertex = second.getX() - first.getX();
        double dy_vertex = second.getY() - first.getY();
        double dz_face = face - first.getZ();
        double x = first.getX() + dx_vertex * (dz_face / dz_vertex);
        double y = first.getY() + dy_vertex * (dz_face / dz_vertex);
        Vertex3D newVertex = new Vertex3D(x, y, face, first.getColor());
        if (first.hasNormal)
            newVertex.setNormal(first.getNormal());
        return newVertex;
    }
}
