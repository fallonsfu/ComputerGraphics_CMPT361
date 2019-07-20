package shading;

import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

public class FlatPixelShader implements PixelShader{

    public Color shade(Polygon polygon, Vertex3D current) {
        return polygon.getFaceColor();
    }
}
