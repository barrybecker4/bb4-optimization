/** Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT  */
package com.barrybecker4.optimization.optimizee.optimizees;

import com.barrybecker4.optimization.parameter.ParameterArray;
import com.barrybecker4.optimization.parameter.VariableLengthIntArray;
import com.barrybecker4.optimization.parameter.types.IntegerParameter;
import com.barrybecker4.optimization.parameter.types.Parameter;
import com.barrybecker4.optimization.strategy.OptimizationStrategyType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.barrybecker4.optimization.optimizee.optimizees.AnalyticFunctionConsts.BASE_TOLERANCE;
import static com.barrybecker4.optimization.optimizee.optimizees.AnalyticFunctionConsts.GLOB_SAMP_TOL;
import static com.barrybecker4.optimization.optimizee.optimizees.AnalyticFunctionConsts.RELAXED_TOL;

/**
 * An enum for different sorts of traveling salesman problems (TSPs) that we might want to test.
 * The TSP is represented by an adjacency matrix that give weights to costs between vertices
 * in a graph. The graph may or may not be a directed graph depending on the problem you are modeling.
 *
 * @author Barry Becker
 */
public enum DominatingSetVariation implements IProblemVariation {

    SIMPLE {
        /**
         * Trivial example.
         * There are three nodes, A, B, C. And this list of lists defines the connectivity of the graph.
         */
        private final List<List<Integer>> ADJACENCIES = Arrays.asList(
                Arrays.asList(1, 2),
                Arrays.asList(0, 1),
                Arrays.asList(0, 2)
        );

        @Override
        public int getNumNodes() {
            return ADJACENCIES.size();
        }

        public ParameterArray getExactSolution() {
            VariableLengthIntArray solution = createSolution(new int[] {0});
            solution.setFitness(1);
            return solution;
        }

        @Override
        public double getFitnessRange() {
            return 8.0;
        }

        @Override
        public double evaluateFitness(ParameterArray a) {
            return computeCost(a, ADJACENCIES);
        }

        @Override
        public double getErrorTolerancePercent(OptimizationStrategyType opt) {
            return getErrorTolerancePercent(opt, new double[] {
                    GLOB_SAMP_TOL, BASE_TOLERANCE, BASE_TOLERANCE, 0.04,  RELAXED_TOL,  0.042,   0.042, BASE_TOLERANCE
            });
        }
    }; /*


    TYPICAL {
        private final List<List<Integer>> ADJACENCIES = Arrays.asList(
                Arrays.asList(15, 21, 25),
                Arrays.asList(2, 4, 7),
                Arrays.asList(1, 3, 5, 7),
                Arrays.asList(2, 5, 8, 9),
                Arrays.asList(1, 6, 7, 12),  // 4
                Arrays.asList(2, 3, 8, 13),
                Arrays.asList(4, 10, 11, 12),
                Arrays.asList(1, 2, 12, 13),
                Arrays.asList(3, 5, 9, 14),   // 8
                Arrays.asList(3, 8, 15),
                Arrays.asList(6, 11, 18),
                Arrays.asList(6, 10, 16),
                Arrays.asList(4, 6, 7, 16, 17),  // 12
                Arrays.asList(5, 7, 14, 17),
                Arrays.asList(8, 13, 15, 17),
                Arrays.asList(0, 9, 14, 21),
                Arrays.asList(11, 12, 19),  // 16
                Arrays.asList(12, 13, 14, 20, 21),
                Arrays.asList(10, 19, 22, 24),
                Arrays.asList(16, 18, 20),
                Arrays.asList(17, 19, 22, 23),  // 20
                Arrays.asList(0, 15, 17, 23),
                Arrays.asList(18, 20, 23, 24),
                Arrays.asList(20, 21, 22, 25),
                Arrays.asList(18, 22, 25),  // 24
                Arrays.asList(0, 23, 24)
        );

        @Override
        public int getNumNodes() {
            return ADJACENCIES.size();
        }

        public ParameterArray getExactSolution() {
            VariableLengthIntArray solution = createSolution(new int[] {6, 7, 8, 19, 21, 24});
            solution.setFitness(6);
            return solution;
        }

        @Override
        public double getFitnessRange() {
            return 40.0;
        }

        @Override
        public double evaluateFitness(ParameterArray a) {
            return computeCost(a, ADJACENCIES);
        }

        @Override
        public double getErrorTolerancePercent(OptimizationStrategyType opt) {
            return getErrorTolerancePercent(opt, new double[] {
                    GLOB_SAMP_TOL, BASE_TOLERANCE, BASE_TOLERANCE, 0.04,  RELAXED_TOL,  0.042,   0.042, BASE_TOLERANCE
            });
        }
    };     */


    /** @return the number of nodes in the graph */
    public abstract int getNumNodes();

    /**
     * Some random initial set of marked nodes.
     * One half or one third of the nodes is probably a good starting point.
     */
    public ParameterArray getInitialGuess() {
        int num = this.getNumNodes();
        List<Parameter> params = new ArrayList<>(num);
        // just add every second node
        for (int i=0; i<num; i+=2) {
            params.add(new IntegerParameter(i, 0, num-1, "p" + i));
        }
        return new VariableLengthIntArray(params, getNumNodes());
    }

    public double getScore(List<Integer> marked, List<List<Integer>> adjacencies) {
        return marked.size() + getNumNotWithinOneHop(marked, adjacencies);
    }

    protected int getNumNotWithinOneHop(List<Integer> marked, List<List<Integer>> adjacencies) {
        int total = 0;
        for (int i=0; i < adjacencies.size(); i++) {
            if (!marked.contains(i)) {
                 total += isNodeOneHopAway(i, marked, adjacencies) ? 0 : 1;
            }
        }
        return total;
    }

    /**
     * @param i node to start searching from
     * @param marked list of marked nodes
     * @return true if node i is only one hop from a marked node
     */
    protected boolean isNodeOneHopAway(int i, List<Integer> marked, List<List<Integer>> adjacencies) {
        List<Integer> nbrs = adjacencies.get(i);
        for (int j : marked) {
            if (nbrs.contains(j)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluate fitness for the analytics function.
     * @param a the position on the parabolic surface given the specified values of p1 and p2
     * @return fitness value
     */
    public abstract double evaluateFitness(ParameterArray a);

    /** Approximate value of maxCost - minCost */
    public abstract double getFitnessRange();

    /**
     * We assume that the parameter array contains 0 based integers
     * @param params last best guess at dominating set.
     * @return the total cost of the path represented by param.
     */
    protected double computeCost(ParameterArray params, List<List<Integer>> adjacencies) {

        List<Integer> marked = new ArrayList<>();
        for (int i = 0; i < params.size(); i++)  {
            Parameter node = params.get(i);
            marked.add((int)node.getValue());
        }

        return getScore(marked, adjacencies);
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
     * Create the solution based on the ordered list of cities.
     * @param nodeList optimal dominating set of marked nodes. May not be unique.
     * @return optimal solution (to compare against at the end of the test).
     */
    protected VariableLengthIntArray createSolution(int[] nodeList) {
        int numNodes = nodeList.length;
        List<Parameter> params = new ArrayList<>(numNodes);
        for (int i=0; i < nodeList.length; i++) {
            params.add(new IntegerParameter(nodeList[i], 0, getNumNodes() - 1, "p" + i));
        }
        return new VariableLengthIntArray(params, getNumNodes());
    }
}