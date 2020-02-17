package com.hzsun.mpos.Http;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import sun.misc.BASE64Encoder;

import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Http.FaceCodeTask.GETCHANGEDATA_CODE;
import static com.hzsun.mpos.Http.FaceCodeTask.GETCHANGEPIC_CODE;
import static com.hzsun.mpos.Http.FaceCodeTask.GETINITDATA_CODE;
import static com.hzsun.mpos.Http.FaceCodeTask.GETINITPIC_CODE;
import static com.hzsun.mpos.Http.FaceCodeTask.GETPHOTO_CODE;
import static com.hzsun.mpos.Http.FaceCodeTask.GETVERSION_CODE;
import static com.hzsun.mpos.Http.FaceCodeTask.UPDATEAPP_CODE;
import static com.hzsun.mpos.Http.FaceCodeTask.UPLOADRECORD_CODE;

public class DataAnalyze {

    //public static final String pathURL = "http://192.168.1.97:28080/facefeature-web/gateway.do";
    public static final String pathURL = "http://192.168.1.43:29090/facefeature-web/gateway.do";


    public static String RequestMsg( String pathURL, String urlContext,Map<String, String> postMap, Map<String, String> fileMap) {
        HttpUtility Request = new HttpUtility( pathURL,fileMap);
        return Request.request(urlContext,postMap, fileMap);
    }

    public static String PostRequest(Map<String,String> map, Map<String, String> fileMap, int code){
        String urlContext;
        JSONObject jsonObjectData = new JSONObject();
        try {
            Set<Map.Entry<String, String>> mapSet = map.entrySet();
            for (Map.Entry<String, String> me : mapSet) {
                jsonObjectData.put(me.getKey(), me.getValue());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Map<String, String> postMap = getPostData(jsonObjectData.toString(), code);
        String mapStr = postMap.toString().replace(", ", "&");
        urlContext = mapStr.substring(1, mapStr.length() - 1);
        if((g_SystemInfo.FaceHTTPServerAdr[0]==0x00)
            ||(g_SystemInfo.iFacehttpLength ==0))
        {
            Log.e("PostRequest","http地址无效");
            return null;
        }
        String strpathURL= new String (g_SystemInfo.FaceHTTPServerAdr,0,g_SystemInfo.iFacehttpLength);
        return RequestMsg(strpathURL, urlContext, postMap,fileMap);
    }

    public static Map<String, String> getPostData(String jsonString,int code) {
        Map<String, String> map = new TreeMap<>();
        if (code==GETVERSION_CODE){
            map.put("method", "hzsun.face.version");
        }else if (code==GETINITDATA_CODE){
            map.put("method", "hzsun.face.initdata");
        }else if (code==GETINITPIC_CODE){
            map.put("method", "hzsun.facepic.initdata");
        } else if (code==GETCHANGEDATA_CODE){
            map.put("method", "hzsun.face.changedata");
        }else if (code==GETCHANGEPIC_CODE){
            map.put("method", "hzsun.facepic.changedata");
        }else if (code==GETPHOTO_CODE){
            map.put("method", "hzsun.face.photo");
        }else if (code==UPDATEAPP_CODE){
            map.put("method", "hzsun.device.userinfo");
        } else if (code == UPLOADRECORD_CODE) {
            map.put("method", "hzsun.face.image");
        }
        map.put("format", "json");
        map.put("charset", "utf-8");
        map.put("publickey", "123456");
        map.put("sign_type", "RSA2");
        map.put("timestamp", toDate(System.currentTimeMillis()));
        map.put("version", "1.0");
        map.put("biz_content", jsonString);
        String strMap = map.toString().replace(", ", "&");
        map.put("sign", encrypt(strMap.substring(1, strMap.length() - 1),code));
        return map;
    }

    public static String Post(String url, Map<String, String> form) {
        HttpURLConnection conn = null;
        PrintWriter pw = null;
        BufferedReader rd = null;
        StringBuilder out = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        String line = null;
        String response = null;
        for (String key : form.keySet()) {
            if (out.length() != 0) {
                out.append("&");
            }
            out.append(key).append("=").append(form.get(key));
        }
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(20000);
            conn.setConnectTimeout(20000);
            conn.setUseCaches(false);
            conn.connect();
            pw = new PrintWriter(conn.getOutputStream());
            pw.print(out.toString());
            pw.flush();
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            response = sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
                if (rd != null) {
                    rd.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }


    public static String toDate(long milliscond) {
        Date date = new Date(milliscond);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String data = simpleDateFormat.format(gc.getTime());
        return data;
    }

    public static String encrypt(String password) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
            //Log.e("去》》》》》》生成签名",password);
            md.update(password.getBytes("UTF-8")); // step 3
            byte raw[] = md.digest(); // step 4
            String hash = (new BASE64Encoder()).encode(raw); // step 5
            return URLEncoder.encode(hash, "utf-8"); // step 6
        } catch (NoSuchAlgorithmException e) {
        } catch (java.io.UnsupportedEncodingException e) {
        }
        return null;
    }

    public static String encrypt(String password,int code) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
            //Log.e("去》》》》》》生成签名",password);
            md.update(password.getBytes("UTF-8")); // step 3
            byte raw[] = md.digest(); // step 4
            String hash = (new BASE64Encoder()).encode(raw); // step 5
            //return hash; // step 6
            if(code==UPLOADRECORD_CODE)
                return hash; // step 6
            else
                return URLEncoder.encode(hash, "utf-8"); // step 6
        } catch (NoSuchAlgorithmException e) {
        } catch (java.io.UnsupportedEncodingException e) {
        }
        return null;
    }

}
