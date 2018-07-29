package com.barrybecker4.optimization.parameter

import com.barrybecker4.optimization.parameter.distancecalculators.{DistanceCalculator, MagnitudeIgnoredDistanceCalculator}
import com.barrybecker4.optimization.parameter.types.{IntegerParameter, Parameter}
import org.scalatest.{BeforeAndAfter, FunSuite}
import com.barrybecker4.common.testsupport.strip
import scala.util.Random
import com.barrybecker4.optimization.parameter.VariableLengthIntSet.createParam


class VariableLengthIntSetSuite extends FunSuite with BeforeAndAfter{

  private var params: VariableLengthIntSet = _
  private var rnd: Random = _
  before {
    rnd = new Random(1)
  }

  test("serialization of typical") {
    params = createDistArray(Array(2, -1, 3, -4))
    assertResult(strip("""
            |parameter[0] = p2 = 2.0 [0, 2.0]
            |parameter[1] = p-1 = -1.00 [-1.00, 0]
            |parameter[2] = p3 = 3.0 [0, 3.0]
            |parameter[3] = p-4 = -4.0 [-4.0, 0]
            |""")) { params.toString }
    assertResult(4) { params.getMaxLength }
  }

  test("serialization when duplicates present") {
    params = createDistArray(Array(2, -1, 3, -1))
    assertResult(strip("""
           |parameter[0] = p2 = 2.0 [0, 2.0]
           |parameter[1] = p-1 = -1.00 [-1.00, 0]
           |parameter[2] = p3 = 3.0 [0, 3.0]
           |""")) { params.toString }
    assertResult(3) { params.getMaxLength }
  }

  test("serialization when several duplicates present") {
    params = createDistArray(Array(2, -1, 2, 3, 2, -1))
    assertResult(strip("""
           |parameter[0] = p2 = 2.0 [0, 2.0]
           |parameter[1] = p-1 = -1.00 [-1.00, 0]
           |parameter[2] = p3 = 3.0 [0, 3.0]
           |""")) { params.toString }
    assertResult(3) { params.getMaxLength }
  }

  test("Similarity when identically equal") {
    params = createDistArray(Array(2, -1, 3, -4))
    assertResult(0.0) { params.distance(params) }
    assertResult(params) {params}
  }

  test("Similarity when equal") {
    params = createDistArray(Array(2, -1, 3, -4))
    val otherParams = createDistArray(Array(2, -1, 3, -4))
    assertResult(0.0) { params.distance(otherParams) }
    assertResult(params) {otherParams}
  }

  test("Similarity when equal, but values in different order") {
    params = createDistArray(Array(2, -1, 3, -4))
    val otherParams = createDistArray(Array(2, 3, -1, -4))
    assertResult(0.0) { params.distance(otherParams) }
    assertResult(params) {otherParams}
  }

  test("Similarity when equal size but different values") {
    params = createDistArray(Array(2, -1, 3, -4))
    val otherParams = createDistArray(Array(2, -1, 3, -2))
    assertResult(2.0) { params.distance(otherParams) }
    assert(params != otherParams)
  }

  test("Similarity when equal size but very different values") {
    params = createDistArray(Array(2, -99, 3, -1))
    val otherParams = createDistArray(Array(2, -1, 30, -2))
    assertResult(124.0) { params.distance(otherParams) }
    assert(params != otherParams)
  }

  test("Similarity when unequal sizes") {
    params = createDistArray(Array(2, -1, 3, -4))
    val otherParams = createDistArray(Array(2, -1, 3, -2, 1))
    assertResult(3.0) { params.distance(otherParams) }
    assert(params != otherParams)
  }

  test("getCombination with values in order") {
    params = createDistArray(Array(2, -1, 3, -4))
    val combo = params.getCombination(Array(2, 3))
    val expected = createIntArray(Array(3, -4), Array(2, -1, 3, -4))
    assertResult(expected.toString) {combo.toString}
    assertResult(expected) { combo }
  }

