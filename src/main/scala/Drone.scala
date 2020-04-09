import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.spark.sql.DataFrame

class Drone(val xc: DataFrame) {
  var violations: DataFrame = xc
  var topicName: String = "violations"

  def spotViolation(producer: KafkaProducer[String,String]): Unit = {

    val randomRow = violations.sample(false,0.1).limit(1).toDF()

    randomRow.show()

    val plate_id = randomRow.first().getString(0)
    val violation_description = randomRow.first().getString(1)
    val house_number = randomRow.first().getString(2)
    val street_name = randomRow.first().getString(3)

    if (plate_id == null || violation_description == null) {

      if(plate_id == null) {
        producer.send(new ProducerRecord[String, String](topicName,"alert", "Cannot read plate ID"), (recordMetadata: RecordMetadata, exception: Exception) => {
          if (exception != null) {
            exception.printStackTrace()
          } else {
            println(s"Metadata about the sent record: ${recordMetadata.toString}")
          }
        })
      }

      else {
        producer.send(new ProducerRecord[String, String](topicName,"alert", "Cannot determinate violation"), (recordMetadata: RecordMetadata, exception: Exception) => {
          if (exception != null) {
            exception.printStackTrace()
          } else {
            println(s"Metadata about the sent record: ${recordMetadata.toString}")
          }
        })
      }
    }

    else {

      var records = Array(
        new ProducerRecord[String, String](topicName,"plateId", plate_id),
        new ProducerRecord[String, String](topicName,"violationDescription", violation_description),
        new ProducerRecord[String, String](topicName,"houseNumber", house_number),
        new ProducerRecord[String, String](topicName,"streetName", street_name)
      )

      for(i <- records.indices) {
        producer.send(records(i), (recordMetadata: RecordMetadata, exception: Exception) => {
          if (exception != null) {
            exception.printStackTrace()
          } else {
            println(s"Metadata about the sent record: ${recordMetadata.toString}")
          }
        })
      }

    }

  }
}
