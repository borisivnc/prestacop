import org.apache.spark
import org.apache.spark.sql.SparkSession

import scala.io.{BufferedSource, Source}
import org.apache.spark.{SparkConf, SparkContext}

object ViolationsAnalyse {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("load_csv")

    val sc = new SparkContext(conf) // Create Spark Context
    // Load local file data
    val spark : SparkSession = SparkSession.builder
      .appName("load_csv")
      .master("localhost[*]")
      .getOrCreate()


    val df = spark.read
      .format("jdbc")
      .option("characterEncoding", "UTF-8")
      .option("url", "jdbc:mysql://localhost:3306/FunDataProg?serverTimezone=Europe/Berlin")
      .option("driver", "com.mysql.cj.jdbc.Driver")
      .option("dbtable","FunDataProg.recordViolations")
      .option("user", "admin")
      .option("password", "password")
      .load()

    //val df_Alert = df.select("Plate ID", "Violation description", "House Number","Street Name").toDF()

    //Just rename the columns' name
    //val new_names = Seq("plate_id","description","house_number","street_name")
    //val df_columns = df_Alert.toDF(new_names:_*)
    val df_columns = df.toDF()
    df_columns.createOrReplaceTempView("records")

    val streetsViolations = spark.sql(
      """
        |SELECT streetName, count(*) AS iterations
        |FROM records
        |GROUP BY streetName
        |ORDER BY iterations DESC
        |LIMIT 10
      """.stripMargin)

    val descriptViolations = spark.sql(
      """
        |SELECT violationDescription, count(*) AS iterations
        |FROM records
        |GROUP BY violationDescription
        |ORDER BY iterations DESC
        |LIMIT 10
      """.stripMargin)

    val plateIDViolations = spark.sql(
      """
        |SELECT plateId, count(*) AS iterations
        |FROM records
        |GROUP BY plateId
        |ORDER BY iterations DESC
        |LIMIT 10
      """.stripMargin)

    streetsViolations.show()
    descriptViolations.show()
    plateIDViolations.show()
  }

}