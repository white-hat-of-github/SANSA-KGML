/**
  * Created by afshin on 12.12.17.
  */

package net.sansa_stack.kgml.rdf

object TestJaccardSimilarity {

  def main(args: Array[String]): Unit = {


    var similarityThreshold = 0.6
    val similarityHandler = new SimilarityHandler(similarityThreshold)

    var longPredicate1 = "isCenterOf"
    var longPredicate2 = "isWorkingWith"
    var sim = similarityHandler.jaccardPredicateSimilarityWithWordNet(longPredicate1, longPredicate2)
    print("similarity of " + longPredicate1 + " and " + longPredicate2 + " is " + sim + "\n")

    longPredicate1 = "isManagerOf"
    longPredicate2 = "isDirectorOf"
    sim = similarityHandler.jaccardPredicateSimilarityWithWordNet(longPredicate1, longPredicate2)
    print("similarity of " + longPredicate1 + " and " + longPredicate2 + " is " + sim + "\n")

    longPredicate1 = "car"
    longPredicate2 = "Auto"
    sim = similarityHandler.jaccardPredicateSimilarityWithWordNet(longPredicate1, longPredicate2)
    print("similarity of " + longPredicate1 + " and " + longPredicate2 + " is " + sim + "\n")


    similarityThreshold = 0.6
    similarityHandler.setThreshold(similarityThreshold)
    longPredicate1 = "composer"
    longPredicate2 = "musicComposer"
    sim = similarityHandler.jaccardPredicateSimilarityWithWordNet(longPredicate1, longPredicate2)
    print("similarity of " + longPredicate1 + " and " + longPredicate2 + " is " + sim + "\n")

    //from drugbank dataset and dbpedia predicate comparison
    //1
    similarityThreshold = 0.7
    similarityHandler.setThreshold(similarityThreshold)
    longPredicate1 = "casNumber"
    longPredicate2 = "casRegistryNumber"
    sim = similarityHandler.jaccardPredicateSimilarityWithWordNet(longPredicate1,longPredicate2)
    print("similarity of " + longPredicate1 + " and " + longPredicate2 + " is " + sim + "\n")

    similarityThreshold = 0.5
    similarityHandler.setThreshold(similarityThreshold)
    longPredicate1 = "director"
    longPredicate2 = "directed"
    sim = similarityHandler.jaccardPredicateSimilarityWithWordNet(longPredicate1,longPredicate2)
    print("similarity of " + longPredicate1 + " and " + longPredicate2 + " is " + sim + "\n")

    //from drugbank dataset and dbpedia predicate comparison
    //1
    similarityThreshold = 0.7
    similarityHandler.setThreshold(similarityThreshold)
    longPredicate1 = "120993-53-5"
    longPredicate2 = "cas:120993-53-5"
    sim = similarityHandler.jaccardPredicateSimilarityWithWordNet(longPredicate1, longPredicate2)
    print("similarity of " + longPredicate1 + " and " + longPredicate2 + " is " + sim + "\n")

    similarityThreshold = 0.7
    similarityHandler.setThreshold(similarityThreshold)
    longPredicate1 = "120993-53-5"
    longPredicate2 = "cas:120993-53-5"
    sim = similarityHandler.jaccardLiteralSimilarityWithLevenshtein(longPredicate1, longPredicate2)
    print("similarity of " + longPredicate1 + " and " + longPredicate2 + " is " + sim + "\n")

    longPredicate1 = "120993-53-5"
    longPredicate2 = "cas:120993-53-5"
    sim = similarityHandler.jaccardLiteralSimilarityWithLevenshtein(longPredicate1, longPredicate2)
    print("similarity of " + longPredicate1 + " and " + longPredicate2 + " is " + sim + "\n")

    longPredicate1 = "<Person"
    longPredicate2 = "<Person"
    sim = similarityHandler.getSimilarity(longPredicate1, longPredicate2)
    print("similarity of " + longPredicate1 + " and " + longPredicate2 + " is " + sim + "\n")

    longPredicate1 = "name"
    longPredicate2 = "phone-numer"
    sim = similarityHandler.jaccardPredicateSimilarityWithWordNet(longPredicate1, longPredicate2)
    print("similarity of " + longPredicate1 + " and " + longPredicate2 + " is " + sim + "\n")
  }



}