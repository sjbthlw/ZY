
TITLE = auto test

SET COUNT = 0

:WHILE

SET /A COUNT+=1


adb wait-for-device
adb shell cat /sys/class/thermal/thermal_zone1/temp
adb shell sleep 3


GOTO WHILE