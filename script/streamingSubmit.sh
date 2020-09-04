#!/bin/bash
export HDP_VERSION=2.6.5.0-292

project_home=/opt/software/spark-project
spark_home=/software/spark-2.4.4-bin-hadoop2.7
for i in $project_home/project-lib/*.jar;
do
  app_jars=$i,${app_jars}
done

len=${#app_jars}-1
app_classpath=${app_jars:0:len}

nohup ${spark_home}/bin/spark-submit \
     --class com.wjc.main \
     --name JT809 \
     --master yarn \
     --deploy-mode cluster \
     --driver-memory 1g \
     --num-executors 3 \
     --executor-cores 2 \
     --executor-memory 1g \
     --files /software/spark-2.4.4-bin-hadoop2.7/conf/mapred-site.xml \
     --conf spark.task.maxFailures=1 \
     --conf spark.streaming.backpressure.enabled=true \
     --conf spark.streaming.stopGracefullyOnShutdown=true \
     --conf spark.streaming.backpressure.initialRate=1000 \
     --conf spark.streaming.kafka.maxRatePerPartition=1000 \
     --conf spark.batch.time=300 \
     --jars ${app_classpath} \
     ${project_home}/project-package/process-1.0-SNAPSHOT.jar "com.wjc.job.realtime.StreamingExample" "run" >${project_home}/project-log/StreamingExample_$(date +\%Y\%m\%d).log 2>&1 &