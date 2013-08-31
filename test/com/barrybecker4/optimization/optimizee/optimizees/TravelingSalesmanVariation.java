/** Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT  */
package com.barrybecker4.optimization.optimizee.optimizees;

import com.barrybecker4.optimization.parameter.ParameterArray;
import com.barrybecker4.optimization.parameter.PermutedParameterArray;
import com.barrybecker4.optimization.parameter.types.IntegerParameter;
import com.barrybecker4.optimization.parameter.types.Parameter;
import com.barrybecker4.optimization.strategy.OptimizationStrategyType;

import static com.barrybecker4.optimization.optimizee.optimizees.AnalyticFunctionConsts.*;

/**
 * An enum for different sorts of traveling salesman problems (TSPs) that we might want to test.
 * The TSP is represented by an adjacency matrix that give weights to costs between vertices
 * in a graph. The graph may or may not be a directed graph depending on the problem you are modeling.
 *
 * @author Barry Becker
 */
public enum TravelingSalesmanVariation {

    SIMPLE {
        /**
         * Trivial example.
         * There are three cities, A, B, C. This is the adjacency cost matrix.
         */
        private final double[][] COST_MATRIX =  {
                {0, 3, 2, 1},
                {3, 0, 1, 2},
                {2, 1, 0, 3},
                {1, 2, 3, 0}
        };

        @Override
        public int getNumCities() {
            return 4;
        }

        @Override
        public double getShortestPathLength() {
            return 6.0;
        }

        @Override
        public PermutedParameterArray getExactSolution() {
            PermutedParameterArray solution =  createSolution(new int[] {0, 2, 1, 3});
            solution.setFitness(getShortestPathLength());
            return solution;
        }

        @Override
        public double getFitnessRange() {
            return 8.0;
        }

        @Override
        public double evaluateFitness(ParameterArray a) {
            return getShortestPathLength() - computeCost(a, COST_MATRIX);
        }

        @Override
        public double getErrorTolerancePercent(OptimizationStrategyType opt) {
            return getErrorTolerancePercent(opt, new double[] {
                    GLOB_SAMP_TOL, BASE_TOLERANCE, BASE_TOLERANCE, 0.04,  RELAXED_TOL,  0.042,   0.042, BASE_TOLERANCE
            });
        }
    },
    STANDARD {
        /**
         * This version is a bit more realistic.
         * Se http://www.tilburguniversity.edu/research/institutes-and-research-groups/center/staff/haemers/reader10ico.pdf
         *       B,  E,  H,  N,  T
         */
        private final double[][] COST_MATRIX =  {
                {0, 54, 48, 92, 24},
                {54, 0, 32, 61, 35},
                {48, 32, 0, 45, 23},
                {92, 61, 45, 0, 67},
                {24, 35, 23, 67, 0}
        };

        @Override
        public int getNumCities() {
            return 5;
        }

        @Override
        public double getShortestPathLength() {
            return 207.0;
        }

        @Override
        public PermutedParameterArray getExactSolution() {
            PermutedParameterArray solution = createSolution(new int[] {2, 4, 0, 1, 3});
            solution.setFitness(getShortestPathLength());
            return solution;
        }

        @Override
        public double getFitnessRange() {
            return 1000.0;
        }

        @Override
        public double evaluateFitness(ParameterArray a) {
            return getShortestPathLength() - computeCost(a, COST_MATRIX);
        }

        @Override
        public double getErrorTolerancePercent(OptimizationStrategyType opt) {
            return getErrorTolerancePercent(opt, new double[] {
                    GLOB_SAMP_TOL, RELAXED_TOL, 0.01, 0.04,  RELAXED_TOL, 0.042, 0.042, BASE_TOLERANCE
            });
        }
    };

    /** @return the number of cities to visit */
    public abstract int getNumCities();


    /** An optimal ordering of the cities to visit such that cost is minimized. */
    public abstract PermutedParameterArray getExactSolution();

    /** Some random initial permutation of the cities */
    public ParameterArray getInitialGuess() {
        int num = this.getNumCities();
        IntegerParameter[] params = new IntegerParameter[num];
        for (int i=0; i<num; i++) {
            params[i] = new IntegerParameter(i, 0, num-1, "p" + i);
        }
        return new PermutedParameterArray(params);
    }

    /** Approximate value of maxCost - minCost */
    public abstract double getFitnessRange();

    public abstract double getShortestPathLength();

    /**
     * Evaluate fitness for the analytics function.
     * @param a the position on the parabolic surface given the specified values of p1 and p2
     * @return fitness value
     */
    public abstract double evaluateFitness(ParameterArray a);

    /**
     * We assume that the parameter array contains 0 based integers
     * @param params
     * @param matrix
     * @return the total cost of the path represented by param.
     */
    protected double computeCost(ParameterArray params, double[][] matrix) {
        double totalCost = 0;
        Parameter lastLocation = params.get(0);

        for (int i=1; i<params.size(); i++)  {
            Parameter currentLocation = params.get(i);
            totalCost += matrix[(int)lastLocation.getValue()][(int)currentLocation.getValue()];
            lastLocation = currentLocation;
        }
        // and back home again
        totalCost +=  matrix[(int)lastLocation.getValue()][(int)params.get(0).getValue()];
        return totalCost ;
    }

    /**
     * Error tolerance for each search strategy and variation of the problem.
     * @param opt optimization strategy.
     * @return error tolerance percent
     */
    public abstract double getErrorTolerancePercent(OptimizationStrategyType opt);

    protected double getErrorTolerancePercent(OptimizationStrategyType opt, double[] percentValues) {

        double percent = 0;
        switch (opt) {
            case GLOBAL_SAMPLING : percent = percentValues[0]; break;
            case GLOBAL_HILL_CLIMBING : percent = percentValues[1]; break;
            case HILL_CLIMBING : percent = percentValues[2]; break;
            case SIMULATED_ANNEALING : percent = percentValues[3]; break;
            case TABU_SEARCH: percent = percentValues[4]; break;
            case GENETIC_SEARCH : percent = percentValues[5]; break;
            case CONCURRENT_GENETIC_SEARCH : percent = percentValues[6]; break;
            case STATE_SPACE: percent = percentValues[7]; break;
        }
        return percent;
    }

    /**
     * Creat the solution based on the ordered list of cities.
     * @param cityList optimal ordering of city indices.
     * @return optimal solution (to compare against at the end of the test).
     */
    protected PermutedParameterArray createSolution(int[] cityList) {
        int numCities = cityList.length;
        IntegerParameter[] params = new IntegerParameter[numCities];
        for (int i=0; i< cityList.length; i++) {
            params[i] = new IntegerParameter(cityList[i], 0, numCities-1, "p" + i);
        }
        return new PermutedParameterArray(params);
    }
}