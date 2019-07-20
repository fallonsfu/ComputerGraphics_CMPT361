package shading;

import geometry.Vertex3D;
import polygon.Polygon;

public class NullFaceShader implements FaceShader {
    public Polygon shade(Polygon polygon) {
        return polygon;
    }
}
