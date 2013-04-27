//3D Vector class using floats
//Created by James Vanderhyde, 25 October 2012
//Modified by Josh Kuestersteffen, 20 April 2013
//  Changed from floats to doubles

package molMan;

/**
 *
 * @author jamesvanderhyde
 */
public class Vector3
{
    public double x,y,z;
    
    public Vector3()
    {
        x=y=z=0f;
    }
    
    public Vector3(double x,double y,double z)
    {
        this.x=x;
        this.y=y;
        this.z=z;
    }
    
    public float length()
    {
        return (float)Math.sqrt(x*x+y*y+z*z);
    }
    
    public static double dot(Vector3 u, Vector3 v)
    {
        return u.x*v.x+u.y*v.y+u.z*v.z;
    }
    
    public static Vector3 cross(Vector3 u, Vector3 v)
    {
        Vector3 r=new Vector3();
        r.x=u.y*v.z-u.z*v.y;
        r.y=u.z*v.x-u.x*v.z;
        r.z=u.x*v.y-u.y*v.x; 
        return r;
    }
    
    public static Vector3 normalized(Vector3 v)
    {
        float d=v.length();
        return new Vector3(v.x/d,v.y/d,v.z/d);
    }
    
    @Override
    public String toString()
    {
        return "("+x+","+y+","+z+")";
        
    }
}
