package shading;

import client.interpreter.SimpInterpreter;
import geometry.Point3DH;
import geometry.Vertex3D;
import windowing.graphics.Color;

import java.util.ArrayList;

public class Lighting {

    public Color ambientLight;
    private ArrayList<Light> lights = new ArrayList<>();
    private Point3DH eyeLocation = new Point3DH(0, 0, 0);

    public void addLight(Light light) {
        lights.add(light);
    }

    public void setAmbientLight(Color ambientLight) {
        this.ambientLight = ambientLight;
    }

    public Color light(Vertex3D cameraSpacePoint, Color kDiffuse, double kSpecular, double specularExponent) {
        Color diffuse = kDiffuse.multiply(ambientLight);
        Color lightSum = new Color(0, 0, 0);
        for(Light light: lights) {

            Point3DH N = cameraSpacePoint.getNormal();
            Point3DH vector = light.cameraSpaceLocation.subtract(cameraSpacePoint.getCameraPoint());
            Point3DH L = vector.normalize();
            Point3DH V = eyeLocation.subtract(cameraSpacePoint.getCameraPoint()).normalize();
            Point3DH R = (N.scale(N.dotProduct(L) * 2)).subtract(L).normalize();
            double N_L = N.dotProduct(L) >= 0 ? N.dotProduct(L) : 0;
            double V_R = V.dotProduct(R) >= 0 ? V.dotProduct(R) : 0;

            double d = vector.getLength();
            double fatt = 1 / (light.getFattA() + light.getFattB() * d);
            double component = kSpecular * Math.pow(V_R, specularExponent);
            Color partialColor = kDiffuse.scale(N_L).add(new Color(component, component, component));
            Color reflection = (light.intensity.scale(fatt)).multiply(partialColor);

            lightSum = lightSum.add(reflection);
        }
        Color result = lightSum.add(diffuse);
        return result;
    }
}
