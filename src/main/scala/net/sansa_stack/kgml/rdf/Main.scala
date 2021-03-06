package net.sansa_stack.kgml.rdf

/**
  * Created by afshin on 10.10.17.
  */

import java.net.URI

import net.sansa_stack.rdf.spark.io.NTripleReader
import net.sansa_stack.rdf.spark.model.TripleRDD._
import org.apache.spark.sql.SparkSession

/*
    This object is for merging KBs and making unique set of predicates in two KBs
 */
object Main {

  var input1 = ""
  var input2 = ""
  var input3 = ""
  var input4 = "false"
  var input5 = "false"

  def main(args: Array[String]) = {
    println("Comparing knowledge graphs")

    if (args.headOption.isDefined) {
      input1 = args(0)
      input2 = args(1)
      input3 = args(2)
      if(args.length > 3){
        input4 = args(3)
      }
      if(args.length > 4){
        input5 = args(4)
      }

    } else {
      println("Arg 1 and 2 are paths:")
      println("Please give path to the two files that contains the dataset in N-Triples format and the experiment number")
      println("Otherwise it reads from default example data sets.")
      println(" ")
      println("Arg 3 is the experiment number")
      println("Experiment number 0: third ar General info of the two datasets.")
      println("Experiment number 1: Getting similarity between predicates")
      println("Experiment number 2: Getting similarity between literal entities")
      println("Experiment number 3: Getting similarity between non-literal (called source in jena definition) entities (Subjects and objects)")
      println(" ")
      println("Arg 4 is to Not to use collect on the main calculation")
      println("By that input 4 sets the WordNet similarity to produce RDD instead of array if arg4 is true")
      println("the default value is false. so to use collect but it requires a lot of RAM on the driver program")
      println(" ")
      println("Arg 5 is to call count() and print info of KGs (like number of literals . predicates etc)")
      println(" count makes the run slower, if you want the final result only leave it as false")
      println(" ")

      println("There is no given argument. For example you can give:")
      input1 = "datasets/dbpediaSimple.nt"
      input2 = "datasets/dbpediaSimple.nt"
//      input1 = "datasets/DBpediaAppleSmalldataset.nt"
//      input2 = "datasets/YagoAppleSmallDataset.nt"
      input3 = "0"
      input4 = "false" // defauls is true
      input5 = "false"

    }
    println("Selected run configuration is as below")
    println("KB1: " + input1)
    println("KB2: " +input2)
    println("Experiment number: " + input3)
    println("To RunWordNetWithRDD(with out collection) " + input4)
    println("To show counts of dataset and their pairs(it will be time consuming on big dataset): " + input5)
    var RunWordNetWithRDD = false
    var RunWithCountCommands = false
    if(input4 == "false" ){
      RunWordNetWithRDD = false
    } else  if(input4 == "true"){
      RunWordNetWithRDD = true
    }
    if(input5 == "false" ){
      RunWithCountCommands = false
    } else if(input5 == "true"){
      RunWithCountCommands = true
    }
    // val input2 = "datasets/dbpediamapping5k.nt"  //dbpedia-3-9-mappingbased_properties_en
    //  val input1 = "datasets/yagofact5k.nt"


    //val input1 = "datasets/dbpedia.nt"
    //val input2 = "datasets/yago.nt"
    input1 = "datasets/dbpediaSimple.nt"
    input2 = "datasets/dbpediaSimple.nt"
    val sparkSession = SparkSession.builder
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.kryoserializer.buffer.max", "1024")
      .config("spark.kryo.registrator", "net.sansa_stack.kgml.rdf.Registrator")
      .appName("Triple merger of " + input1 + " and " + input2 + " ")
      .getOrCreate()

    //    val sparkSession = SparkSession.builder()
    //      .config("spark.kryo.registrator", "net.sansa_stack.owl.spark.dataset.UnmodifiableCollectionKryoRegistrator")
    //      .appName("ManchesterSyntaxOWLAxiomsDatasetBuilderTest").master("local[*]").getOrCreate()

    val triplesRDD1 = NTripleReader.load(sparkSession, URI.create(input1)) // RDD[Triple]

    val triplesRDD2 = NTripleReader.load(sparkSession, URI.create(input2))

    var makeResult0 = false
    var makeResult1 = false
    var makeResult2 = false
    var makeResult3 = false

      if(input3 == "0"){   // General info of the two datasets
        makeResult0 = true
      }
      if(input3 == "1"){ // Getting similarity between predicates
        makeResult1 = true
      }
      if(input3 == "2"){ // Getting similarity between literal entities
        makeResult2 = true
      }
      if(input3 == "3"){ // Getting similarity between non-literal (called source in jena definition) entities (Subjects and objects)
        makeResult3 = true
      }
      makeResult0 = true
      makeResult1 = false
      if (makeResult0) {
        //##################### Get graph specs ############################
        //triplesRDD1.cache()

      //union of all triples

           //val unionEntities = triplesRDD1.getSubjects.union(triplesRDD2.getSubjects).union(triplesRDD1.getObjects.union(triplesRDD2.getObjects)).distinct()
      val unionEntities = (triplesRDD1.getSubjects ++ triplesRDD1.getObjects).distinct().zipWithUniqueId
      println("Number of unique entities in the KGs is " + unionEntities.count()) //=355
      //println("10 first unique entities  \n")
      //unionEntities.take(10).foreach(println(_))

      //convert entities and predicates1 to index of numbers
      val unionEntityIDs = (
        triplesRDD1.getSubjects
          ++ triplesRDD1.getObjects
        )
        .map(line => line.toString()).distinct.zipWithUniqueId
      println("Number of unique entity IDs in the two KGs is " + unionEntityIDs.count()) //=225!!

    }
    //distinct does not work on Node objects and the result of zipWithUniqueId become wrong
    // therefore firstly I convert node to string by .map(line => line.toString())


    //this.printGraphInfo(unifiedTriplesRDD, unionEntityIDs, unionRelationIDs)
    //var merg = new MergeKGs()

    // var cm = merg.createCoordinateMatrix(unifiedTriplesRDD, unionEntityIDs, unionRelationIDs)

    //println("matrix rows:" + cm.numRows() + "\n")
    //println("matrix cols:" + cm.numCols() + "\n")

    //find their similarity, subjects and predicates1. example> barak obama in different KBs
    // find similarites between URI of the same things in differnet  languages  of dbpeida

    //what I propose is modling it with dep neural networks
    // The training part aims to learn the semantic relationships among
    // entities and relations with the negative entities (bad entities),
    // and the goal of the prediction part is giving i triplet
    // score with the vector representations of entities and relations.

    if (makeResult1) {

      //Getting the predicates without URIs
      val predicatesWithoutURIs1 = triplesRDD1.map(_.getPredicate.getLocalName).distinct() //.zipWithIndex()
      val predicatesWithKeys1 = triplesRDD1.map(_.getPredicate.getLocalName).distinct().zipWithIndex()

      val predicatesWithoutURIs2 = triplesRDD2.map(_.getPredicate.getLocalName).distinct() //.zipWithIndex()
      val predicatesWithKeys2 = triplesRDD2.map(_.getPredicate.getLocalName).distinct().zipWithIndex()


      if (RunWithCountCommands) {
        println("Predicates without URI in KG1 are " + predicatesWithoutURIs1.count()) //313
        predicatesWithoutURIs1.distinct().take(5).foreach(println)
        println("first predicate " + predicatesWithoutURIs1.take(predicatesWithoutURIs1.count().toInt).apply(0))

        println("Predicates without URI in KG2 are " + predicatesWithoutURIs2.count()) //81
        predicatesWithoutURIs2.distinct().take(5).foreach(println)
        println("first predicate " + predicatesWithoutURIs2.first())
        //println("first predicate "+ predicatesWithoutURIs2.take(predicatesWithoutURIs2.count().toInt).apply(1))
      }

      var partitions = new Partitioning(sparkSession.sparkContext)
      partitions.predicatesRDDPartitioningByKey(predicatesWithKeys1, predicatesWithKeys2)

      //############################ Getting similarity between predicates ####################################
      println("//############################ Getting similarity between predicates ####################################")
      var preSim = new PredicatesSimilarity(sparkSession.sparkContext)
      var eval: Evaluation = new Evaluation()

      if (!RunWordNetWithRDD) {
        //this creates array:
        val similarPredicates = preSim.matchPredicatesByWordNet(predicatesWithoutURIs1, predicatesWithoutURIs2)
        similarPredicates.take(similarPredicates.length).foreach(println(_))

        // this works with array similarPredicates:
        var compresionRatio = eval.compressionRatio(predicatesWithoutURIs1.count() + predicatesWithoutURIs2.count(), similarPredicates.length)
        println("Compression Ration in predicates merging = " + compresionRatio + "%")

      } else {
        val similarPredicates = preSim.matchPredicatesByWordNetRDD(predicatesWithoutURIs1, predicatesWithoutURIs2)
        //similarPredicates.saveAsTextFile(outputDir+ "/predicates")
        if (RunWithCountCommands) {
          var compresionRatio = eval.compressionRatio(predicatesWithoutURIs1.count() + predicatesWithoutURIs2.count(), similarPredicates.count())
          println("Compression Ration in predicates merging = " + compresionRatio + "%")
        }
      }
    }

    if (makeResult2) {
      println("//############################ Getting similarity between literal entities  ####################################")

      var preSim = new PredicatesSimilarity(sparkSession.sparkContext)

      val literalObjects1 = triplesRDD1.filter(_.getObject.isLiteral).map(_.getObject.getLiteralValue.toString).distinct()
      val literalObjects2 = triplesRDD2.filter(_.getObject.isLiteral).map(_.getObject.getLiteralValue.toString).distinct()

      if(RunWithCountCommands) {
        println("Literal entities (without URI) in KG1 are " + literalObjects1.count())

        literalObjects1.distinct().take(5).foreach(println)
        println("First literal entity " + literalObjects1.take(literalObjects1.count().toInt).apply(0))
        println("Literal entities (without URI) in KG2 are " + literalObjects2.count()) //81

        literalObjects2.distinct().take(5).foreach(println)
        println("First literal entity" + literalObjects2.first())
      }
      val entSim = new EntitiesSimilarity(sparkSession.sparkContext)
      var eval: Evaluation = new Evaluation()
      if (!RunWordNetWithRDD) {
        //this creates array:
        val similarLiteralEntities = entSim.matchLiteralEntitiesByWordNet(literalObjects1, literalObjects2)
        //similarLiteralEntities.take(similarLiteralEntities.length).foreach(println(_))

        var eval: Evaluation = new Evaluation()
        // this works with array similarLiteralEntities
        if(RunWithCountCommands) {
          val compressionRatio2 = eval.compressionRatio(literalObjects1.count() + literalObjects2.count(), similarLiteralEntities.length)
          println("Compression Ration in literal entities merging = " + compressionRatio2 + "%")
        }
      }else{
          val similarLiteralEntities = entSim.matchLiteralEntitiesByWordNetRDD(literalObjects1, literalObjects2)
          //similarLiteralEntities.saveAsTextFile(outputDir+ "/litEntites")

        if(RunWithCountCommands) {
          val compressionRatio2 = eval.compressionRatio(literalObjects1.count() + literalObjects2.count(), similarLiteralEntities.count())
          println("Compression Ration in literal entities merging = " + compressionRatio2 + "%")
        }
      }


    }

    if (makeResult3) {
      println("//############################ Getting similarity between non-literal (called source in jena definition) entities (Subjects and objects) ####################################")
      // exactly like predicates:

      val objects1 = triplesRDD1.filter(_.getObject.isURI).map(_.getObject.getLocalName).distinct()
      val objects2 = triplesRDD2.filter(_.getObject.isURI).map(_.getObject.getLocalName).distinct()
      val subjects1 = triplesRDD1.map(_.getSubject.getLocalName).distinct()
      val subjects2 = triplesRDD2.map(_.getSubject.getLocalName).distinct()
      val entitiy1 = objects1 ++ subjects1
      val entitiy2 = objects2 ++ subjects2

      if(RunWithCountCommands) {
        println("Entities (with URI) in KG1 are " + entitiy1.count())
        entitiy1.distinct().take(5).foreach(println)
        println("First literal entity " + entitiy1.take(entitiy1.count().toInt).apply(0))

        println("Entities (with URI) in KG2 are " + entitiy2.count()) //81
        entitiy2.distinct().take(5).foreach(println)
        println("First literal entity" + entitiy2.first())
      }
      val subSim = new EntitiesSimilarity(sparkSession.sparkContext)
      var eval: Evaluation = new Evaluation()

      if (!RunWordNetWithRDD) {
        //this creates array:
        val similarEntities = subSim.matchLiteralEntitiesByWordNet(entitiy1, entitiy2)
        //similarEntities.take(similarEntities.length).foreach(println(_))

        // this works with array of similarEntities
        if(RunWithCountCommands) {
          val compressionRatio3 = eval.compressionRatio(entitiy1.count() + entitiy2.count(), similarEntities.length)
          println("Compression Ratio in non-literal Entities merging = " + compressionRatio3 + "%")
        }
      }else{
        val similarEntities = subSim.matchLiteralEntitiesByWordNetRDD(entitiy1,entitiy2)
        //similarEntities.saveAsTextFile(outputDir + "/nonlitEntites")
        if(RunWithCountCommands) {
          val compressionRatio3 = eval.compressionRatio(entitiy1.count() + entitiy2.count(), similarEntities.count())
          println("Compression Ratio in non-literal Entities merging = " + compressionRatio3 + "%")
        }
      }
    }
    sparkSession.stop
  }

}