import java.time.Duration

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.Properties

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConverters._
import org.apache.spark.sql.{DataFrame, SparkSession}

object MainProgram extends App {

  val conf = new SparkConf()
    .setMaster("local")
    .setAppName("localConsumeNotif")

  val sc = new SparkContext(conf) // Create Spark Context

    lazy val spark: SparkSession = SparkSession.builder().master("local").appName("ConsumeNotification").getOrCreate()
    import spark.implicits._
    val connectionProperties = new Properties()
    connectionProperties.put("user", "admin")
    connectionProperties.put("password", "password")


    val props: Properties = new Properties()

    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "mainProgram")

    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
    consumer.subscribe(List("violations").asJava)
    var plateId = ""
    var violationDescription = ""
    var houseNumber = ""
    var streetName = ""
    while (true) {
      val records: ConsumerRecords[String, String] = consumer.poll(Duration.ofMillis(100))
      records.asScala.foreach { record =>
        //println(s"offset = ${record.offset()}, key = ${record.key()}, value = ${record.value()}")
        if (record.key() == "plateId") {
          plateId = record.value()
          violationDescription = ""
          houseNumber = ""
          streetName = ""
        }
        else if(record.key() == "alert") {
          plateId = ""
          violationDescription = ""
          houseNumber = ""
          streetName = ""
          println("We need human check !")
          println(s"key = ${record.key()}, value = ${record.value()}")
        }
        else if (record.key() == "violationDescription") {
          violationDescription = record.value()
        }
        else if (record.key() == "houseNumber") {
          houseNumber = record.value()
        }
        else if (record.key() == "streetName") {
          streetName = record.value()
        }
        if (plateId != "" && violationDescription != "" && houseNumber != "" && streetName != "") {
         // println("Plate id :"+plateId+" violation description :"+violationDescription + " house number :"+houseNumber+" street name :"+streetName)
          var row =  Seq((plateId, violationDescription, houseNumber, streetName)).toDF("plateID","violationDescription","houseNumber","streetName")
          row.write
            .option("characterEncoding", "UTF-8")
            .option("driver", "com.mysql.cj.jdbc.Driver")
            .mode("append")                   //Add DF into next line if tablee already exists
            .jdbc("jdbc:mysql://localhost:3306/FunDataProg?serverTimezone=Europe/Berlin","FunDataProg.recordViolations",connectionProperties)

        }
      }
    }

}
