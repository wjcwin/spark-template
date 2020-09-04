#!/bin/bash
export HDP_VERSION=2.6.5.0-292
export JAVA_HOME=/usr/local/jdk1.8.0_162
export PATH=$PATH:$JAVA_HOME/bin

yesterday=`date -d -1day +%Y-%m-%d`

project_home=/opt/software/spark-project
spark_home=/software/spark-2.4.4-bin-hadoop2.7

for i in `cat /opt/software/spark-project/project-script/ods/MergeSmallFile.conf`
do
  dirs=/hive/ODS.db/${i}/partitionday=${yesterday},${dirs}
done
dir_len=${#dirs}-1
dir_all=${dirs:0:dir_len}

for i in $project_home/project-lib/*.jar;
do
  app_jars=$i,${app_jars}
done
len=${#app_jars}-1
app_classpath=${app_jars:0:len}

nohup ${spark_home}/bin/spark-submit \
 --class com.wjc.Main \
 --name MergeSmallFile \
 --master yarn \
 --deploy-mode cluster \
 --driver-memory 512m \
 --num-executors 3 \
 --executor-cores 1 \
 --executor-memory 512m \
 --files /software/spark-2.4.4-bin-hadoop2.7/conf/mapred-site.xml \
 --conf spark.task.maxFailures=1 \
 --jars ${app_classpath} \
${project_home}/project-package/process-1.0-SNAPSHOT.jar "com.wjc.job.offline.MergeSmallFile" "run" ${dir_all}>${project_home}/project-log/MergeSmallFile_$(date +\%Y\%m\%d).log 2>&1 &