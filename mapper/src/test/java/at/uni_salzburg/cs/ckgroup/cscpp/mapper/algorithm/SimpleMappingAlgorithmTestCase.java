package at.uni_salzburg.cs.ckgroup.cscpp.mapper.algorithm;

import org.junit.Assert;
import org.junit.Test;

import at.uni_salzburg.cs.ckgroup.course.CartesianCoordinate;

public class SimpleMappingAlgorithmTestCase {

	@Test
	public void test() {
		CartesianCoordinate current = new CartesianCoordinate(0, 0, 2);
		CartesianCoordinate point = new CartesianCoordinate(0, 0, 3); 
		CartesianCoordinate next = new CartesianCoordinate(0, 0, 5);
		
		Double d0 = isNear(current, next, point, 0.5, 0.5);
		Assert.assertNotNull(d0);
		Double d = oldIsNear(current, next, point, 0.5, 0.5);
		Assert.assertNull(d);
	}

	
	private Double oldIsNear(CartesianCoordinate current, CartesianCoordinate next, CartesianCoordinate point, double tol, double velocity) {
	  // direction vec = next - current
	  CartesianCoordinate dir = next.subtract(current);
	  // CN = c + t*dir -> t
	  double t = ((current.multiply(dir)*(-1)) + (point.multiply(dir)) / dir.multiply(dir));
	  // t in CN -> L
	  CartesianCoordinate l = new CartesianCoordinate(current.getX() + t*dir.getX(), current.getY() + t*dir.getY(), current.getZ() + t*dir.getZ());
	  
	  if(current.norm() <= l.norm() && l.norm() <= next.norm()) {
	      // d = | P - L |
	      double d = point.subtract(l).norm();
	      
	      return d <= tol ? point.subtract(current).norm()/velocity : null;
	  }
	  else return null;
	}
	
    private Double isNear(CartesianCoordinate current, CartesianCoordinate next, CartesianCoordinate point, double tol, double velocity) {
    	// Are we in the tolerance sphere ?
    	CartesianCoordinate pmc = point.subtract(current);
    	double pmcNorm = pmc.norm();
    	if (pmcNorm <= tol) {
    		return pmcNorm / velocity;
    	}
    	
    	// Are we heading towards the VV point?
    	CartesianCoordinate dir = next.subtract(current);
    	if (dir.multiply(pmc) < 0) {
    		return null;
    	}
    	
    	// We are heading towards the VV point, but do we reach it?
    	double dirNorm = dir.norm();
        double d = dir.crossProduct(pmc).norm() / dirNorm;
        return pmcNorm <= dirNorm + tol && d <= tol ? pmcNorm / velocity : null;
    }
}
