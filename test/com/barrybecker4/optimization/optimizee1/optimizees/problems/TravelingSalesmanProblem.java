// Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.optimization.optimizee1.optimizees.problems;

import com.barrybecker4.optimization.Optimizer1;
import com.barrybecker4.optimization.optimizee1.optimizees.OptimizeeProblem;
import com.barrybecker4.optimization.parameter1.ParameterArray;
import com.barrybecker4.optimization.strategy1.OptimizationStrategyType;

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
        return "TSP: " + variation_.name();
    }


    /**
     * we evaluate directly not by comparing with a different trial.
     */
    @Override
    public boolean evaluateByComparison() {
        return false;
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
        Optimizer1 optimizer =
                new Optimizer1(problem, LOG_FILE_HOME + "tsp_optimization.txt");

        ParameterArray initialGuess = problem.getInitialGuess();

        ParameterArray solution =
                optimizer.doOptimization(OptimizationStrategyType.SIMULATED_ANNEALING, initialGuess, v.getFitnessRange());

        showSolution(problem, solution);
    }
}
