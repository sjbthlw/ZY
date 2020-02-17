#! /bin/sh

DATE=`date +"%Y-%m-%d"` 
echo "========START LOG DATE is $DATE========="

RUN_PID=`ps | grep -n ' com.hzsun.mpos' | busybox awk '{print $2}'`
echo "========RUN APP is $RUN_PID========="
logcat | grep -a $RUN_PID 
