package shading;

import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

public class InterpolatingPixelShader implements PixelShader {

    public Lighting lighting;

    public InterpolatingPixelShader(Lighting lighting) {
        this.lighting = lighting;
    }

    public Color shade(Polygon polygon, Vertex3D current) {

        Color pixelColor = lighting.light(current, current.getColor(), polygon.getkSpecular(), polygon.getSpecularExponent());
        return pixelColor;
    }
}
