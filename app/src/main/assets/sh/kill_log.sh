#! /bin/sh

KILL_PID=`ps | grep -n ' logcat' | busybox awk '{print $2}'`
echo "========kill shell is $KILL_PID========="
kill -9 $KILL_PID




