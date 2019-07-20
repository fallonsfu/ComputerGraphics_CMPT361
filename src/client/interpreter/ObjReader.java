package client.interpreter;

import java.util.ArrayList;
import java.util.List;

import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

class ObjReader {
	private static final char COMMENT_CHAR = '#';
	private static final int NOT_SPECIFIED = -1;

	private class ObjVertex {
		private int vertexIndex;
		private int textureIndex;
		private int normalIndex;

		public ObjVertex(int vertex, int texture, int normal) {
			vertexIndex = vertex;
			textureIndex = texture;
			normalIndex = normal;
		}
		public int getVertex() {return vertexIndex;}
		public int getTexture() {return textureIndex;}
		public int getNormal() {return normalIndex;}
	}
	private class ObjFace extends ArrayList<ObjVertex> {
		private static final long serialVersionUID = -4130668677651098160L;
	}
	private LineBasedReader reader;

	private List<Vertex3D> objVertices;
	private List<Point3DH> objNormals;
	private List<ObjFace> objFaces;
	private List<Vertex3D> transformedVertices;
	private List<Point3DH> transformedNormals;

	private Color color;

	ObjReader(String filename, Color defaultColor) {
		reader = new LineBasedReader("simp/" + filename);
		objVertices = new ArrayList<>();
		transformedVertices = new ArrayList<>();
		transformedNormals = new ArrayList<>();
		objNormals = new ArrayList<>();
		objFaces = new ArrayList<>();
		color = Color.WHITE;
	}

	public void render(SimpInterpreter interpreter) {
		transformVertices(interpreter);
		transformNormals(interpreter);
		color = interpreter.defaultColor;

		for(ObjFace face: objFaces) {
			Polygon polygon = polygonForFace(face);
			Polygon clipped = interpreter.clipper.clip3D(polygon);
			Vertex3D[] vertices = new Vertex3D[clipped.length()];
			for(int i = 0; i < clipped.length(); i++) {
				Vertex3D vertex = interpreter.transformToScreen(interpreter.project(clipped.get(i)));
				vertices[i] = vertex;
			}
			if(vertices.length >= 3)
				interpreter.render(vertices);
		}
	}

	private void transformVertices(SimpInterpreter interpreter) {
		for(Vertex3D vertex: objVertices) {
			Vertex3D transformed = interpreter.transformToCamera(vertex);
			transformedVertices.add(transformed);
		}
	}

	private void transformNormals(SimpInterpreter interpreter) {
		for(Point3DH normal: objNormals) {
			Point3DH transformed = interpreter.transformNormal(normal);
			transformedNormals.add(transformed);
		}
	}

	private Polygon polygonForFace(ObjFace face) {
		Polygon result = Polygon.makeEmpty();
		for(ObjVertex objVertex: face) {
			int vertexIndex = objVertex.getVertex();
			int normalIndex = objVertex.getNormal();
			Vertex3D vertex = transformedVertices.get(vertexIndex);
			if (normalIndex != -100000) {
				Point3DH normal = transformedNormals.get(normalIndex);
				vertex.setNormal(normal);
			}
			vertex.setColor(color);
			result.add(vertex);
		}
		return result;
	}

	public void read() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretObjLine(line);
		}
	}
	private void interpretObjLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretObjCommand(tokens);
			}
		}
	}

	private void interpretObjCommand(String[] tokens) {
		switch(tokens[0]) {
		case "v" :
		case "V" :
			interpretObjVertex(tokens);
			break;
		case "vn":
		case "VN":
			interpretObjNormal(tokens);
			break;
		case "f":
		case "F":
			interpretObjFace(tokens);
			break;
		default:	// do nothing
			break;
		}
	}

	private void interpretObjFace(String[] tokens) {
		ObjFace face = new ObjFace();

		for(int i = 1; i<tokens.length; i++) {
			String token = tokens[i];
			String[] subtokens = token.split("/");
			int vertexIndex = objIndex(subtokens, 0, objVertices.size());
			int textureIndex = objIndex(subtokens, 1, 0);
			int normalIndex = objIndex(subtokens, 2, objNormals.size());

			ObjVertex objVertex = new ObjVertex(vertexIndex, textureIndex, normalIndex);
			face.add(objVertex);
		}
		objFaces.add(face);
	}

	private int objIndex(String[] subtokens, int tokenIndex, int baseForNegativeIndices) {
		if (tokenIndex >= subtokens.length)
			return -100000;
		if (subtokens[tokenIndex].equals(""))
			return -100000;
		int index = Integer.parseInt(subtokens[tokenIndex]);
		if(index < 0)
			index = baseForNegativeIndices + index;
		else
			index = index - 1;
		return index;
	}

	private void interpretObjNormal(String[] tokens) {
		int numArgs = tokens.length - 1;
		if(numArgs != 3) {
			throw new BadObjFileException("vertex normal with wrong number of arguments : " + numArgs + ": " + tokens);
		}
		Point3DH normal = SimpInterpreter.interpretPoint(tokens, 1);

		objNormals.add(normal);
	}
	private void interpretObjVertex(String[] tokens) {
		int numArgs = tokens.length - 1;
		Point3DH point = objVertexPoint(tokens, numArgs);
		Color color = objVertexColor(tokens, numArgs);

		objVertices.add(new Vertex3D(point, color));
	}

	private Color objVertexColor(String[] tokens, int numArgs) {
		if(numArgs == 6) {
			System.out.println("4");
			return SimpInterpreter.interpretColor(tokens, 4);
		}
		if(numArgs == 7) {
			System.out.println("5");
			return SimpInterpreter.interpretColor(tokens, 5);
		}
		return color;
	}

	private Point3DH objVertexPoint(String[] tokens, int numArgs) {
		if(numArgs == 3 || numArgs == 6) {
			return SimpInterpreter.interpretPoint(tokens, 1);
		}
		else if(numArgs == 4 || numArgs == 7) {
			return SimpInterpreter.interpretPointWithW(tokens, 1);
		}
		throw new BadObjFileException("vertex with wrong number of arguments : " + numArgs + ": " + tokens);
	}
}