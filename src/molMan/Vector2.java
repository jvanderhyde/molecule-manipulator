//A 2-dimensional vector of double values
//Created by James Vanderhyde, 10 July 2012
//Modified by James Vanderhyde, 5 October 2012
//  Fixed up for CS 398 Computer Graphics

package molMan;

public class Vector2
{
    final double x,y;
    
    public Vector2(double x, double y)
    {
        this.x=x;
        this.y=y;
    }
    
    public double dot(Vector2 v)
    {
        return x*v.x+y*v.y;
    }
    
    public Vector2 times(double c)
    {
        return new Vector2(c*x,c*y);
    }
    
    public double crosz(Vector2 v)
    {
        return x*v.y-v.x*y;
    }
    
    public double norm()
    {
        return Math.sqrt(x*x+y*y);
    }

    public double normsquare()
    {
        return x*x+y*y;
    }
    
    public Vector2 normalized()
    {
        return this.times(1/norm());
    }
    
    public Vector2 add(Vector2 v)
    {
        return new Vector2(x+v.x,y+v.y);
    }
    
    public Vector2 subtract(Vector2 v)
    {
        return new Vector2(x-v.x,y-v.y);
    }
    
    public Vector2 orthogon()
    {
        return new Vector2(y,-x);
    }
    
    public static Vector2 angleAndLength(double angle,double length)
    {
        return new Vector2(length*Math.cos(angle),length*Math.sin(angle));
    }
    
    @Override
    public String toString()
    {
        return "("+x+","+y+")";
    }
    
}
