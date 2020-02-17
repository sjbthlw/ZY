# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

#定义变量ProjectRoot为工程根目录，用相对路径没通过编译，可能是路径写错，以后再试
#本次使用绝对路径作为参数 D:\Android\android_pro\ZYTK35_MPOS_APP\app\
#set(ProjectRoot D:/Android/android_pro/ZYTK35_MPOS_APP/)


add_library( # Sets the name of the library.
             nativelib
             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/nativelib.c
             src/main/cpp/SECLIB.c
             src/main/cpp/Deslib.c
             src/main/cpp/bsp/pn512.c
             src/main/cpp/bsp/rtc.c
             src/main/cpp/bsp/uart.c
             src/main/cpp/bsp/uartqr.c
             src/main/cpp/bsp/uartprinter.c
              src/main/cpp/bsp/uartdockpos.c
             src/main/cpp/bsp/public.c
             src/main/cpp/bsp/sam.c
             src/main/cpp/card/CardInfo.c
             src/main/cpp/card/MifCard.c
             src/main/cpp/card/CPUCard.c
             src/main/cpp/card/SingleWalletCard.c
             src/main/cpp/card/SAM_App.c
             src/main/cpp/secret/SecLIB_POS.c
             src/main/cpp/secret/secret.c
             #src/main/cpp/secret/SecLIB_POS.h
             #src/main/cpp/secret/SecLIB_Define.h
	     )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

#动态方式加载 libFaceCheck.so是libxxxx.so的xxxx部分
add_library(FaceCheck SHARED IMPORTED)
add_library(msc SHARED IMPORTED)

#设置要连接的so的相对路径，${ANDROID_ABI}表示so文件的ABI类型的路径,这一步引入了动态加入编译的so
set_target_properties(FaceCheck  PROPERTIES IMPORTED_LOCATION ${PROJECT_SOURCE_DIR}/app/libs/${ANDROID_ABI}/libFaceCheck.so)
#语音包
set_target_properties(msc  PROPERTIES IMPORTED_LOCATION ${PROJECT_SOURCE_DIR}/app/libs/${ANDROID_ABI}/libmsc.so)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       nativelib
                       #the third library
#                       FaceCheck
#                       msc

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

#add_library(FaceCheck SHARED IMPORTED)
#set_target_properties(FaceCheck
#PROPERTIES IMPORTED_LOCATION
#${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libFaceCheck.so)
#/src/main/jniLibs/${ANDROID_ABI}/libFaceCheck.so)