  test("getCombination with values out of order") {
    params = createDistArray(Array(2, -1, 3, -4))
    val combo = params.getCombination(Array(3, 2))
    val expected = createIntArray(Array(3, -4), Array(2, -1, 3, -4))
    assertResult(expected) { combo }
  }

  test("getCombination of all values") {
    val combo = params.getCombination(Array(1, 0, 3, 2))
    val expected = createIntArray(Array(2, -1, 3, -4), Array(2, -1, 3, -4))
    assertResult(expected) { combo }
  }

  test("GetSamplePopulationSizeWhenSmall"){
    params = createDistArray(Array(2, -1, 3, -4))
    assertResult(256) { params.getSamplePopulationSize }
  }

  test("GetSamplePopulationSizeWhenLarge") {
    params = createDistArray(Array(2, -1, 3, -1, 3, -4, -2, -3, 5, -9, 6, -17, 11))
    assertResult(4000) { params.getSamplePopulationSize }
  }

  test("Find 0 GlobalSamples") {
    params = createDistArray(Array(2, -1, 3, -1))
    assertThrows[NoSuchElementException] {
      getListFromIterator(params.findGlobalSamples(0))
    }
  }

  test("Find 1 GlobalSamples") {
    params = createDistArray(Array(2, -1, 3))
    val samples = getListFromIterator(params.findGlobalSamples(1))
    assertResult(1) { samples.length }
    val expParams = Array(
      createDistArray(Seq(-1), Array(2, -1, 3))
    )
    assertResult(expParams) { samples }
  }

  test("Find 2 GlobalSamples") {
    params = createDistArray(Array(2, -1, 3, -4))
    val samples = getListFromIterator(params.findGlobalSamples(2))
    assertResult(2) { samples.length }
  }

  test("Find 3 GlobalSamples") {
    params = createDistArray(Array(2, -1, 3, -4))
    val samples = getListFromIterator(params.findGlobalSamples(3))
    assertResult(3) { samples.length }
  }

  /* This may a problem because there are not 4 global samples of 3 values.
     The only combinations are 2 -1, -1 3, 2 3. */
  test("Find 4 GlobalSamples") {
    params = createDistArray(Array(2, -1, 3))
    val samples = getListFromIterator(params.findGlobalSamples(4))
    assertResult(4) { samples.length }
    val expParams = Array(
      createDistArray(Seq(2), Array(2, -1, 3)),
      createDistArray(Seq(-1), Array(2, -1, 3)),
      createDistArray(Seq(2, -1), Array(2, -1, 3)),
      createDistArray(Seq(3), Array(2, -1, 3))
    )
    assertResult(expParams) { samples }
  }

  /* This may a problem because there are not 4 global samples of 3 values.
     The only combinations are 2 -1, -1 3, 2 3. */
  test("Find 9 GlobalSamples when only 7") {
    params = createDistArray(Array(2, -1, 3))
    val samples = getListFromIterator(params.findGlobalSamples(9))
    assertResult(7) { samples.length }
  }

  test("Find 10 GlobalSamples") {
    params = createDistArray(Array(2, -1, 3, -5, 3, -4, -2, -3, 5, -9, 6))
    val samples = getListFromIterator(params.findGlobalSamples(10))
    assertResult(10)  { samples.length }
  }

  test("Find 15 GlobalSamples when only 15") {
    params = createDistArray(Array(2, -1, 3, -4))
    val samples = getListFromIterator(params.findGlobalSamples(15))
    assertResult(15) { samples.length }
  }

  test("Find 97 GlobalSamples from array of 6 elements") {
    params = createDistArray(Array(2, -1, 3, -4, -7, 9, 7))
    val samples = getListFromIterator(params.findGlobalSamples(97))
    assertResult(97) { samples.length }
  }

  test("Find 97 GlobalSamples when only 15") {
    params = createDistArray(Array(2, -1, 3, -4))
    val samples = getListFromIterator(params.findGlobalSamples(97))
    assertResult(15) { samples.length }
  }

