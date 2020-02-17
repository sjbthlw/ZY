package com.hzsun.mpos.Http;

import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.hzsun.mpos.Http.FaceCodeTask.HEVT_CONTIMEOUT;
import static com.hzsun.mpos.Http.FaceCodeTask.HEVT_READTIMEOUT;
import static com.hzsun.mpos.Http.FaceCodeTask.HEVT_WRITETIMEOUT;
import static com.hzsun.mpos.Http.FaceCodeTask.gHttpRecvHandler;


public class HttpUtility {


    private HttpURLConnection urlConnection;
    private String addr;
    //分隔符
    private String finalSplit = "---------------------------123821742118716";

    HttpUtility(String address,Map<String, String> fileMap) {
        super();
        // TODO Auto-generated constructor stub
        openConnection(address,fileMap);
        addr = address;
    }

    String request(String msg, Map<String, String> postMap, Map<String, String> fileMap) {
        if (urlConnection == null) {
            /**无法连接服务器，请重新检查网络**/
            return null;
        }
        write(msg, postMap, fileMap);  //上传数据到服务器
        String json = read();  //从服务器读取数据
        if (json.equals("")) {
            /**无法连接服务器，请重新检查网络**/
            return null;
        }
        //LogUtil.showLargeLog(json,4000,"请求返回数据");
        return json;
    }

    private void openConnection(String address,Map<String, String> fileMap) {
        try {
            URL url = new URL(address);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);

            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            if (fileMap != null) {
                urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + finalSplit);
            }
            urlConnection.connect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            /**无法连接服务器，请重新检查网络**/
            gHttpRecvHandler.sendEmptyMessage(HEVT_CONTIMEOUT);
            e.printStackTrace();
        }
    }

    private String read() {
        if (urlConnection == null) {
            return "";
        } else {
            try {
                InputStream stream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(stream, "utf-8");
                BufferedReader br = new BufferedReader(reader);
                StringBuilder builder = new StringBuilder();
                String result;
                while ((result = br.readLine()) != null) {
                    builder.append(result.trim());
                }
                return builder.toString();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                gHttpRecvHandler.sendEmptyMessage(HEVT_READTIMEOUT);
                e.printStackTrace();
                return "";
            }
        }
    }

    private void write(String msg,Map<String, String> postMap, Map<String, String> fileMap) {
        if (urlConnection != null) {
            try {
                OutputStream stream = urlConnection.getOutputStream();
                if (fileMap!=null){
                    StringBuffer  entry = new StringBuffer();
                    Set<Map.Entry<String,String>> textEntrySet = postMap.entrySet();
                    for (Map.Entry<String,String> textEntry : textEntrySet){
                        String inputName = (String) textEntry.getKey();
                        String inputValue = (String) textEntry.getValue();
                        inputValue = new String(inputValue.getBytes("GBK"));
                        entry.append("\r\n").append("--").append(finalSplit).append("\r\n");
                        entry.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
                        entry.append(inputValue);
                    }
                    stream.write(entry.toString().getBytes("utf-8"));
                    byte[] fileBytes = getFileData(fileMap, stream);
                    if (fileBytes != null) {
                        stream.write(fileBytes);
                    }
                } else {
                    stream.write(msg.getBytes());
                }
                stream.flush();
                stream.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                gHttpRecvHandler.sendEmptyMessage(HEVT_WRITETIMEOUT);
                e.printStackTrace();
            }
        }
    }


    private byte[] getFileData(Map<String, String> fileMap, OutputStream stream) {
        if (urlConnection != null) {
            try {
                Iterator<Map.Entry<String, String>> iter = fileMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    File file = new File(inputValue);
                    String filename = file.getName();
                    String contentType = getMimeType(file);
                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append("\r\n").append("--").append(finalSplit).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + filename + "\"\r\n");
                    strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
                    stream.write(strBuf.toString().getBytes());
                    DataInputStream in = new DataInputStream(new FileInputStream(file));
                    int bytes = 0;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = in.read(bufferOut)) != -1) {
                        stream.write(bufferOut, 0, bytes);
                    }
                    in.close();
                }
                byte[] endData = ("\r\n--" + finalSplit + "--\r\n").getBytes();
                return endData;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private String getMimeType(File file) {
        String suffix = getSuffix(file);
        if (suffix == null) {
            return "file/*";
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if (type != null || !type.isEmpty()) {
            return type;
        }
        return "file/*";
    }

    private String getSuffix(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }
        String fileName = file.getName();
        if (fileName.equals("") || fileName.endsWith(".")) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return null;
        }
    }

}
