package polygon;

import geometry.Point3DH;
import geometry.Vertex;
import geometry.Vertex3D;
import shading.FaceShader;
import shading.FlatFaceShader;
import shading.PixelShader;
import shading.VertexShader;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class FilledPolygonRenderer implements PolygonRenderer{

    @Override
    public void drawPolygon(Polygon polygon, Drawable drawable, FaceShader faceShader, VertexShader vertexShader, PixelShader pixelShader) {

        polygon = faceShader.shade(polygon);
        Vertex3D[] vertices = new Vertex3D[polygon.length()];
        for (int i = 0; i < polygon.length(); i++) {
            Vertex3D vertex = vertexShader.shade(polygon, polygon.get(i));
            vertices[i] = vertex;
        }

        for (int i = 1; i < vertices.length - 1; i++) {
            Polygon triangle = Polygon.make(vertices[0], vertices[i], vertices[i + 1]);
            triangle.setFaceColor(polygon.getFaceColor());
            triangle.setSpecular(polygon.getkSpecular(), polygon.getSpecularExponent());

            if(vertices[0].interpolants.color && vertices[0].interpolants.normal)
                polygonFilling_COLOR_NORMAL(triangle, drawable, pixelShader);
            else if(vertices[0].interpolants.color)
                polygonFilling_COLOR(triangle, drawable, pixelShader);
            else
                polygonFilling(triangle, drawable, pixelShader);
        }
    }

    private void polygonFilling(Polygon triangle, Drawable drawable, PixelShader pixelShader) {
        Chain leftChain = triangle.leftChain();
        Chain rightChain = triangle.rightChain();

        Vertex3D top = leftChain.get(0);
        Vertex3D left = leftChain.numVertices <= 1 ? leftChain.get(0) : leftChain.get(1);
        Vertex3D right = rightChain.numVertices <= 1 ? rightChain.get(0) : rightChain.get(1);
        Vertex3D bottom = left.getIntY() <= right.getIntY() ? left : right;
        Vertex3D middle = left.getIntY() <= right.getIntY() ? right : left;
        boolean isMiddleAtLeft = middle == left ? true : false;

        double m_left = getInverseSlopeX(top, left);
        double m_right = getInverseSlopeX(top, right);
        double mz_left = getInverseSlopeZ(top, left);
        double mz_right = getInverseSlopeZ(top, right);


        Vertex3D leftStart = top.getIntY() == left.getIntY() ? left : top;
        Vertex3D rightStart = top.getIntY() == right.getIntY() ? right : top;

        double x_left = leftStart.getIntX();
        double x_right = rightStart.getIntX();
        double z_left  = 1 / leftStart.getZ();
        double z_right  = 1 / rightStart.getZ();

        for(int y = top.getIntY(); y > bottom.getIntY(); y--) {
            if(y <= middle.getIntY()) {
                if(isMiddleAtLeft) {
                    m_left = getInverseSlopeX(middle, bottom);
                    mz_left = getInverseSlopeZ(middle, bottom);
                }
                else {
                    m_right = getInverseSlopeX(middle, bottom);
                    mz_right = getInverseSlopeZ(middle, bottom);
                }
            }
            x_left = x_left + m_left;
            x_right = x_right + m_right;
            z_left = z_left + mz_left;
            z_right = z_right + mz_right;

            double deltaX = x_right - x_left;
            double mZ = (z_right - z_left) / deltaX;
            double z = z_left;

            for(int x = (int)Math.round(x_left); x < (int)Math.round(x_right); x++){
                double csz = 1 / z;
                Vertex3D vertex = new Vertex3D(x, y, csz, Color.WHITE);
                int argbColor = pixelShader.shade(triangle, vertex).asARGB();
                drawable.setPixel(x, y, csz, argbColor);
                z = z + mZ;
            }
        }
    }

    private void polygonFilling_COLOR(Polygon triangle, Drawable drawable, PixelShader pixelShader) {
        Chain leftChain = triangle.leftChain();
        Chain rightChain = triangle.rightChain();

        Vertex3D top = leftChain.get(0);
        Vertex3D left = leftChain.numVertices <= 1 ? leftChain.get(0) : leftChain.get(1);
        Vertex3D right = rightChain.numVertices <= 1 ? rightChain.get(0) : rightChain.get(1);
        Vertex3D bottom = left.getIntY() <= right.getIntY() ? left : right;
        Vertex3D middle = left.getIntY() <= right.getIntY() ? right : left;
        boolean isMiddleAtLeft = middle == left ? true : false;

        double m_left = getInverseSlopeX(top, left);
        double m_right = getInverseSlopeX(top, right);
        double mz_left = getInverseSlopeZ(top, left);
        double mz_right = getInverseSlopeZ(top, right);

        Color rgb_leftSlope = getInverseSlopeRGB(top, left);
        Color rgb_rightSlope = getInverseSlopeRGB(top, right);

        Vertex3D leftStart = top.getIntY() == left.getIntY() ? left : top;
        Vertex3D rightStart = top.getIntY() == right.getIntY() ? right : top;

        double x_left = leftStart.getIntX();
        double x_right = rightStart.getIntX();
        double z_left  = 1 / leftStart.getZ();
        double z_right  = 1 / rightStart.getZ();
        Color colorLeft = leftStart.getColor().scale(1 / leftStart.getZ());
        Color colorRight = rightStart.getColor().scale(1 / rightStart.getZ());

        for(int y = top.getIntY(); y > bottom.getIntY(); y--) {
            if(y <= middle.getIntY()) {
                if(isMiddleAtLeft) {
                    m_left = getInverseSlopeX(middle, bottom);
                    mz_left = getInverseSlopeZ(middle, bottom);
                    rgb_leftSlope = getInverseSlopeRGB(middle, bottom);
                }
                else {
                    m_right = getInverseSlopeX(middle, bottom);
                    mz_right = getInverseSlopeZ(middle, bottom);
                    rgb_rightSlope = getInverseSlopeRGB(middle, bottom);
                }
            }
            x_left = x_left + m_left;
            x_right = x_right + m_right;
            z_left = z_left + mz_left;
            z_right = z_right + mz_right;
            colorLeft = colorLeft.add(rgb_leftSlope);
            colorRight = colorRight.add(rgb_rightSlope);

            double deltaX = x_right - x_left;
            double mZ = (z_right - z_left) / deltaX;
            Color mRGB = (colorRight.subtract(colorLeft)).scale(1 / deltaX);

            double z = z_left;
            Color rgb = colorLeft;

            for(int x = (int)Math.round(x_left); x < (int)Math.round(x_right); x++){
                double csz = 1 / z;
                Color csz_rgb = rgb.scale(csz);
                Vertex3D vertex = new Vertex3D(x, y, csz, csz_rgb);
                int argbColor = pixelShader.shade(triangle, vertex).asARGB();
                drawable.setPixel(x, y, csz, argbColor);
                z = z + mZ;
                rgb = rgb.add(mRGB);
            }
        }
    }

    private void polygonFilling_COLOR_NORMAL(Polygon triangle, Drawable drawable, PixelShader pixelShader) {
        Chain leftChain = triangle.leftChain();
        Chain rightChain = triangle.rightChain();

        Vertex3D top = leftChain.get(0);
        Vertex3D left = leftChain.numVertices <= 1 ? leftChain.get(0) : leftChain.get(1);
        Vertex3D right = rightChain.numVertices <= 1 ? rightChain.get(0) : rightChain.get(1);
        Vertex3D bottom = left.getIntY() <= right.getIntY() ? left : right;
        Vertex3D middle = left.getIntY() <= right.getIntY() ? right : left;
        boolean isMiddleAtLeft = middle == left ? true : false;

        double m_left = getInverseSlopeX(top, left);
        double m_right = getInverseSlopeX(top, right);
        double mz_left = getInverseSlopeZ(top, left);
        double mz_right = getInverseSlopeZ(top, right);
        double mx_left = getInverseSlopeX_(top, left);
        double mx_right = getInverseSlopeX_(top, right);
        double my_left = getInverseSlopeY_(top, left);
        double my_right = getInverseSlopeY_(top, right);

        Color rgb_leftSlope = getInverseSlopeRGB(top, left);
        Color rgb_rightSlope = getInverseSlopeRGB(top, right);
        Point3DH normal_left = getInverseSlopeNormal(top, left);
        Point3DH normal_right = getInverseSlopeNormal(top, right);

        Vertex3D leftStart = top.getIntY() == left.getIntY() ? left : top;
        Vertex3D rightStart = top.getIntY() == right.getIntY() ? right : top;

        double x_left = leftStart.getIntX();
        double x_right = rightStart.getIntX();
        double z_left  = 1 / leftStart.getZ();
        double z_right  = 1 / rightStart.getZ();
        double csx_left = leftStart.getCameraPoint().getX() / leftStart.getZ();
        double csx_right = rightStart.getCameraPoint().getX() / rightStart.getZ();
        double csy_left = leftStart.getCameraPoint().getY() / leftStart.getZ();
        double csy_right = rightStart.getCameraPoint().getY() / rightStart.getZ();
        Color colorLeft = leftStart.getColor().scale(1 / leftStart.getZ());
        Color colorRight = rightStart.getColor().scale(1 / rightStart.getZ());
        Point3DH normalLeft = leftStart.getNormal().scale(1 / leftStart.getZ());
        Point3DH normalRight = rightStart.getNormal().scale(1 / rightStart.getZ());

        for(int y = top.getIntY(); y > bottom.getIntY(); y--) {
            if(y <= middle.getIntY()) {
                if(isMiddleAtLeft) {
                    m_left = getInverseSlopeX(middle, bottom);
                    mz_left = getInverseSlopeZ(middle, bottom);
                    mx_left = getInverseSlopeX_(middle, bottom);
                    my_left = getInverseSlopeY_(middle, bottom);
                    rgb_leftSlope = getInverseSlopeRGB(middle, bottom);
                    normal_left = getInverseSlopeNormal(middle, bottom);
                }
                else {
                    m_right = getInverseSlopeX(middle, bottom);
                    mz_right = getInverseSlopeZ(middle, bottom);
                    mx_right = getInverseSlopeX_(middle, bottom);
                    my_right = getInverseSlopeY_(middle, bottom);
                    rgb_rightSlope = getInverseSlopeRGB(middle, bottom);
                    normal_right = getInverseSlopeNormal(middle, bottom);
                }
            }
            x_left = x_left + m_left;
            x_right = x_right + m_right;
            z_left = z_left + mz_left;
            z_right = z_right + mz_right;
            csx_left = csx_left + mx_left;
            csx_right = csx_right + mx_right;
            csy_left = csy_left + my_left;
            csy_right = csy_right + my_right;
            colorLeft = colorLeft.add(rgb_leftSlope);
            colorRight = colorRight.add(rgb_rightSlope);
            normalLeft = normalLeft.add(normal_left);
            normalRight = normalRight.add(normal_right);

            double deltaX = x_right - x_left;
            double mZ = (z_right - z_left) / deltaX;
            double mX = (csx_right - csx_left) / deltaX;
            double mY = (csy_right - csy_left) / deltaX;
            Color mRGB = (colorRight.subtract(colorLeft)).scale(1 / deltaX);
            Point3DH mNormal = (normalRight.subtract(normalLeft)).scale(1 / deltaX);

            double z = z_left;
            double x_ = csx_left;
            double y_ = csy_left;
            Color rgb = colorLeft;
            Point3DH normal = normalLeft;

            for(int x = (int)Math.round(x_left); x < (int)Math.round(x_right); x++){
                double csz = 1 / z;
                double csx = x_ * csz;
                double csy = y_ * csz;
                Color csz_rgb = rgb.scale(csz);
                Point3DH currentNormal = normal.scale(csz);
                Vertex3D current = new Vertex3D(csx, csy, csz, csz_rgb);
                current.setNormal(currentNormal);
                int argbColor = pixelShader.shade(triangle, current).asARGB();
                drawable.setPixel(x, y, csz, argbColor);
                z = z + mZ;
                x_ = x_ + mX;
                y_ = y_ + mY;
                rgb = rgb.add(mRGB);
                normal = normal.add(mNormal);
            }
        }
    }

    private double getInverseSlopeX(Vertex3D p1, Vertex3D p2) {

        double deltaX = p2.getIntX() - p1.getIntX();
        double deltaY = p2.getIntY() - p1.getIntY();
        double inversedSlope = - (deltaX / deltaY);

        return inversedSlope;
    }

    private double getInverseSlopeZ(Vertex3D p1, Vertex3D p2) {

        double deltaZ = 1 / p2.getZ() - 1 / p1.getZ();
        double deltaY = p2.getIntY() - p1.getIntY();
        double inversedSlope = - (deltaZ / deltaY);

        return inversedSlope;
    }

    private double getInverseSlopeX_(Vertex3D p1, Vertex3D p2) {
        double deltaY = p2.getIntY() - p1.getIntY();
        double deltaX = p2.getCameraPoint().getX() / p2.getZ() -  p1.getCameraPoint().getX() / p1.getZ();
        double slope = - (deltaX / deltaY);
        return slope;
    }

    private double getInverseSlopeY_(Vertex3D p1, Vertex3D p2) {
        double deltaY = p2.getIntY() - p1.getIntY();
        double deltaY_ = p2.getCameraPoint().getY() / p2.getZ() -  p1.getCameraPoint().getY() / p1.getZ();
        double slope = - (deltaY_ / deltaY);
        return slope;
    }

    private Color getInverseSlopeRGB(Vertex3D p1, Vertex3D p2) {

        double deltaY = p2.getIntY() - p1.getIntY();
        double deltaR = p2.getColor().getR() / p2.getZ() - p1.getColor().getR() / p1.getZ();
        double deltaG = p2.getColor().getG() / p2.getZ() - p1.getColor().getG() / p1.getZ();
        double deltaB = p2.getColor().getB() / p2.getZ() - p1.getColor().getB() / p1.getZ();

        double slopeR = -(deltaR / deltaY);
        double slopeG = -(deltaG / deltaY);
        double slopeB = -(deltaB / deltaY);

        return new Color(slopeR, slopeG, slopeB);
    }

    private Point3DH getInverseSlopeNormal(Vertex3D p1, Vertex3D p2) {

        double deltaY = p2.getIntY() - p1.getIntY();
        double dx = p2.getNormal().getX() / p2.getZ() - p1.getNormal().getX() / p1.getZ();
        double dy = p2.getNormal().getY() / p2.getZ() - p1.getNormal().getY() / p1.getZ();
        double dz = p2.getNormal().getZ() / p2.getZ() - p1.getNormal().getZ() / p1.getZ();

        double slopeX = -(dx / deltaY);
        double slopeY = -(dy / deltaY);
        double slopeZ = -(dz / deltaY);

        return new Point3DH(slopeX, slopeY, slopeZ);
    }

    public static FilledPolygonRenderer make(){ return new FilledPolygonRenderer(); }
}