  test("random neighbor (4 params). r = 1.6") {
    params = createDistArray(Array(2, -1, 3, -4))
    val radius = 1.6

    val nbr = params.getRandomNeighbor(radius)
    assertResult(strip("""
         |parameter[0] = p2 = 2.0 [0, 2.0]
         |parameter[1] = p3 = 3.0 [0, 3.0]
         |parameter[2] = p-4 = -4.0 [-4.0, 0]
         |""")) { nbr.toString }

    val nbr2 = params.getRandomNeighbor(radius)
    assertResult(strip("""
         |parameter[0] = p-1 = -1.00 [-1.00, 0]
         |parameter[1] = p3 = 3.0 [0, 3.0]
         |parameter[2] = p-4 = -4.0 [-4.0, 0]
         |""")) { nbr2.toString }

    val nbr3 = params.getRandomNeighbor(radius)
    assertResult(strip("""
         |parameter[0] = p-1 = -1.00 [-1.00, 0]
         |parameter[1] = p3 = 3.0 [0, 3.0]
         |parameter[2] = p-4 = -4.0 [-4.0, 0]
         |""")) { nbr2.toString }
  }

  test("random neighbor (4 params). r = 1.2") {
    params = createDistArray(Array(2, -1, 3, -4))
    val radius = 1.2

    val nbr = params.getRandomNeighbor(radius)
    assertResult(strip("""
        |parameter[0] = p2 = 2.0 [0, 2.0]
        |parameter[1] = p3 = 3.0 [0, 3.0]
        |parameter[2] = p-4 = -4.0 [-4.0, 0]
        |""")) { nbr.toString }

    val nbr2 = params.getRandomNeighbor(radius)
    assertResult(strip("""
       |parameter[0] = p-1 = -1.00 [-1.00, 0]
       |parameter[1] = p3 = 3.0 [0, 3.0]
       |parameter[2] = p-4 = -4.0 [-4.0, 0]
       |""")) { nbr2.toString }

    val nbr3 = params.getRandomNeighbor(radius)
    assertResult(strip("""
       |parameter[0] = p-1 = -1.00 [-1.00, 0]
       |parameter[1] = p3 = 3.0 [0, 3.0]
       |parameter[2] = p-4 = -4.0 [-4.0, 0]
       |""")) { nbr2.toString }
  }

  test("random neighbor (4 params). r =  0.3") {
    params = createDistArray(Array(2, -1, 3, -4))
    val radius = 0.3

    val nbr = params.getRandomNeighbor(radius)
    assertResult(strip("""
        |parameter[0] = p2 = 2.0 [0, 2.0]
        |parameter[1] = p3 = 3.0 [0, 3.0]
        |parameter[2] = p-4 = -4.0 [-4.0, 0]
        |""")) { nbr.toString }

    val nbr2 = params.getRandomNeighbor(radius)
    assertResult(strip("""
       |parameter[0] = p-1 = -1.00 [-1.00, 0]
       |parameter[1] = p3 = 3.0 [0, 3.0]
       |parameter[2] = p-4 = -4.0 [-4.0, 0]
       |""")) { nbr2.toString }

    val nbr3 = params.getRandomNeighbor(radius)
    assertResult(strip("""
       |parameter[0] = p2 = 2.0 [0, 2.0]
       |parameter[1] = p-1 = -1.00 [-1.00, 0]
       |parameter[2] = p3 = 3.0 [0, 3.0]
       |""")) { nbr3.toString }
  }

