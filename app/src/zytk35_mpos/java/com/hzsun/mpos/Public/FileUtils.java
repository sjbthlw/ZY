package com.hzsun.mpos.Public;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hzsun.mpos.Global.Global.DATAPath;
import static com.hzsun.mpos.Global.Global.FACEFEASIZE;
import static com.hzsun.mpos.Global.Global.LogPath;
import static com.hzsun.mpos.Global.Global.PicPath;
import static com.hzsun.mpos.Global.Global.ZYTKFacePath;
import static com.hzsun.mpos.Public.NumConvertUtils.Bytes2Hexstr;
import static com.hzsun.mpos.Public.Utility.memcpy;


public class FileUtils {

    //定义枚举类型
    public enum EN_FileType {
        //基础参数
        en_BasicInfo_File,
        //版本文件
        en_Version_File,
        //本机参数
        en_LocalInfo_File,
        //本机网络参数
        en_LocalNetInfo_File,
        //系统参数
        en_System_File,
        //终端机站点参数
        en_Station_File,
        //钱包参数
        en_Purse_File,
        //单键参数
        en_SingleBond_File,
        //营业分组
        en_TimeGroup_File,
        //身份钱包
        en_IdentityWallet_File,
        //身份优惠
        en_IdentityDiscount_File,
        //工作密钥
        en_WorkKey_File,
        //初始黑白名单
        en_InitBlack_File,
        //变更黑白名单
        en_ChgBlack_File,
        //记录系统终端参数
        en_Record_File,
        //黑白名单
        en_CardBWList_File,
        //交易流水
        en_WasteBooks_File,
        //交易流水备份
        en_WasteBooksBak_File,
        //二维码交易流水
        en_QrCodeWasteBooks_File,
        //人脸交易流水
        en_FaceWasteBooks_File,
        //FTPServer信息
        en_FTPServer_File,
        //终端机升级程序
        en_TermProgram_File,
        //人脸特征码参数
        en_FaceCodeInfo_File
    }

    /**
     * 判断文件夹是否存在如果没有存在就创建文件夹
     **/
    private static final String TAG = "FileUtils";

    //获取数据文件名
    public static String GetFileName(EN_FileType FileType) {
        String FilePathName = "";

        //根据文件类型，取文件名字
        switch (FileType) {
            //基础数据文件
            case en_BasicInfo_File:
                FilePathName = DATAPath + "BasicInfo.dat";
                break;
            //工作密钥
            case en_WorkKey_File:
                FilePathName = DATAPath + "WorkKey.dat";
                break;
            //本地数据文件
            case en_LocalInfo_File:
                FilePathName = DATAPath + "LocalInfo.dat";
                break;
            //本地网络数据文件
            case en_LocalNetInfo_File:
                FilePathName = DATAPath + "LocalNetInfo.dat";
                break;
            //版本号文件
            case en_Version_File:
                FilePathName = DATAPath + "VersionInfo.dat";
                break;
            //系统参数
            case en_System_File:
                FilePathName = DATAPath + "SytemInfo.dat";
                break;
            //终端机参数
            case en_Station_File:
                FilePathName = DATAPath + "StationInfo.dat";
                break;
            //钱包参数
            case en_Purse_File:
                FilePathName = DATAPath + "PurseInfo.dat";
                break;
            //单键参数
            case en_SingleBond_File:
                FilePathName = DATAPath + "SingleBond.dat";
                break;
            //营业分组
            case en_TimeGroup_File:
                FilePathName = DATAPath + "TimeGroup.dat";
                break;
            //身份钱包
            case en_IdentityWallet_File:
                FilePathName = DATAPath + "IdentityWallet.dat";
                break;
            //身份优惠
            case en_IdentityDiscount_File:
                FilePathName = DATAPath + "IdentityDiscount.dat";
                break;
            //记录系统终端参数
            case en_Record_File:
                FilePathName = DATAPath + "RecordInfo.dat";
                break;
            //流水
            case en_WasteBooks_File:
                FilePathName = DATAPath + "WasteBooks.dat";
                break;
            //黑白名单
            case en_CardBWList_File:
                FilePathName = DATAPath + "CardBlackWhite.dat";
                break;
            //终端程序
            case en_TermProgram_File:
                FilePathName = DATAPath + "TermProgram.dat";
                break;
            //二维码记录
            case en_QrCodeWasteBooks_File:
                FilePathName = DATAPath + "QrCodeWasteBooks.dat";
                break;
            // 人脸流水记录
            case en_FaceWasteBooks_File:
                FilePathName = DATAPath + "FaceWasteBooks.dat";
                break;
            //FTPServer信息
            case en_FTPServer_File:
                FilePathName = DATAPath + "FTPServerInfo.dat";
                break;
            //流水备份
            case en_WasteBooksBak_File:
                FilePathName = DATAPath + "WasteBooksBak.dat";
                break;

            //人脸特征码参数
            case en_FaceCodeInfo_File:
                FilePathName = DATAPath + "FaceCodeInfo.dat";
                break;

            default:
                break;
        }
        return FilePathName;
    }

