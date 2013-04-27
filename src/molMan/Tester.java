/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package molMan;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author mixxm_000
 */
public class Tester {
    
    public static void main(String[] args)
    {
    	   String str = "moveto 0.0 { -7 999 -52 89.99} 100.0 0.0 0.0 {-0.119950056 -0.16490018 0.12435001} 5.8099546 {0.0 0.0 0.0} -0.9978736 -1.972454 0.0;;";
    	   
    	   Scanner sc = new Scanner(str).useDelimiter("}");
	    	String temp = "";
	    	while(sc.hasNext()){ temp+= sc.next();}
	    	   
	    	sc = new Scanner(temp);
	    	int counter = 0;
	    	double[] vals = new double[5];
	    	while(sc.hasNext() && counter<5)
	    	{
	    		if(sc.hasNextDouble())
	    		{
	    			vals[counter] = sc.nextDouble();
	    			counter++;
	    		}
	    		else sc.next();				    		
	    	}
		   				
			//Scanner sc = new Scanner(stateCommand);
			//double test = sc.nextDouble(); //gets rid of the first num that we dont need
			double u = vals[1];//sc.nextDouble();
			double v = vals[2];//sc.nextDouble();
			double w = vals[3];//sc.nextDouble();
			double a = vals[4];//sc.nextDouble();
			
			double x = 4;
			double y = 0;
			double z = 0;
			
			
			RotationMatrix rotMat = new RotationMatrix(0, 0, 0, u, v, w, a);
			
			double[] rotVector = rotMat.timesXYZ(x, y, z);
			
			for(int i=0;i<rotVector.length; i++)System.out.println(rotVector[i]);
			
			/*
			double ux = u*x;
			double uy = u*y;
			double uz = u*z;
			double vx = v*x;
			double vy = v*y;
			double vz = v*z;
			double wx = w*x;
			double wy = w*y;
			double wz = w*z;
			
			double sa = Math.sin(a);
			double ca = Math.cos(a);
			
			x = u*(ux+vy+wz)+(x*(v*v+w*w)-u*(vy+wz))*ca+(-wy+vz)*sa;
			y = v*(ux+vy+wz)+(y*(u*u+w*w)-v*(ux+wz))*ca+(wx-uz)*sa;
			z = w*(ux+vy+wz)+(z*(u*u+v*v)-w*(ux+vy))*ca+(-vx+uy)*sa;
			*/
			
			
			
    	   //Matcher m = Pattern.compile("(?!=\\d\\.\\d\\.)([\\d.]+)").matcher(str);
    	   /* while (m.find())
    	    {
    	        double d = Double.parseDouble(m.group(1));
    	        System.out.println(d);
    	    }
    	    */
    	
    }
}