  test("random neighbor (4 params). r =  0.1") {
    params = createDistArray(Array(2, -1, 3, -4))
    val radius = 0.1

    val nbr = params.getRandomNeighbor(radius)
    assertResult(strip("""
                         |parameter[0] = p2 = 2.0 [0, 2.0]
                         |parameter[1] = p3 = 3.0 [0, 3.0]
                         |parameter[2] = p-4 = -4.0 [-4.0, 0]
                         |""")) { nbr.toString }

    val nbr2 = params.getRandomNeighbor(radius)
    assertResult(strip("""
                         |parameter[0] = p2 = 2.0 [0, 2.0]
                         |parameter[1] = p-1 = -1.00 [-1.00, 0]
                         |parameter[2] = p-4 = -4.0 [-4.0, 0]
                         |""")) { nbr2.toString }

    val nbr3 = params.getRandomNeighbor(radius)
    assertResult(strip("""
                         |parameter[0] = p-1 = -1.00 [-1.00, 0]
                         |parameter[1] = p3 = 3.0 [0, 3.0]
                         |parameter[2] = p-4 = -4.0 [-4.0, 0]
                         |""")) { nbr3.toString }
  }

  test("random neighbor (all 11 params). r = 1.2") {
    params = createDistArray(Array(2, -1, 3, -5, 3, -4, -2, -3, 5, -9, 6))
    val radius = 1.2
    val nbr = params.getRandomNeighbor(radius)
    assertResult(strip("""
        |parameter[0] = p2 = 2.0 [0, 2.0]
        |parameter[1] = p-1 = -1.00 [-1.00, 0]
        |parameter[2] = p3 = 3.0 [0, 3.0]
        |parameter[3] = p-4 = -4.0 [-4.0, 0]
        |parameter[4] = p-2 = -2.0 [-2.0, 0]
        |parameter[5] = p-3 = -3.0 [-3.0, 0]
        |parameter[6] = p5 = 5.0 [0, 5.0]
        |parameter[7] = p-9 = -9.0 [-9.0, 0]
        |parameter[8] = p6 = 6.0 [0, 6.0]
        |""")) { nbr.toString }

    val nbr2 = params.getRandomNeighbor(radius)
    assertResult(strip("""
       |parameter[0] = p2 = 2.0 [0, 2.0]
       |parameter[1] = p-1 = -1.00 [-1.00, 0]
       |parameter[2] = p3 = 3.0 [0, 3.0]
       |parameter[3] = p-5 = -5.0 [-5.0, 0]
       |parameter[4] = p-4 = -4.0 [-4.0, 0]
       |parameter[5] = p-2 = -2.0 [-2.0, 0]
       |parameter[6] = p-3 = -3.0 [-3.0, 0]
       |parameter[7] = p5 = 5.0 [0, 5.0]
       |parameter[8] = p-9 = -9.0 [-9.0, 0]
       |""")) { nbr2.toString }
  }

  test("swap nodes (11 params). r =  0.3") {
    params = createDistArray(Array(2, -1, 3, -5, 3, -4, -2, -3, 5, -9, 6))
    val radius = 0.3
    val nbr = params.getRandomNeighbor(radius)
    assertResult(strip("""
        |parameter[0] = p2 = 2.0 [0, 2.0]
        |parameter[1] = p-1 = -1.00 [-1.00, 0]
        |parameter[2] = p3 = 3.0 [0, 3.0]
        |parameter[3] = p-4 = -4.0 [-4.0, 0]
        |parameter[4] = p-2 = -2.0 [-2.0, 0]
        |parameter[5] = p-3 = -3.0 [-3.0, 0]
        |parameter[6] = p5 = 5.0 [0, 5.0]
        |parameter[7] = p-9 = -9.0 [-9.0, 0]
        |parameter[8] = p6 = 6.0 [0, 6.0]
        |""")) { nbr.toString }

    val nbr2 = params.getRandomNeighbor(radius)
    assertResult(strip("""
         |parameter[0] = p2 = 2.0 [0, 2.0]
         |parameter[1] = p-1 = -1.00 [-1.00, 0]
         |parameter[2] = p3 = 3.0 [0, 3.0]
         |parameter[3] = p-5 = -5.0 [-5.0, 0]
         |parameter[4] = p-4 = -4.0 [-4.0, 0]
         |parameter[5] = p-2 = -2.0 [-2.0, 0]
         |parameter[6] = p-3 = -3.0 [-3.0, 0]
         |parameter[7] = p5 = 5.0 [0, 5.0]
         |parameter[8] = p-9 = -9.0 [-9.0, 0]
         |""")) { nbr2.toString }
  }

