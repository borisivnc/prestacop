import java.util.Properties
import java.util.logging.{Level, Logger}

import org.apache.spark
import org.apache.spark.sql.SparkSession

import scala.io.{BufferedSource, Source}
import org.apache.spark.{SparkConf, SparkContext}
import java.util.Properties
import java.util.logging.{Level, Logger}

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

object Drone extends App {

    System.setProperty("hadoop.home.dir", "C:\\winutils\\")

    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("loadcsv")

    val sc = new SparkContext(conf) // Create Spark Context
    // Load local file data
    val spark : SparkSession = SparkSession.builder
      .appName("loadcsv")
      .master("local[2]")
      .getOrCreate()

  val props: Properties = new Properties()

  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
  props.put(ProducerConfig.LINGER_MS_CONFIG, "1")

  Logger.getLogger("org").setLevel(Level.WARNING)
  Logger.getLogger("akka").setLevel(Level.WARNING)
  Logger.getLogger("kafka").setLevel(Level.WARNING)

  val producer: KafkaProducer[String, String] = new KafkaProducer[String,String](props)

  val topicName = "scala"


    val sourceDf = spark.read.format("csv").option("header", "true").load("C:\\Users\\boris\\IdeaProjects\\fdp_project-master\\IdeaProjects\\project\\parking_violations.csv")
    val df1 = sourceDf.select("Plate ID", "Violation description", "House Number","Street Name").toDF()

  val randomRow = df1.sample(false,0.1).limit(1).toDF()

  randomRow.show(1)

    val plate_id = randomRow.first().getString(0)
    val violation_description = randomRow.first().getString(1)
    val house_number = randomRow.first().getString(2)
    val street_name = randomRow.first().getString(3)

    if (plate_id.equals(null) || violation_description.equals(null)) {
      print("alert")
    }
    else{
      print("Plate Id : "+ plate_id + " / Violation description : "+ violation_description + " / House number : "+ house_number + " / Street name : "+ street_name )

      val record = new ProducerRecord[String, String](topicName,"violation","Plate Id : "+ plate_id + " / Violation description : "+ violation_description + " / House number : "+ house_number + " / Street name : "+ street_name )
      producer.send(record, (recordMetadata: RecordMetadata, exception: Exception) => {
        if (exception != null) {
          exception.printStackTrace()
        } else {
          println(s"Metadata about the sent record: ${recordMetadata.toString}")
        }
      })



    }

}
