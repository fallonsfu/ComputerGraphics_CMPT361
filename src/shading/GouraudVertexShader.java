package shading;

import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

public class GouraudVertexShader implements VertexShader{

    public Lighting lighting;

    public GouraudVertexShader(Lighting lighting) {
        this.lighting = lighting;
    }

    public Vertex3D shade(Polygon polygon, Vertex3D vertex) {
        if (!vertex.hasNormal) {
            Vertex3D v1 = polygon.get(0);
            Vertex3D v2 = polygon.get(1);
            Vertex3D v3 = polygon.get(2);
            Point3DH normal = getFaceNormal(v1.getCameraPoint(), v2.getCameraPoint(), v3.getCameraPoint());
            vertex.setNormal(normal);
        }
        Color vertexColor = lighting.light(vertex, vertex.getColor(), polygon.getkSpecular(), polygon.getSpecularExponent());
        vertex.setColor(vertexColor);
        vertex.setInterpolants(true, false, false);
        return vertex;
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
