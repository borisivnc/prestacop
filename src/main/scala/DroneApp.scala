import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import java.util.Properties
import java.util.logging.{Level, Logger}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

object DroneApp extends App {

  System.setProperty("hadoop.home.dir", "C:\\winutils\\")

  val conf = new SparkConf()
    .setMaster("local")
    .setAppName("DroneApp")

  val sc = new SparkContext(conf)

  val spark : SparkSession = SparkSession.builder
    .appName("DroneApp")
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

  val sourceDf = spark.read.format("csv").option("header", "true").load("C:\\Users\\boris\\IdeaProjects\\fdp_project-master\\IdeaProjects\\project\\parking_violations.csv")
  val violationsDataFrame = sourceDf.select("Plate ID", "Violation description", "House Number","Street Name").toDF()

  val drones = Array(
    new Drone(violationsDataFrame),
    new Drone(violationsDataFrame),
    new Drone(violationsDataFrame),
    new Drone(violationsDataFrame),
    new Drone(violationsDataFrame)
  )

  for(i <- drones.indices) drones(i).spotViolation(producer)

}
