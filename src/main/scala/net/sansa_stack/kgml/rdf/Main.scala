package net.sansa_stack.kgml.rdf

/**
  * Created by afshin on 10.10.17.
  */

import java.io.{ByteArrayInputStream, File, PrintWriter}
import java.net.URI

import breeze.linalg.max
import com.sun.rowset.internal.Row
import net.didion.jwnl.data.POS
import net.sansa_stack.kgml.rdf.wordnet.WordNet

import scala.collection.{immutable, mutable}
import org.apache.spark.sql.{Row, SparkSession}
import net.sansa_stack.rdf.spark.io.NTripleReader
import net.sansa_stack.rdf.spark.model.{JenaSparkRDD, JenaSparkRDDOps}
import net.sansa_stack.rdf.spark.model.TripleRDD._
import net.sansa_stack.rdf.spark.model.JenaSparkRDD
import org.aksw.jena_sparql_api.utils.Triples
import org.apache.jena.graph
import org.apache.jena.graph.{Node, Node_URI}
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}
import org.apache.spark.rdd.{PairRDDFunctions, RDD, RDDOperationScope}
import net.didion.jwnl.data.POS

import scala.collection.JavaConverters._

/*
    This object is for merging KBs and making unique set of predicates in two KBs
 */
object Main {

  def main(args: Array[String]) = {

    val input1 = "src/main/resources/dbpediaOnlyAppleobjects.nt"
    val input2 = "src/main/resources/yagoonlyAppleobjects.nt"

    val sparkSession = SparkSession.builder
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .appName("Triple reader example (" + input1 + ")")
      .getOrCreate()

    val triplesRDD1 = NTripleReader.load(sparkSession, URI.create(input1))
    val triplesRDD2 = NTripleReader.load(sparkSession, URI.create(input2))

    //##################### Get graph specs ############################
    triplesRDD1.cache()
    println("Number of triples in the first KG\n")   //dbpedia 2457
    println(triplesRDD1.count().toString)

    println("Number of triples in the second KG\n") //yago 738
    println(triplesRDD2.count().toString)


    val subject1 = triplesRDD1.map(f => (f.getSubject, 1))
    val subject1Count = subject1.reduceByKey(_ + _)
    println("SubjectCount in the first KG \n")
    subject1Count.take(5).foreach(println(_))

    val predicates1 = triplesRDD1.map(_.getPredicate).distinct
    println("Number of unique predicates in the first KB is "+predicates1.count()) //333
    val predicates2 = triplesRDD2.map(_.getPredicate).distinct
    println("Number of unique predicates in the second KB is "+predicates2.count()) //83

    // Storing the unique predicates in separated files, one for DBpedia and one for YAGO
    val predicatesKG1 = triplesRDD1.map(f => (f.getPredicate, 1))
    val predicate1Count = predicatesKG1.reduceByKey(_ + _)
    //predicates1.take(5).foreach(println(_))
    //val writer1 = new PrintWriter(new File("src/main/resources/DBpedia_Predicates.nt" ))
    //predicate1Count.collect().sortBy(f=> f._2  * -1 ).foreach(x=>{writer1.write(x.toString()+ "\n")})
    //writer1.close()


    val predicatesKG2 = triplesRDD2.map(f => (f.getPredicate, 1))
    val predicate2Count = predicatesKG2.reduceByKey(_ + _)
    //val writer2 = new PrintWriter(new File("src/main/resources/YAGO_Predicates.nt" ))
    //predicate2Count.collect().sortBy(f=> f._2  * -1 ).foreach(x=>{writer2.write(x.toString()+ "\n")})
    //writer2.close()
    //######################### Creating union for the two RDD triples##############################
    //union of all triples
    val unifiedTriplesRDD = triplesRDD1.union(triplesRDD2)

    //union all predicates1
    val unionRelation = triplesRDD1.getPredicates.union(triplesRDD2.getPredicates).distinct()
    println("Number of unique predicates1 in the two KGs is "+ unionRelation.count()) //=413 it should be 333+83=416 which means we have 3 predicates1 is the same
    println("10 first unique relations  \n")
    unionRelation.take(10).foreach(println(_))

    val unionRelationIDsssss = (triplesRDD1.getPredicates.union(triplesRDD2.getPredicates)).distinct().zipWithUniqueId
    //val unionRelationIDs = (triplesRDD1.getPredicates ++ triplesRDD2.getPredicates).distinct()
    println("Number of unique relationIDssssss in the two KGs is "+ unionRelationIDsssss.count())//413
    println("10 first unique relationIDssssss  \n")
    unionRelationIDsssss.take(10).foreach(println(_))

    //merging the middle row:  take unique list of the union of predicates1
    //IDs converted to string
    val unionRelationIDs = (triplesRDD1.getPredicates ++ triplesRDD2.getPredicates)
      .map(line => line.toString()).distinct.zipWithUniqueId
    //val unionRelationIDs = (triplesRDD1.getPredicates ++ triplesRDD2.getPredicates).distinct()
    println("Number of unique relationIDs in the two KGs is "+ unionRelationIDs.count())//413
    println("10 first unique relationIDs  \n")
    unionRelationIDs.take(10).foreach(println(_))

    //val unionEntities = triplesRDD1.getSubjects.union(triplesRDD2.getSubjects).union(triplesRDD1.getObjects.union(triplesRDD2.getObjects)).distinct()
    val unionEntities = (triplesRDD1.getSubjects++ triplesRDD1.getObjects++ triplesRDD2.getSubjects++ triplesRDD2.getObjects).distinct().zipWithUniqueId
    println("Number of unique entities in the two KGs is "+ unionEntities.count()) //=355
    //println("10 first unique entities  \n")
    //unionEntities.take(10).foreach(println(_))

    //convert entities and predicates1 to index of numbers
    val unionEntityIDs = (
      triplesRDD1.getSubjects
        ++ triplesRDD1.getObjects
        ++ triplesRDD2.getSubjects
        ++ triplesRDD2.getObjects)
      .map(line => line.toString()).distinct.zipWithUniqueId
    println("Number of unique entity IDs in the two KGs is "+ unionEntityIDs.count()) //=225!!


    //distinct does not work on Node objects and the result of zipWithUniqueId become wrong
    // therefore firstly I convert node to string by .map(line => line.toString())


    //this.printGraphInfo(unifiedTriplesRDD, unionEntityIDs, unionRelationIDs)
    var merg = new MergeKGs()

    var cm = merg.createCoordinateMatrix(unifiedTriplesRDD, unionEntityIDs, unionRelationIDs)

    println("matrix rows:" + cm.numRows() + "\n")
    println("matrix cols:" + cm.numCols() + "\n")

    //find their similarity, subjects and predicates1. example> barak obama in different KBs
    // find similarites between URI of the same things in differnet  languages  of dbpeida

    //what I propose is modling it with dep neural networks
    // The training part aims to learn the semantic relationships among
    // entities and relations with the negative entities (bad entities),
    // and the goal of the prediction part is giving i triplet
    // score with the vector representations of entities and relations.




    //Getting the predicates without URIs
    val predicatesWithoutURIs1 = triplesRDD1.map(_.getPredicate.getLocalName).distinct()
    println("Predicates without URI in KG1 are "+ predicatesWithoutURIs1.count()) //313
    predicatesWithoutURIs1.distinct().take(5).foreach(println)
    println("first predicate "+ predicatesWithoutURIs1.take(predicatesWithoutURIs1.count().toInt).apply(0))

    val predicatesWithoutURIs2 = triplesRDD2.map(_.getPredicate.getLocalName).distinct()
    println("Predicates without URI in KG2 are "+ predicatesWithoutURIs2.count()) //81
    predicatesWithoutURIs2.distinct().take(5).foreach(println)
    println("first predicate "+ predicatesWithoutURIs2.first())
    //println("first predicate "+ predicatesWithoutURIs2.take(predicatesWithoutURIs2.count().toInt).apply(1))

    //Getting similarity between predicates
    val wn = WordNet()
    val s:SimilarityHandler = new SimilarityHandler(10)
    //val sim = s.getMeanWordNetNounSimilarity(predicatesWithoutURIs1.take(predicatesWithoutURIs1.count().toInt).apply(0),predicatesWithoutURIs2.take(predicatesWithoutURIs2.count().toInt).apply(4))
    val sim = s.getMeanWordNetNounSimilarity("dog","hit")
    //val sim = s.getMeanWordNetNounSimilarity("imagesize","containerfor") //not work because of it has more than one word
    println("Mean WordNet Noun Similarity = " + sim)
    //println(POS.getPOSForLabel("dog"))
    //println(wn.synsets("dog"))
    //println(wn.synsets(predicatesWithoutURIs2.take(predicatesWithoutURIs2.count().toInt).apply(0))
    //  .map(line => line.getPOS).distinct.last.getLabel) //get POS noun
    try{
      println(getPOStag(predicatesWithoutURIs2.take(predicatesWithoutURIs2.count().toInt).apply(4)))
    }catch {case e: Exception => println("Not found, exception caught: " + e)}



    //verb or noun
    //println(wn.synsets("hit").map(line => line.getPOS).distinct.last.getLabel)
    //we should separate verb and noun predicates
    //verb predicates in first KG

    //if(wn.synsets("hit").map(line => line.getPOS).distinct.last.getLabel.equals("noun")){

    //}
    //val p:RDD[String]=null

    /*
    var i:Int = 0
    //val rdd = ( i <- 0 to 5).map(x => getPOStag(predicatesWithoutURIs2.take(predicatesWithoutURIs2.count().toInt).apply(i)).reduce(_ union _)
    /*if you want to pass the loop index to the helper function, you can do something like this : val rdd = (1 to n).zipWithIndex.map{ case (x, index) => helperFunction(i) }.reduce(_ union _) Of course, in this case it is not necessary as we have an integer incrementing collection but you can replace (1 to n) by any collection */
    var j:Int = 0
    val p:RDD
    for( i <- 0 to 5){
      try
        {
          p  = sparkSession.sparkContext.parallelize(getPOStag(predicatesWithoutURIs2.take(predicatesWithoutURIs2.count().toInt).apply(i)))
        }catch {case e: Exception => println("Not found, exception caught: " + e)}


    }
    */


    // for loop execution with i range
    /*for( i <- 1 to predicatesWithoutURIs1.count(); b <- 1 to predicatesWithoutURIs2.count()){
      val sim = s.getMeanWordNetNounSimilarity("dog","cat")
    }*/

    sparkSession.stop

  }
  def getPOStag(word: String): String = {
    val wn = WordNet()
    val s = wn.synsets(word).map(line => line.getPOS).distinct.last.getLabel
    s
  }


}