    //创建数据文件
    public static int CreateDataFile(EN_FileType FileType) {
        //获取文件名
        String sFileName = GetFileName(FileType);
        if (sFileName == "")
            return -1;

        //创建文件夹
        CreatDirectory(DATAPath);
        //创建数据文件
        CreateFile(sFileName);
        return 0;
    }

    public static void CreatDirectory(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
    }

    public static void CreatDirectory(File file) {
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
    }

    /**
     * 判断文件是否存在如果没有存在就创建文件
     **/
    public static void CreateFile(String filename) {
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, "create File \"+filename+\" is fail! \\r\\n" + e.getMessage());
            }
        }
    }

    public static void close(OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static File CheckAndCreateFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    //读取List数据
    public static ArrayList<String> ReadListFileData(String fileName) {
        ObjectInputStream objectInputStream = null;
        FileInputStream fileInputStream = null;
        ArrayList<String> savedArrayList = new ArrayList<>();
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                return null;
            }
            fileInputStream = new FileInputStream(file.toString());
            objectInputStream = new ObjectInputStream(fileInputStream);
            savedArrayList = (ArrayList<String>) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return savedArrayList;
    }

    //写List数据
    public static void WriteListToFile(ArrayList tArrayList, String fileName) {

        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        FileInputStream fileInputStream = null;
        try {
            File file = CheckAndCreateFile(fileName);

            fileOutputStream = new FileOutputStream(file.toString());  //新建一个内容为空的文件
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(tArrayList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (objectOutputStream != null) {
            try {
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //读取MAP数据
    public static Map<String, String> ReadMapFileData(String fileName) {
        ObjectInputStream objectInputStream = null;
        FileInputStream fileInputStream = null;
        Map<String, String> savedArrayList = new HashMap<>();
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                return null;
            }
            fileInputStream = new FileInputStream(file.toString());
            objectInputStream = new ObjectInputStream(fileInputStream);
            savedArrayList = (Map<String, String>) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return savedArrayList;
    }

    //写MAP数据
    public static void WriteMapToFile(Map tArrayList, String fileName) {

        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        FileInputStream fileInputStream = null;
        try {
            File file = CheckAndCreateFile(fileName);

            fileOutputStream = new FileOutputStream(file.toString());  //新建一个内容为空的文件
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(tArrayList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (objectOutputStream != null) {
            try {
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取指定文件大小
     *
     * @param
     * @return
     * @throws Exception
     */
    public static long getFileSize(String path) throws Exception {
        long size = 0;

        String strFilePath = path;

        File fFile = new File(path);
        if (fFile.exists() && fFile.isFile()) {
            FileInputStream fis = null;
            fis = new FileInputStream(fFile);
            size = fis.available();
        } else {
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }


    public static String GetFileInfo(String path) {
        String strFilePath = path;

        File fFile = new File(path);
        if (fFile.exists() && fFile.isFile()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(strFilePath);
                byte[] buffer = new byte[fileInputStream.available()];
                fileInputStream.read(buffer);
                return new String(buffer);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void SaveFileInfo(String strInfo, String path) {
        //生成文件夹之后，再生成文件，不然会出错
        long lngPos = 0;
        String strFilePath = path;
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(lngPos);
            raf.write(strInfo.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    /**
     * 创建文件
     *
     * @param filePath 文件地址
     * @param fileName 文件名
     * @return
     */
    public static boolean CreateFile(String filePath, String fileName) {
        String strFilePath = filePath + fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            /**  注意这里是 mkdirs()方法  可以创建多个文件夹 */
            file.mkdirs();
        }
        File subfile = new File(strFilePath);

        if (!subfile.exists()) {
            try {
                boolean b = subfile.createNewFile();
                return b;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * 遍历文件夹下的文件
     *
     * @param file 地址
     */
    public static List<File> getFile(File file) {
        List<File> list = new ArrayList<>();
        File[] fileArray = file.listFiles();
        if (fileArray == null) {
            return null;
        } else {
            for (File f : fileArray) {
                if (f.isFile()) {
                    list.add(0, f);
                } else {
                    getFile(f);
                }
            }
        }
        return list;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件地址
     * @return
     */
    public static boolean deleteFile(String filePath) {
        if (!TextUtils.isEmpty(filePath)){
            File file = new File(filePath);
            if (file.exists()){
                if (file.isFile()) {
                    file.delete();
                }
                return true;
            }else {
                return false;
            }
        }
        return false;
    }


    /**
     * 删除文件
     *
     * @param filePath 文件地址
     * @return
     */
    public static boolean deleteFiles(String filePath) {
        List<File> files = getFile(new File(filePath));
        if (files.size() != 0) {
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);

                /**  如果是文件则删除  如果都删除可不必判断  */
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
        return true;
    }


    /**
     * 向文件中添加内容
     *
     * @param strcontent 内容
     * @param filePath   地址
     * @param fileName   文件名
     */
    public static void writeToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        String strFilePath = filePath + fileName;
        RandomAccessFile raf = null;
        try {
            File subfile = new File(strFilePath);
            if (!subfile.exists()) {
                subfile.getParentFile().mkdirs();
                subfile.createNewFile();
            }
            /**   构造函数 第二个是读写方式    */
            raf = new RandomAccessFile(subfile, "rw");
            /**  将记录指针移动到该文件的最后  */
            raf.seek(subfile.length());
            /** 向文件末尾追加内容  */
            raf.write(strcontent.getBytes());

            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 修改文件内容（覆盖或者添加）
     *
     * @param path    文件地址
     * @param content 覆盖内容
     * @param append  指定了写入的方式，是覆盖写还是追加写(true=追加)(false=覆盖)
     */
    public static void modifyFile(String path, String content, boolean append) {
        try {
            FileWriter fileWriter = new FileWriter(path, append);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取文件内容
     *
     * @param filePath 地址
     * @param filename 名称
     * @return 返回内容
     */
    public static String getString(String filePath, String filename) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(filePath + filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 重命名文件
     *
     * @param oldPath 原来的文件地址
     * @param newPath 新的文件地址
     */
    public static void renameFile(String oldPath, String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);

        Log.e(TAG, oldPath);
        Log.e(TAG, newPath);

        //执行重命名
        oleFile.renameTo(newFile);
    }


    /**
     * 复制文件
     *
     * @param fromFile 要复制的文件目录
     * @param toFile   要粘贴的文件目录
     * @return 是否复制成功
     */
    public static boolean copy(String fromFile, String toFile) {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromFile);
        //如同判断SD卡是否存在或者文件是否存在
        //如果不存在则 return出去
        if (!root.exists()) {
            return false;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();

        //目标目录
        File targetDir = new File(toFile);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        //遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory())//如果当前项为子目录 进行递归
            {
                copy(currentFiles[i].getPath() + "/", toFile + currentFiles[i].getName() + "/");

            } else//如果当前项为文件则进行文件拷贝
            {
                CopySdcardFile(currentFiles[i].getPath(), toFile + currentFiles[i].getName());
            }
        }
        return true;
    }

    //文件拷贝
    //要复制的目录下的所有非子目录(文件夹)文件拷贝
    public static boolean CopySdcardFile(String fromFile, String toFile) {

        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    //移动人脸lib文件
    public static void MoveAssetsFile(Context context, String fileName, String fileNewPath) {
        //将目标文件移动至可目录中
        AssetManager assets = context.getAssets();
        try {
            String[] files = assets.list("");
            System.out.println(files);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            File file = new File(fileNewPath + "/" + fileName);
            if (!file.exists()) {
                file.createNewFile();
                InputStream is = assets.open(fileName);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024 * 1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //移动SHELL脚本文件
    public static void MoveAssetsSHFile(Context context, String fileNewPath) {
        //将目标文件移动至可目录中
        String assetPath = "sh";
        String fileName = "";
        AssetManager assets = context.getAssets();

        try {
            String[] fileList = assets.list(assetPath);
            if (fileList.length != 0) {
                for (int i = 0; i < fileList.length; i++) {
                    fileName = fileList[i];
                    //判断.so
                    if (fileName.indexOf(".sh") == -1) {
                        continue;
                    }
                    File file = new File(fileNewPath + "/" + fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    InputStream is = assets.open(assetPath + "/" + fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024 * 8];
                    int byteCount = 0;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    is.close();
                    fos.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //移动人脸lib文件
    public static void MoveAllFaceAssetsFile(Context context, String fileNewPath) {
        //将目标文件移动至可目录中
        String fileName = "";
        AssetManager assets = context.getAssets();

        try {
            String[] fileList = assets.list("");
            if (fileList.length != 0) {
                for (int i = 0; i < fileList.length; i++) {
                    fileName = fileList[i];
                    //判断.so
                    if (fileName.indexOf(".so") == -1) {
                        continue;
                    }
                    File file = new File(fileNewPath + "/" + fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    InputStream is = assets.open(fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024 * 1024];
                    int byteCount = 0;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    is.close();
                    fos.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //移动IAP文件
    public static void MoveIAPAssetsFile(Context context, String fileNewPath) {
        //将目标文件移动至可目录中
        String assetPath = "iap";
        String fileName = "";
        AssetManager assets = context.getAssets();

        try {
            String[] fileList = assets.list(assetPath);
            if (fileList.length != 0) {
                for (int i = 0; i < fileList.length; i++) {
                    fileName = fileList[i];

                    File file = new File(fileNewPath + "/" + fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    InputStream is = assets.open(assetPath + "/" + fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024 * 1024];
                    int byteCount = 0;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    is.close();
                    fos.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024*8];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    //保存照片文件到本地
    public static int SavePhotoData(InputStream inputStream, String strFilePath, String strFileName) throws Exception {
        File file = new File(strFilePath, strFileName);
        File fileParent = file.getParentFile();
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }

        int length = 0;
        byte[] data = readStream(inputStream);
        if (data != null) {
            length = data.length;
            Log.d(TAG, "用户照片文件大小:" + length);
            if((length < (512 * 1024))&&(length > (10 * 1024))){
                OutputStream out = new FileOutputStream(file);
                out.write(data, 0, length);
                return length;
            }else{
                Log.e(TAG,"文件大小超出512K或者不到10K");
                return 0;
            }
        }
        return 0;
    }

    //保存程序文件到本地
    public static void SaveAppFileData(InputStream inputStream, String strFilePath, String strFileName) throws IOException {
        File file = new File(strFilePath, strFileName);
        OutputStream out = new FileOutputStream(file);
        byte[] buffer = new byte[1024 * 1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
    }

    //保存特征码到本地
    public static void SaveFaceCodeFile(InputStream inputStream, String strFilePath, String strFileName) throws IOException {
        File file = new File(strFilePath, strFileName);
        OutputStream out = new FileOutputStream(file);
        byte[] buffer = new byte[1024 * 8];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
    }

    //保存特征码到本地
    public static void SaveFaceCodeFile(byte[] date, String strFilePath, String strFileName) throws IOException {
        File file = new File(strFilePath, strFileName);
        OutputStream out = new FileOutputStream(file);
        int length = date.length;
        out.write(date, 0, length);
    }

    //写人脸数据到文件中
    public static byte[] SaveFaceCodeData(byte[] mFaceFeature, int iFeatureID) {
        long lngPos = 0;
        int lngLength = 0;
        int byteCount = 0;

        //获取文件名
        String sFileName = ZYTKFacePath + "facedata.v";

        lngPos = ((iFeatureID) * FACEFEASIZE);
        File fFile = new File(sFileName);

        if (!fFile.exists() || !fFile.isFile()) {
            try {
                fFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d("", "create File \"+filename+\" is fail! \\r\\n" + e.getMessage());
            }
        }
        if (fFile.exists() && fFile.isFile()) {
            byte[] buffer = new byte[FACEFEASIZE];
            byte[] RecvBuffer = new byte[FACEFEASIZE];
            RandomAccessFile random = null;
            try {
                lngLength = 0;
                memcpy(buffer, 0, mFaceFeature, 0, mFaceFeature.length);
                lngLength = mFaceFeature.length;
                //lngLength = inputStream.read(buffer);
                if (lngLength != FACEFEASIZE) {
                    Log.e(TAG, "获取特征码数据长度err:" + lngLength);
                    return null;
                }
                //判断特征码头文件 第一字节0x91 第二字节2-16
                if (((buffer[0] & 0xff) != 0x91) || ((buffer[1] & 0xff) < 2 || (buffer[1] & 0xff) > 16)) {
                    Log.e(TAG, String.format("获取特征码数据头错误:%02x.%02x", buffer[0], buffer[1]));
                    return null;
                }
                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //文件读写出现异常！
                Log.e(TAG, e.getMessage());
            } finally {
                try {
                    random.close();
                    return buffer;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
        }
        return null;
    }


    //写人脸数据到文件中
    public static byte[] SaveFaceCodeData(InputStream inputStream, int iFeatureID) {
        long lngPos = 0;
        int lngLength = 0;
        int byteCount = 0;

        //获取文件名
        String sFileName = ZYTKFacePath + "facedata.v";

        lngPos = ((iFeatureID) * FACEFEASIZE);
        File fFile = new File(sFileName);

        if (!fFile.exists() || !fFile.isFile()) {
            try {
                fFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d("", "create File \"+filename+\" is fail! \\r\\n" + e.getMessage());
            }
        }
        if (fFile.exists() && fFile.isFile()) {
            byte[] buffer = new byte[FACEFEASIZE];
            byte[] RecvBuffer = new byte[FACEFEASIZE];
            RandomAccessFile random = null;
            try {
                lngLength = 0;
                while ((byteCount = inputStream.read(RecvBuffer)) != -1) {
                    memcpy(buffer, lngLength, RecvBuffer, 0, byteCount);
                    lngLength += byteCount;
                    if (lngLength == FACEFEASIZE)
                        break;
                }
                //lngLength = inputStream.read(buffer);
                if (lngLength != FACEFEASIZE) {
                    Log.e(TAG, "获取特征码数据长度err:" + lngLength);
                    return null;
                }
                //判断特征码头文件 第一字节0x91 第二字节2-16
                if(((buffer[0]&0xff)!=0x91)||((buffer[1]&0xff)<2 || (buffer[1]&0xff)>16)){
                    Log.e(TAG, String.format("获取特征码数据头错误:%02x.%02x",buffer[0],buffer[1]));
                    return null;
                }
                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //文件读写出现异常！
                Log.d("", e.getMessage());
            } finally {
                try {
                    random.close();
                    return buffer;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
        }
        return null;
    }

    //删除人脸数据到文件中
    public static byte[] DelFaceCodeData(int iFeatureID) {
        long lngPos = 0;

        //获取文件名
        String sFileName = ZYTKFacePath + "facedata.v";

        lngPos = ((iFeatureID) * FACEFEASIZE);
        File fFile = new File(sFileName);

        if (!fFile.exists() || !fFile.isFile()) {
            try {
                fFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d("", "create File \"+filename+\" is fail! \\r\\n" + e.getMessage());
            }
        }
        if (fFile.exists() && fFile.isFile()) {
            byte[] buffer = new byte[FACEFEASIZE];
            RandomAccessFile random = null;
            try {
                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //文件读写出现异常！
                Log.d("", e.getMessage());
            } finally {
                try {
                    random.close();
                    return buffer;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
        }
        return null;
    }


    /**
     * 保存bitmap到本地
     *
     * @param bitmap
     * @return
     */
    public static void SaveBitmap(Bitmap bitmap, String strPicName) {
        String savePath;
        File filePic;

        savePath = PicPath;
        try {
            filePic = new File(savePath + strPicName + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("xxx", "saveBitmap: 2return");
            return;
        }
        Log.d("xxx", "saveBitmap: " + filePic.getAbsolutePath());
    }

    public static void WriteLogTofile(String line) {

        String strTime = Publicfun.toData(System.currentTimeMillis());
        String content = strTime + ":" + line + "\r\n";
        File file = new File(LogPath
                + "Log.txt");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file, true);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件夹大小(递归)
     *
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(java.io.File file) {

        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);

                } else {
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }


    /**
     * 获取当前文件夹大小，不递归子文件夹
     *
     * @param file
     * @return
     */
    public static long getCurrentFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    //跳过子文件夹

                } else {
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return size;
    }

    //重载
    public static long getCurrentFolderSize(File[] fileList) {
        long size = 0;
        try {
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    //跳过子文件夹

                } else {
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return size;
    }


    /**
     * 删除指定目录下文件及目录
     *
     * @param deleteThisPath
     * @param filePath
     * @return
     */
    public static boolean deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 处理目录
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {// 如果是文件，删除
                        file.delete();
                    } else {// 目录
                        if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                            file.delete();
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


//    /**
//     * 删除指定目录下文件
//     *
//     * @param filePath
//     * @return
//     */
//    public static void deleteFile(String filePath) {
//        if (!TextUtils.isEmpty(filePath)) {
//            try {
//                File file = new File(filePath);
//                java.io.File[] fileList = file.listFiles();
//                for (int i = 0; i < fileList.length; i++) {
//                    if (!fileList[i].isDirectory()) {
//                        fileList[i].delete();
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }


    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte(s)";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }

    /**
     * 按文件修改时间排序
     * @param filePath
     */
    public static ArrayList<String> orderByDate(String filePath) {
        ArrayList<String> FileNameList = new ArrayList<String>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;// 如果 if 中修改为 返回-1 同时此处修改为返回 1 排序就会是递减
            }

            public boolean equals(Object obj) {
                return true;
            }

        });

        for (File file1 : files) {
            if (file1.isDirectory()) {
                FileNameList.add(file1.getName());
            }
        }
        return FileNameList;
    }

    //重载
    public static ArrayList<String> orderByDate( File[] files) {
        ArrayList<String> FileNameList = new ArrayList<String>();
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;// 如果 if 中修改为 返回-1 同时此处修改为返回 1 排序就会是递减
            }

            public boolean equals(Object obj) {
                return true;
            }

        });

        for (File file1 : files) {
            if (file1.isFile()) {
                FileNameList.add(file1.getName());
            }
        }
        return FileNameList;
    }

    /**
     * 获取图片类型
     * JPG图片头信息:FFD8FF
     * PNG图片头信息:89504E47
     * GIF图片头信息:47494638
     * BMP图片头信息:424D
     *
     * @param is 图片文件流
     * @return 图片类型:jpg|png|gif|bmp
     */
    public static int getImageType(InputStream is) {
        int iRet = 0;
        String JPG_HEX="ffd8ff";  //* JPG图片头信息:FFD8FF
        String PNG_HEX="89504e47";  //* PNG图片头信息:89504E47
        String BMP_HEX="424d";  //* BMP图片头信息:424D
        if (is != null) {
            byte[] b = new byte[4];
            try {
                is.read(b, 0, b.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String hexStr = Bytes2Hexstr(b);//图片文件流前4个字节的头信息（子文字母）
            if (hexStr != null) {
                if ((hexStr.startsWith(JPG_HEX))
                ||(hexStr.startsWith(PNG_HEX))
                ||(hexStr.startsWith(BMP_HEX))){
                    return 0;
                }
            }
        }
        return 1;
    }

    public static int getImageType(byte[] data) {

        String JPG_HEX="ffd8ff";  //* JPG图片头信息:FFD8FF
        String PNG_HEX="89504e47";  //* PNG图片头信息:89504E47
        String BMP_HEX="424d";  //* BMP图片头信息:424D

        byte[] b = new byte[4];
        memcpy(b, data ,b.length);
        String hexStr = Bytes2Hexstr(b);//图片文件流前4个字节的头信息（子文字母）
        if (hexStr != null) {
            if ((hexStr.startsWith(JPG_HEX))
                    ||(hexStr.startsWith(PNG_HEX))
                    ||(hexStr.startsWith(BMP_HEX))){
                Log.d(TAG,"图片格式"+hexStr);
                return 0;
            }
        }
        return 1;
    }

}
