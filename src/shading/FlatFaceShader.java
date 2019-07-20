package shading;

import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

public class FlatFaceShader implements FaceShader {

    public Lighting lighting;

    public FlatFaceShader(Lighting lighting) {
        this.lighting = lighting;
    }

    public Polygon shade(Polygon polygon) {
        Vertex3D center = getCenterPoint(polygon);
        Point3DH normal;
        if(polygon.get(0).hasNormal)
            normal = getAverageNormal(polygon);
        else
            normal = getFaceNormal(polygon.get(0).getCameraPoint(), polygon.get(1).getCameraPoint(), polygon.get(2).getCameraPoint());
        center.setNormal(normal);
        Color faceColor = lighting.light(center, center.getColor(), polygon.getkSpecular(), polygon.getSpecularExponent());
        polygon.setFaceColor(faceColor);
        return polygon;
    }

    private Vertex3D getCenterPoint(Polygon polygon) {
        Vertex3D vertexSum = polygon.get(0);
        for(int i = 1; i < polygon.length(); i++)
            vertexSum = vertexSum.add(polygon.get(i));
        return vertexSum.scale(1.0 / polygon.length());
    }

    private Point3DH getAverageNormal(Polygon polygon) {
        Point3DH normalSum = polygon.get(0).getNormal();
        for(int i = 1; i < polygon.length(); i++)
            normalSum = normalSum.add(polygon.get(i).getNormal());
        return normalSum.scale(1.0 / polygon.length()).normalize();
    }

    private Point3DH getFaceNormal(Point3DH p0, Point3DH p1, Point3DH p2) {
        Point3DH p01 = p1.subtract(p0);
        Point3DH p02 = p2.subtract(p0);
        Point3DH normal = new Point3DH(p01.getY()*p02.getZ() - p01.getZ()*p02.getY(),
                p01.getZ()*p02.getX() - p01.getX()*p02.getZ(),
                p01.getX()*p02.getY() - p01.getY()*p02.getX());
        return normal.normalize();
    }
}
