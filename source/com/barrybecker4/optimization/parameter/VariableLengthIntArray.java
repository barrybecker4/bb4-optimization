/** Copyright by Barry G. Becker, 2013. Licensed under MIT License: http://www.opensource.org/licenses/MIT  */
package com.barrybecker4.optimization.parameter;

import com.barrybecker4.common.math.MathUtil;
import com.barrybecker4.optimization.optimizee.Optimizee;
import com.barrybecker4.optimization.parameter.improvement.DiscreteImprovementFinder;
import com.barrybecker4.optimization.parameter.improvement.Improvement;
import com.barrybecker4.optimization.parameter.sampling.VariableLengthGlobalSampler;
import com.barrybecker4.optimization.parameter.types.IntegerParameter;
import com.barrybecker4.optimization.parameter.types.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *  Represents a 1 dimensional, variable length, array of unique integer parameters.
 *  The order of the integers does not matter.
 *
 *  @author Barry Becker
 */
public class VariableLengthIntArray extends AbstractParameterArray {

    /** the maximum number of params in the array that is possible */
    private int maxLength;

    /** Default constructor */
    protected VariableLengthIntArray() {}

    /**
     * Constructor
     * @param params an array of params to initialize with.
     */
    public VariableLengthIntArray(List<Parameter> params, int max) {
        super(params);
        maxLength = max;
    }

    /** @return the maximum length of the variable length array */
    public int getMaxLength() {
        return maxLength;
    }

    @Override
    protected VariableLengthIntArray createInstance() {
        return new VariableLengthIntArray();
    }

    /**
     * The distance computation will be quite different for this than a regular parameter array.
     * We want the distance to represent a measure of the amount of similarity between two instances.
     * There are two ways in which instance can differ, and the weighting assigned to each may depend on the problem.
     *  - the length of the parameter array
     *  - the set of values in the parameter array.
     * Generally, the distance is greater the greater the number of parameters that is different.
     * @return the distance between this parameter array and another.
     */
    public double distance(ParameterArray pa)  {

        int thisLength = size();
        int thatLength = pa.size();

        List<Integer> theseValues = new ArrayList<>(thisLength);
        List<Integer> thoseValues = new ArrayList<>(thatLength);

        for (Parameter p : params_) {
            theseValues.add((int)p.getValue());
        }
        for (int i=0; i< thatLength; i++) {
            thoseValues.add((int)pa.get(i).getValue());
        }

        Collections.sort(theseValues);
        Collections.sort(thoseValues);

        int valueDifferences = calcValueDifferences(theseValues, thoseValues);

        return Math.abs(thisLength - thatLength) + valueDifferences;
    }

    /**
     * Perform a sort of merge sort on the two sorted lists of values to find matches.
     * The more matches there are between the two lists, the more similar they are.
     * The magnitude of the differences between values does not matter, only whether
     * they are the same or different.
     * @param theseValues first ordered list
     * @param thoseValues second ordered list
     * @return measure of the difference between the two sorted lists.
     *   It will return 0 if the two lists are the same.
     */
    private int calcValueDifferences(List<Integer> theseValues, List<Integer> thoseValues) {

        int thisLen = theseValues.size();
        int thatLen = thoseValues.size();
        int thisCounter = 0;
        int thatCounter = 0;
        int matchCount = 0;

        while (thisCounter < thisLen && thatCounter < thatLen) {
            double thisVal = theseValues.get(thisCounter);
            double thatVal = thoseValues.get(thatCounter);
            if (thisVal < thatVal) {
                thisCounter++;
            }
            else if (thatVal > thisVal) {
                thatCounter++;
            }
            else {  // they are the same
                thisCounter++;
                thatCounter++;
                matchCount++;
            }
        }
        return Math.max(thisLen, thatLen) - matchCount;
    }

    /**
     * Create a new permutation that is not too distant from what we have now.
     * The two ways a configuration of marked nodes can change is
     *  - add or remove nodes
     *  - change values of nodes
     * @param radius a indication of the amount of variation to use. 0 is none, 2 is a lot.
     *   Change Math.min(1, 10 * radius * N/100) of the entries, where N is the number of params
     * @return the random nbr.
     */
    public VariableLengthIntArray getRandomNeighbor(double radius) {

        if (size() <= 1) return this;

        double probAddRemove = 1.0/(1.0 + radius);
        boolean add = false;
        boolean remove = false;
        if (MathUtil.RANDOM.nextDouble() > probAddRemove) {
            if ((MathUtil.RANDOM.nextDouble() > 0.5 || size() <= 1) && size() < maxLength-1 ) {
                add = true;
            }
            else {
                remove = true;
            }
        }
        int numNodesToMove;
        VariableLengthIntArray nbr = (VariableLengthIntArray)this.copy();

        if (add || remove) {
            numNodesToMove = MathUtil.RANDOM.nextInt(Math.min(size(), (int)(radius + 1)));
        }
        else {
            numNodesToMove = 1 + MathUtil.RANDOM.nextInt(1 + (int)radius);
        }

        if (remove) {
            removeRandomParam(nbr);
        }
        if (add) {
            addRandomParam(nbr);
        }
        moveNodes(numNodesToMove, nbr);
        return nbr;
    }

