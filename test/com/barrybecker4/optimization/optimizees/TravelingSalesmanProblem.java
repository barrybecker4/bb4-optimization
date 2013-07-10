/** Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT  */
package com.barrybecker4.optimization.optimizees;

import com.barrybecker4.common.util.FileUtil;
import com.barrybecker4.optimization.Optimizer;
import com.barrybecker4.optimization.parameter.ParameterArray;
import com.barrybecker4.optimization.strategy.OptimizationStrategyType;

import static com.barrybecker4.optimization.OptimizerTestCase.LOG_FILE_HOME;

/**
 * This is a simple search space to test the optimization package.
 *
 * @author Barry Becker
 */
public class TravelingSalesmanProblem extends OptimizeeProblem {

    private TravelingSalesmanVariation variation_ = TravelingSalesmanVariation.SIMPLE;


    /** constructor */
    public TravelingSalesmanProblem(TravelingSalesmanVariation variation) {
        variation_ = variation;
    }

    @Override
    public String getName() {
        return "Traveling Salesman Problem";
    }

    /**
     * we evaluate directly not by comparing with a different trial.
     */
    @Override
    public boolean evaluateByComparison() {
        return false;
    }

    // not used
    @Override
    public double compareFitness(ParameterArray a, ParameterArray b) {
        return 0.0;
    }

    /**
     * Use the cost matrix for the TSP variation to determine this.
     * @return fitness value
     */
    @Override
    public double evaluateFitness(ParameterArray a) {
        return variation_.evaluateFitness(a);
    }

    @Override
    public ParameterArray getExactSolution() {
        return variation_.getExactSolution();
    }

    @Override
    public ParameterArray getInitialGuess() {
        return variation_.getInitialGuess();
    }

    @Override
    public double getFitnessRange() {
        return variation_.getFitnessRange();
    }

    /**
     * This finds the solution for the above optimization problem.
     */
    public static void main(String[] args) {
        TravelingSalesmanVariation v = TravelingSalesmanVariation.SIMPLE;
        OptimizeeProblem problem = new TravelingSalesmanProblem(v);
        Optimizer optimizer =
                new Optimizer(problem, LOG_FILE_HOME + "tsp_optimization.txt");

        ParameterArray initialGuess = problem.getInitialGuess();

        ParameterArray solution =
                optimizer.doOptimization(OptimizationStrategyType.SIMULATED_ANNEALING, initialGuess, v.getFitnessRange());

        System.out.println( "\n************************************************************************" );
        System.out.println( "The solution to the 7-11 Test Problem is :\n"+solution );
        System.out.println( "Which evaluates to: " + problem.evaluateFitness(solution));
        System.out.println( "We expected to get exactly 711000000:  p1-4 = {316, 125, 120, 150} " );
    }
}
