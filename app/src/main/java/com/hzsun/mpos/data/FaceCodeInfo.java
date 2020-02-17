package com.hzsun.mpos.data;

import struct.StructClass;
import struct.StructField;

@StructClass
public class FaceCodeInfo {

    @StructField(order = 0)
    public long lngLocalVer;               //本机版本号
    @StructField(order = 1)
    public long lngPlatVer;               //平台版本号
}
