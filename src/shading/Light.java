package shading;

import geometry.Point3DH;
import windowing.graphics.Color;

public class Light {
    Color intensity;
    Point3DH cameraSpaceLocation;
    private double fattA;
    private double fattB;

    public Light(Color color, Point3DH csl, double A, double B) {
        intensity = color;
        cameraSpaceLocation = csl;
        fattA = A;
        fattB = B;
    }

    public double getFattA() {return fattA;}
    public double getFattB() {return fattB;}
}