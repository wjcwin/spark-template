#!/bin/bash
export HDP_VERSION=2.6.5.0-292
export JAVA_HOME=/usr/local/jdk1.8.0_162
export PATH=$PATH:$JAVA_HOME/bin

project_home=/opt/software/spark-project
spark_home=/software/spark-2.4.4-bin-hadoop2.7
month=$ date -d "$(date +%Y%m)01 last month" +%Y%m
for i in $project_home/project-lib/*.jar;
do
  app_jars=$i,${app_jars}
done

len=${#app_jars}-1
app_classpath=${app_jars:0:len}

 ${spark_home}/bin/spark-submit \
     --class com.hikcreate.main \
     --name SparkExample \
     --master yarn \
     --deploy-mode cluster \
     --driver-memory 1g \
     --num-executors 2 \
     --executor-cores 1 \
     --executor-memory 512m \
     --files /software/spark-2.4.4-bin-hadoop2.7/conf/mapred-site.xml \
     --conf spark.task.maxFailures=1 \
     --conf spark.sql.crossJoin.enabled=true \
     --jars ${app_classpath} \
     ${project_home}/project-package/process-1.0-SNAPSHOT.jar "com.wjc.job.offline.SparkExample" "run"
# >${project_home}/project-log/SparkExample-$(date +\%Y\%m\%d).log 2>&1 &