name := "project"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.4.3",
  "org.apache.spark" %% "spark-sql" % "2.4.3",
  "org.scalactic" %% "scalactic" % "3.0.8" % Test,
  "org.apache.kafka" %% "kafka" % "2.4.1",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
)

libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-log4j12")) }