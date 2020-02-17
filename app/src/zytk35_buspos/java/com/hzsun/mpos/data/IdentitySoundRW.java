package com.hzsun.mpos.data;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hzsun.mpos.Public.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_Identity_Sound_File;


public class IdentitySoundRW {

    private static final String TAG = IdentitySoundRW.class.getSimpleName();

    //创建读取身份语音信息文件
    public static int CreateIdentitySoundFile() {
        //创建文件
        if (FileUtils.CreateDataFile(en_Identity_Sound_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取读取身份语音信息
    public static LinkedHashMap<String, String> ReadIdentitySoundFile() {
        LinkedHashMap<String, String> map=new LinkedHashMap<>();
        String sFileName = FileUtils.GetFileName(en_Identity_Sound_File);
        if (sFileName == "")
            return map;
        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()){
            try {
                StringBuilder builder = new StringBuilder();
                InputStreamReader reader = new InputStreamReader(new FileInputStream(fFile));
                BufferedReader in = new BufferedReader(reader);
                String text;
                try {
                    while ((text = in.readLine()) != null) {
                        builder.append(text);
                    }
                    reader.close();
                    in.close();
                    String result = builder.toString();
                    if (TextUtils.isEmpty(result)) {
                        return map;
                    } else {
                        try {
                            Gson gson = new Gson();
                            map = gson.fromJson(result, LinkedHashMap.class);
                            Log.i(TAG, map.toString());
                            return map;
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                            Log.i(TAG, "文件解析失败");
                            return map;
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Log.e(TAG, "无读取身份语音信息文件");
        }
        return map;
    }

    //写读取身份语音信息
    public static int WriteIdentitySoundFile(String json) {
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_Identity_Sound_File);
        if (sFileName == "") {
            return -1;
        }
        File fFile = new File(sFileName);
        if (!fFile.exists()) {
            if (CreateIdentitySoundFile() != 0) {
                return -1;
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(fFile);
            out.write(json.getBytes());
            out.close();
            return 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }


}