    public void setCombination(List<Integer> indices) {
         assert  indices.size() <= size() :
                 "The number of indices ("+indices.size()+") was greater than the size ("+size()+")";
         List<Parameter> newParams = new ArrayList<>(size());
         for (int i : indices) {
             newParams.add(createParam(i));
         }
         params_ = newParams;
    }

    /**
     * Globally sample the parameter space.
     *
     * @param requestedNumSamples approximate number of samples to retrieve.
     * If the problem space is small and requestedNumSamples is large, it may not be possible to return this
     * many unique samples.
     * @return some number of unique samples.
     */
    public Iterator<VariableLengthIntArray> findGlobalSamples(long requestedNumSamples) {
        return new VariableLengthGlobalSampler(this, requestedNumSamples);
    }

    /**
     * {@inheritDoc}
     * Try swapping parameters randomly until we find an improvement (if we can).
     */
    public Improvement findIncrementalImprovement(Optimizee optimizee, double jumpSize,
                                                  Improvement lastImprovement, Set<ParameterArray> cache) {
        DiscreteImprovementFinder finder = new DiscreteImprovementFinder(this);
        return finder.findIncrementalImprovement(optimizee, jumpSize, cache);
    }

    /**
     * @return get a completely random solution in the parameter space.
     */
    public ParameterArray getRandomSample() {

        List<Integer> marked = new LinkedList<>();
        for (int i=0; i<maxLength; i++) {
            if (MathUtil.RANDOM.nextDouble() > 0.5) {
                marked.add(i);
            }
        }
        List<Parameter> newParams = new ArrayList<>();
        for (int markedNode : marked) {
            newParams.add(createParam(markedNode));
        }

        return new VariableLengthIntArray(newParams, maxLength);
    }

    /**
     * @return a copy of ourselves.
     */
    public AbstractParameterArray copy() {
        VariableLengthIntArray copy = (VariableLengthIntArray) super.copy();
        copy.maxLength = maxLength;
        return copy;
    }

    /**
     * @param i the integer parameter's value
     * @return a new integer parameter.
     */
    private Parameter createParam(int i) {
        return  new IntegerParameter(i, 0, maxLength - 1, "p" + i);
    }

    private void removeRandomParam(VariableLengthIntArray nbr) {
        int indexToRemove = MathUtil.RANDOM.nextInt(size());
        assert nbr.size() > 0;
        List<Parameter> newParams = new ArrayList<>(nbr.size()-1);

        for (int i=0; i < nbr.size(); i++) {
            if (i != indexToRemove) {
                newParams.add(nbr.get(i));
            }
        }
        nbr.params_ = newParams;
    }

    private void addRandomParam(VariableLengthIntArray nbr) {

        List<Integer> freeNodes = getFreeNodes(nbr);
        int newSize = nbr.size() + 1;
        assert newSize <= maxLength;
        List<Parameter> newParams = new ArrayList<>(newSize);
        for (Parameter p : nbr.params_) {
            newParams.add(p);
        }
        int value = freeNodes.get(MathUtil.RANDOM.nextInt(freeNodes.size()));
        newParams.add(createParam(value));
        nbr.params_ = newParams;
    }

    /**
     * select num free nodes randomly and and swap them with num randomly selected marked nodes.
     * @param numNodesToMove number of nodes to move to new locations
     * @param nbr neighbor parameter array
     */
    private void moveNodes(int numNodesToMove, VariableLengthIntArray nbr) {
        List<Integer> freeNodes = getFreeNodes(nbr);
        int numSelect = Math.min(freeNodes.size(), numNodesToMove);
        List<Integer> swapNodes = selectRandomNodes(numSelect, freeNodes);

        for (int i=0; i<numSelect; i++) {
            int index = MathUtil.RANDOM.nextInt(nbr.size());
            nbr.get(index).setValue(swapNodes.get(i));
        }
    }

    private List<Integer> selectRandomNodes(int numNodesToSelect, List<Integer> freeNodes) {
        List<Integer> selected = new LinkedList<>();
        for (int i=0; i < numNodesToSelect; i++) {
            int node = freeNodes.get(MathUtil.RANDOM.nextInt(freeNodes.size()));
            selected.add(node);
            freeNodes.remove((Integer) node);
        }
        return selected;
    }

    private List<Integer> getFreeNodes(VariableLengthIntArray nbr) {
        List<Integer> freeNodes = new ArrayList<>(maxLength);
        Set<Integer> markedNodes = new HashSet<>();
        for (Parameter p : nbr.params_) {
            markedNodes.add((int)p.getValue());
        }

        for (int i = 0; i < maxLength; i++) {
            if (!markedNodes.contains(i))   {
                freeNodes.add(i);
            }
        }
        return freeNodes;
    }
}
