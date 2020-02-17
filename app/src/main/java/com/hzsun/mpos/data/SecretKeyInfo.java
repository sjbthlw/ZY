package com.hzsun.mpos.data;

import struct.StructClass;
import struct.StructField;

@StructClass
public class SecretKeyInfo {

    @StructField(order = 0)
    public byte[] cKey = new byte[512];              //客户设密卡信息
}
