package geometry;

import windowing.graphics.Color;

import java.util.ArrayList;

public class Vertex3D implements Vertex {
	protected Point3DH point;
	protected Point3DH cameraPoint;
	protected Color color;
	public InterpolantList interpolants;
	public boolean hasNormal;
	protected Point3DH normal;
//	protected Halfplane3DH normal;

	public class InterpolantList {
		public boolean color = false;
		public boolean cameraPoint = false;
		public boolean normal = false;
	}
	
	public Vertex3D(Point3DH point, Color color) {
		super();
		this.point = point;
		this.cameraPoint = point;
		this.color = color;
		hasNormal = false;
		interpolants = new InterpolantList();
	}

	public Vertex3D(Point3DH point, Point3DH cameraPoint, Color color) {
		super();
		this.point = point;
		this.cameraPoint = cameraPoint;
		this.color = color;
		hasNormal = false;
		interpolants = new InterpolantList();
	}

	public Vertex3D(double x, double y, double z, Color color) {
		this(new Point3DH(x, y, z), color);
		hasNormal = false;
		interpolants = new InterpolantList();
	}

	public Vertex3D() {
	}
	public double getX() {
		return point.getX();
	}
	public double getY() {
		return point.getY();
	}
	public double getZ() {
		return point.getZ();
	}
	public double getCameraSpaceZ() {
		return getZ();
	}
	public Point getPoint() {
		return point;
	}
	public Point3DH getPoint3D() {
		return point;
	}
	public Point3DH getCameraPoint() {return cameraPoint;}

	public Point3DH getNormal() {
		return normal;
	}

	public void setNormal(Point3DH normal) {
		this.normal = normal;
		hasNormal = true;
	}

	public void setInterpolants(boolean color, boolean cameraPoint, boolean normal) {
		interpolants.color = color;
		interpolants.cameraPoint = cameraPoint;
		interpolants.normal = normal;
	}
	
	public int getIntX() {
		return (int) Math.round(getX());
	}
	public int getIntY() {
		return (int) Math.round(getY());
	}
	public int getIntZ() {
		return (int) Math.round(getZ());
	}
	
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) { this.color = color; }
	
	public Vertex3D rounded() {
		return new Vertex3D(point.round(), color);
	}
	public Vertex3D add(Vertex other) {
		Vertex3D other3D = (Vertex3D)other;
		return new Vertex3D(point.add(other3D.getPoint()),
				            cameraPoint.add(other3D.getCameraPoint()),
				            color.add(other3D.getColor()));
	}
	public Vertex3D subtract(Vertex other) {
		Vertex3D other3D = (Vertex3D)other;
		return new Vertex3D(point.subtract(other3D.getPoint()),
				            cameraPoint.subtract(other3D.getCameraPoint()),
				            color.subtract(other3D.getColor()));
	}
	public Vertex3D scale(double scalar) {
		return new Vertex3D(point.scale(scalar),
							cameraPoint.scale(scalar),
				            color.scale(scalar));
	}
	public Vertex3D replacePoint(Point3DH newPoint) {
		return new Vertex3D(newPoint, color);
	}
	public Vertex3D replaceColor(Color newColor) {
		return new Vertex3D(point, newColor);
	}
	public Vertex3D euclidean() {
		Point3DH euclidean = getPoint3D().euclidean();
		return replacePoint(euclidean);
	}
	
	public String toString() {
		return "(" + getX() + ", " + getY() + ", " + getZ() + ", " + getColor().toIntString() + ")";
	}
	public String toIntString() {
		return "(" + getIntX() + ", " + getIntY() + getIntZ() + ", " + ", " + getColor().toIntString() + ")";
	}

}
