//A class for 2D transformation matrices
//Created by James Vanderhyde, 4 October 2012

package molMan;

/**
 *
 * @author jamesvanderhyde
 */
public class Matrix3x3
{
    public double m11, m12, tx;
    public double m21, m22, ty;
    public double m31, m32, tz; 
    
    /**
     * Initializes a new identity matrix
     */
    public Matrix3x3()
    {
        m11=m22=tz = 1.0;
        m12=m21=m31=0.0;
        tx=ty=m32=0.0;
    }
    
    /**
     * Transforms the given vector using this matrix.
     *   Leaves the given vector intact.
     * @param v A vector to transform
     * @return a new vector, the transformed input vector
     */
    public Vector3 transform(Vector3 v)
    {
        Vector3 vp = new Vector3();
        vp.x = this.m11*v.x + this.m12*v.y+this.tx*v.z;
        vp.y = this.m21*v.x + this.m22*v.y+this.ty*v.z;
        vp.z = this.m31*v.x + this.m32*v.z+this.tz*v.z;
        
        return vp;
    }
    
    
    /**
     * Multiplies the two given matrices in the given order.
     * @param a Matrix A
     * @param b Matrix B
     * @return a new matrix C = AB
     */
    public static Matrix3x3 multiply(Matrix3x3 a, Matrix3x3 b)
    {
        Matrix3x3 c=new Matrix3x3();
        c.m11=a.m11*b.m11+a.m12*b.m21+a.tx*b.m31;
        c.m12=a.m11*b.m12+a.m12*b.m22+a.tx*b.m32;
        c.m21=a.m21*b.m11+a.m22*b.m21+a.ty*b.m31;
        c.m22=a.m21*b.m12+a.m22*b.m22+a.ty*b.m32;
        c.m31=a.m31*b.m11+a.m32*b.m21+a.tz*b.m31;
        c.m32=a.m31*b.m12+a.m32*b.m22+a.tz*b.m32;
        c.tx=a.m11*b.tx+a.m12*b.ty+a.tx*b.tz;
        c.ty=a.m21*b.tx+a.m22*b.ty+a.ty*b.tz;
        c.tz=a.m31*b.tx+a.m32*b.ty+a.tz*b.tz;
        return c;
    }
    
    /**
     * Creates a new transformation matrix that applies a rotation
     * @param theataInDegrees The angle of rotation, CCW from the input axis
     * @return A new transformation matrix
     */
    public static Matrix3x3 rotationMatrix(double theataInDegrees)
    {
        Matrix3x3 r=new Matrix3x3();
        double theta=theataInDegrees/180*Math.PI;
        double ct=Math.cos(theta);
        double st=Math.sin(theta);
        r.m11 = ct; r.m12 = -st;
        r.m21 = st; r.m22 = ct;
        r.m31 = r.m32 = r.tx = r.ty = 0;
        r.tz = 1;
        return r;
    }
    /**
     * 
     * @param tx tx value for the Matrix
     * @param ty ty value for the Matrix
     * @param tz tz value for the Matrix
     * @return a translation Matrix t
     */
    public static Matrix3x3 translationMatrix(double tx, double ty, double tz)
    {
        Matrix3x3 t = new Matrix3x3();
        t.tx = tx;
        t.ty = ty;
        t.tz = tz;
        t.m11 = t.m22 = 1.0;
        t.m12 = t.m21 = t.m31 = t.m32 = 0.0;
        return t;
    }
}
