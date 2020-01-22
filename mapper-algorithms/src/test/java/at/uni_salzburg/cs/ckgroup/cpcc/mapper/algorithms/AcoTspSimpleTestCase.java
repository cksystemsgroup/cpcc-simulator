
package at.uni_salzburg.cs.ckgroup.cpcc.mapper.algorithms;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class AcoTspSimpleTestCase
{

    public static final double[][] cities_01 = {
        {0, 0}, {1, 1}, {2, 2}, {3, 3}, {4, 4}
    };

    public static final double[][] cities_02 = {
        {1, 1}, {1, 9}, {9, 9}, {9, 1}
    };

    public static final double[][] cities_03 = {
        {3, 0}, {4, 1}, {6, 0}, {6, 2}, {5, 2},
        {7, 3}, {8, 0}, {9, 5}, {3, 2}, {2, 2},
    };

    public static final double[][] cities_04 = {
        {0, 0}, {1, 1}, {2, 2}, {3, 3}, {4, 4}, {2, 0}
    };

    public static final double[][] cities_05 = {
        {0, 0}, {5, 5}, {1, 1}, {2, 2}, {3, 3}, {4, 4}
    };

    public static final double[][] cities_06 = {
        {1, 1}, {1, 3}, {1, 9}, {2, 5}, {2, 7}, {3, 1}, {3, 3}, {4, 0}, {4, 3}, {4, 7},
        {5, 1}, {5, 7}, {5, 9}, {6, 5}, {7, 3}, {7, 9}, {8, 7}, {9, 1}, {9, 5}, {9, 9},
        {10, 3}, {10, 7}, {11, 1}, {11, 3}, {12, 3}, {12, 5}, {12, 9}, {13, 1}, {13, 5}, {13, 7},
    };

    public static double[][] createCostMatrix(double[][] cities)
    {
        int n = cities.length;
        double[][] costs = new double[n][n];

        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                double dx = cities[j][0] - cities[i][0];
                double dy = cities[j][1] - cities[i][1];
                costs[i][j] = Math.sqrt(dx * dx + dy * dy);
            }
        }

        return costs;
    }

    @Test
    public void test01()
    {
        System.out.println("starting");
        double[][] cities = createCostMatrix(cities_03);
        //		cities[0][1] = 0;
        //		cities[1][0] = 0;
        List<Integer> path = AcoTspSimple.calculateBestPath(cities, 4000, 3);
        System.out.println(path);
        System.out.println("len = " + AcoTspSimple.realPathLength(cities, path));
        Assert.assertEquals(21.228009718084742, AcoTspSimple.realPathLength(cities, path), 1E-9);
    }

    //	@Test
    public void test02()
    {

        System.out.println("starting");
        double[][] cities = createCostMatrix(cities_06);

        List<Integer> path = AcoTspSimple.calculateBestPath(cities, 1000, 5);
        System.out.println(path);
        System.out.println("len = " + AcoTspSimple.realPathLength(cities, path));

        for (Integer k : path)
        {
            System.out.printf("%.0f,%.0f\n", cities_06[k][0], cities_06[k][1]);
        }
    }
}