  test("random neighbor (5 of 11 params). r = 1.2") {
    params = createDistArray(Seq(-4, -9, 2, 6, 3), Array(2, -1, 3, -5, 3, -4, -2, -3, 5, -9, 6))
    val radius = 1.2
    val nbr = params.getRandomNeighbor(radius)
    assertResult(strip("""
         |parameter[0] = p-4 = -4.0 [-4.0, 0]
         |parameter[1] = p-9 = -9.0 [-9.0, 0]
         |parameter[2] = p2 = 2.0 [0, 2.0]
         |parameter[3] = p6 = 6.0 [0, 6.0]
         |parameter[4] = p3 = 3.0 [0, 3.0]
         |""")) { nbr.toString }

    val nbr2 = params.getRandomNeighbor(radius)
    assertResult(strip("""
         |parameter[0] = p-4 = -4.0 [-4.0, 0]
         |parameter[1] = p-9 = -9.0 [-9.0, 0]
         |parameter[2] = p2 = 2.0 [0, 2.0]
         |parameter[3] = p6 = 6.0 [0, 6.0]
         |parameter[4] = p3 = 3.0 [0, 3.0]
         |""")) { nbr2.toString }
  }

  test("swap nodes (5 of 11 params). r =  0.3") {
    params = createDistArray(Seq(-4, -9, 2, 6, 3), Array(2, -1, 3, -5, 3, -4, -2, -3, 5, -9, 6))
    val radius = 0.3
    val nbr = params.getRandomNeighbor(radius)
    assertResult(strip("""
         |parameter[0] = p-4 = -4.0 [-4.0, 0]
         |parameter[1] = p-9 = -9.0 [-9.0, 0]
         |parameter[2] = p2 = 2.0 [0, 2.0]
         |parameter[3] = p6 = 6.0 [0, 6.0]
         |parameter[4] = p3 = 3.0 [0, 3.0]
         |""")) { nbr.toString }

    val nbr2 = params.getRandomNeighbor(radius)
    assertResult(strip("""
         |parameter[0] = p-4 = -4.0 [-4.0, 0]
         |parameter[1] = p-9 = -9.0 [-9.0, 0]
         |parameter[2] = p2 = 2.0 [0, 2.0]
         |parameter[3] = p6 = 6.0 [0, 6.0]
         |parameter[4] = p3 = 3.0 [0, 3.0]
         |""")) { nbr2.toString }
  }
  private def getListFromIterator(iter: Iterator[VariableLengthIntSet]): Array[VariableLengthIntSet] =
    iter.toArray

  private def createArray(dCalc: DistanceCalculator, numberList: IndexedSeq[Int]) = {
    val params = for (i <- numberList) yield createParam(i)
    new VariableLengthIntSet(params, numberList, dCalc, rnd)
  }

  def createIntArray(intParams: IndexedSeq[Int], fullSeq: IndexedSeq[Int]): VariableLengthIntSet = {
    val params = for (num <- intParams) yield createParam(num)
    VariableLengthIntSet.createInstance(params, fullSeq, rnd)
  }

  private def createDistArray(numberList: IndexedSeq[Int]) = {
    var numSet = Set[Int]()
    val params = for (num <- numberList if !numSet.contains(num)) yield {
      numSet = numSet + num
      createParam(num)
    }
    VariableLengthIntSet.createInstance(params, numberList, rnd)
  }

  private def createDistArray(numbers: Seq[Int], fullList: IndexedSeq[Int]) = {
    assert(numbers.size == numbers.toSet.size, "There must not be duplicates")
    val params = for (i <- numbers) yield createParam(i)
    VariableLengthIntSet.createInstance(params.toIndexedSeq, fullList, rnd)
  }

  def createDistIgnoredArray(numberList: IndexedSeq[Int]): VariableLengthIntSet =
    createArray(new MagnitudeIgnoredDistanceCalculator, numberList)
}
