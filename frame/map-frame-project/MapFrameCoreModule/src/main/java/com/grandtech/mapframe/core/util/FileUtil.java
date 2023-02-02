package com.grandtech.mapframe.core.util;

import android.content.Context;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by zy on 2016/10/18.
 */

public class FileUtil {

    /**
     * 复制asset文件到指定目录
     *
     * @param oldPath asset下的路径
     * @param newPath SD卡下保存路径
     */
    public static Boolean copyAssets(Context context, String oldPath, String newPath) {
        Boolean flag = true;
        try {
            File file = new File(newPath);
            if (file.exists()) {
                return flag;
            }
            String fileNames[] = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                file = new File(newPath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {// 如果是文件
                if (oldPath != null && oldPath.contains("file:///android_asset/")) {
                    oldPath = oldPath.replace("file:///android_asset/", "");
                }
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
            flag = true;
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }


    public static Boolean copyFile(String oldPath, String newPath) {
        Boolean flag = true;
        try {
            File file = new File(newPath);
            if (file.exists()) {
                return flag;
            }
            InputStream is = new FileInputStream(new File(oldPath));
            FileOutputStream fos = new FileOutputStream(new File(newPath));
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                // buffer字节
                fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
            }
            fos.flush();// 刷新缓冲区
            is.close();
            fos.close();
            flag = true;
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    public static Boolean copyFileAnyhow(String oldPath, String newPath) {
        Boolean flag = true;
        try {
            createFileParentPath(newPath);
            InputStream is = new FileInputStream(new File(oldPath));
            FileOutputStream fos = new FileOutputStream(new File(newPath));
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                // buffer字节
                fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
            }
            fos.flush();// 刷新缓冲区
            is.close();
            fos.close();
            flag = true;
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }


    public static void copyDir(String sourcePath, String newPath) {
        File file = new File(sourcePath);
        String[] filePath = file.list();
        if (!(new File(newPath)).exists()) {
            (new File(newPath)).mkdir();
        }
        for (int i = 0; i < filePath.length; i++) {
            if ((new File(sourcePath + file.separator + filePath[i])).isDirectory()) {
                copyDir(sourcePath + file.separator + filePath[i], newPath + file.separator + filePath[i]);
            }
            if (new File(sourcePath + file.separator + filePath[i]).isFile()) {
                copyFile(sourcePath + file.separator + filePath[i], newPath + file.separator + filePath[i]);
            }
        }
    }


    public static boolean isExist(String path) {
        File f = new File(path);
        return f.exists();
    }

    public static long getFileSizeByPath(String path) {
        File file = new File(path);
        if (file != null && file.exists()) {
            return file.length();
        } else {
            return 0;
        }
    }

    public static boolean reNameFileExtend(String path, String extend) {
        if (isExist(path)) {
            String newPath = path.substring(0, path.lastIndexOf(".")) + extend;
            File oldfile = new File(path);
            File newfile = new File(newPath);
            if (!isExist(newPath)) {
                return oldfile.renameTo(newfile);
            }
        }
        return false;
    }

    /**
     * 重命名一个文件夹
     *
     * @param path
     * @param name
     * @return
     */
    public static boolean reNameFolder(String path, String name) {
        if (isExist(path)) {
            String newPath = getParentPathByPath(path) + "/" + name;
            File oldfile = new File(path);
            File newfile = new File(newPath);
            if (!isExist(newPath)) {
                return oldfile.renameTo(newfile);
            }
        }
        return false;
    }


    /**
     * 给定文件路径 获取文件名
     *
     * @param path
     * @return
     */
    public static String getFileNameByPath(String path) {
        try {
            String fileName = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 给定文件路径 获取文件后缀名
     *
     * @param path
     * @return
     */
    public static String getFileExtendNameByPath(String path) {
        try {
            String fileName = path.substring(path.lastIndexOf('.'));
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 给定文件路径 获取文件后缀名
     *
     * @param path
     * @return
     */
    public static String getFilePathWithOutExtendName(String path) {
        try {
            String fileName = path.substring(0, path.lastIndexOf('.'));
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 给定文件路径 获取文件父路径
     *
     * @param path
     * @return
     */
    public static String getParentPathByPath(String path) {
        try {
            String parentPath = path.substring(0, path.lastIndexOf('/'));
            return parentPath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 给定文件路径 获取文件名
     *
     * @param path
     * @return
     */
    public static String getFileFullNameByPath(String path) {
        try {
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 给定文件路径 获取文件父路径
     *
     * @param path
     * @return
     */
    public static File getParentFileByPath(String path) {
        try {
            String parentPath = path.substring(path.lastIndexOf('/') + 1);
            File _file = new File(parentPath);
            if (_file.exists()) {
                return _file;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查找path 路径下的所有文件和文件夹
     *
     * @param path
     * @return
     */
    public static Map<File, String> scanFile(String path) {
        Map<File, String> map = new HashMap<>();
        File _file = new File(path);
        if (_file != null && _file.isDirectory()) {
            File[] _fs = _file.listFiles();

            return null;
        } else {
            map.put(new File(path), "");
        }
        return map;
    }

    /**
     * 非递归
     *
     * @param path
     */
    public static Map<File, String> fileTree(String path, String filter) {
        Map<File, String> mapTree = new HashMap<>();
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<>();
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    System.out.println("文件夹:" + file2.getAbsolutePath());
                    list.add(file2);
                    folderNum++;
                } else {
                    System.out.println("文件:" + file2.getAbsolutePath());
                    fileNum++;

                    String _name = file2.getName();
                    String _path = file2.getAbsolutePath();
                    _path = _path.replace(path + "/", "");
                    _path = _path.replace(_name, "");
                    if (filter != null && !_name.endsWith(filter)) {
                        mapTree.put(file2, _path);
                    }
                }
            }
            File temp_file;
            while (!list.isEmpty()) {
                temp_file = list.removeFirst();
                files = temp_file.listFiles();
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        System.out.println("文件夹:" + file2.getAbsolutePath());
                        list.add(file2);
                        folderNum++;
                    } else {
                        System.out.println("文件:" + file2.getAbsolutePath());
                        fileNum++;

                        String _name = file2.getName();
                        String _path = file2.getAbsolutePath();
                        _path = _path.replace(path + "/", "");
                        _path = _path.replace(_name, "");
                        if (filter != null && !_name.endsWith(filter)) {
                            mapTree.put(file2, _path);
                        }
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
        System.out.println("文件夹共有:" + folderNum + ",文件共有:" + fileNum);
        return mapTree;
    }

    /**
     * 非递归
     *
     * @param path
     */
    public static void traverseFolder(String path) {
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<File>();
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    System.out.println("文件夹:" + file2.getAbsolutePath());
                    list.add(file2);
                    folderNum++;
                } else {
                    System.out.println("文件:" + file2.getAbsolutePath());
                    fileNum++;
                }
            }
            File temp_file;
            while (!list.isEmpty()) {
                temp_file = list.removeFirst();
                files = temp_file.listFiles();
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        System.out.println("文件夹:" + file2.getAbsolutePath());
                        list.add(file2);
                        folderNum++;
                    } else {
                        System.out.println("文件:" + file2.getAbsolutePath());
                        fileNum++;
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
        System.out.println("文件夹共有:" + folderNum + ",文件共有:" + fileNum);
    }


    /**
     * 删除指定文件所在文件夹 所有同名的文件 后缀可以不同
     *
     * @param path
     * @return
     */
    public static boolean deleteSameNameFile(String path) {
        boolean flag;
        if (isExist(path)) {
            try {
                File parentFile = new File(path).getParentFile();
                String fileName = getFileNameByPath(path);
                String fileExtendName = getFileExtendNameByPath(path);
                File[] files = parentFile.listFiles();
                for (File f : files) {
                    String name = f.getName().substring(0, f.getName().lastIndexOf('.'));
                    if (name.equals(fileName)) {
                        f.delete();
                    }
                }
                flag = true;
            } catch (Exception e) {
                flag = false;
            }
        } else {
            flag = false;
        }
        return false;
    }

    /**
     * 文件删除成功 true；不存在或者未成功返回 false
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        if (isExist(path)) {
            File f = new File(path);
            return f.delete();
        } else {
            return false;
        }
    }

    /**
     * 创建指定文件夹
     *
     * @param path
     */
    public static void createFile(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    /**
     * 创建指定文件夹
     *
     * @param path
     */
    public static void createFileParentPath(String path) {
        int lastIndex = path.lastIndexOf("/");
        path = path.substring(0, lastIndex);
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    /**
     * 创建指定文件夹(必要时创建父文件夹)
     *
     * @param path
     * @return
     */
    public static Boolean createAllFile(String path) {
        File f = new File(path);
        if (!f.exists()) {
            return f.mkdirs();
        }
        return false;
    }

    /**
     * 迭代打印文件名称
     *
     * @param path
     */
    public static void getFileNameInFolider(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        for (File f : files) {
            System.out.println(f.getAbsoluteFile().toString());
            if (f.isDirectory()) {
                path = f.getAbsolutePath();
                getFileNameInFolider(path);
            }
        }
    }


    /**
     * 获取一个文件夹下所有的文件名称
     *
     * @param path         指定父类文件夹路径
     * @param filterString 文件名称过滤字段
     * @param recursion    是否迭代遍历
     * @return
     */
    public static List<Object> getFileNameInFolder(String path, String filterString, boolean recursion) {
        List<Object> nameArray = new ArrayList<>();
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            return null;
        }
        for (File f : files) {
            if (f.isDirectory() && recursion) {
                path = f.getAbsolutePath();
                nameArray.addAll(getFileNameInFolder(path, filterString, recursion));
            } else {
                String fileName = f.getName();
                if (filterString != null && fileName.contains(filterString)) {
                    nameArray.add(fileName);
                }
                if (filterString == null) {
                    nameArray.add(fileName);
                }
            }
        }
        return nameArray;
    }


    /**
     * 获取一个文件夹下所有的文件路径
     *
     * @param path         指定父类文件夹路径
     * @param filterString 文件名称过滤字段
     * @param recursion    是否迭代遍历
     * @return
     */
    public static List<Object> getFilePathInFolder(String path, String filterString, boolean recursion) {
        List<Object> nameArray = new ArrayList<>();
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            return null;
        }
        for (File f : files) {
            if (f.isDirectory() && recursion) {
                path = f.getAbsolutePath();
                nameArray.addAll(getFilePathInFolder(path, filterString, recursion));
            } else {
                String fPath = f.getPath();
                if (filterString != null && fPath.contains(filterString)) {
                    nameArray.add(fPath);
                }
                if (filterString == null) {
                    nameArray.add(fPath);
                }
            }
        }
        return nameArray;
    }


    /**
     * 获取一个文件夹下所有的文件路径
     *
     * @param path         指定父类文件夹路径
     * @param filterString 文件名称过滤字段
     * @param recursion    是否迭代遍历
     * @return
     */
    public static List<Object> getFilePathInFolder(String path, String[] filterString, boolean recursion) {
        List<Object> nameArray = new ArrayList<>();
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            return null;
        }
        for (File f : files) {
            if (f.isDirectory() && recursion) {
                path = f.getAbsolutePath();
                nameArray.addAll(getFilePathInFolder(path, filterString, recursion));
            } else {
                String fPath = f.getPath();
                if (filterString != null) {
                    for (int i = 0; i < filterString.length; i++) {
                        String filter = filterString[i];
                        if (fPath.contains(filter)) {
                            nameArray.add(fPath);
                            break;
                        }
                    }
                } else {
                    nameArray.add(fPath);
                }
            }
        }
        return nameArray;
    }


    public static String[] getPathInFolder(String path, String[] filterString, boolean recursion) {
        List<Object> paths = getFilePathInFolder(path, filterString, recursion);
        if (paths != null) {
            String[] filePath = new String[paths.size()];
            for (int i = 0; i < filePath.length; i++) {
                filePath[i] = paths.get(i).toString();
            }
            return filePath;
        }
        return null;
    }

    /**
     * 获取文件大小
     *
     * @param path
     * @return
     */
    public static long getFileSize(String path) {
        String[] paths = getPathInFolder(path, null, true);
        long sumSize = 0;
        File file;
        if (paths != null) {
            for (int i = 0; i < paths.length; i++) {
                file = new File(paths[i]);
                sumSize += file.length();
            }
        } else {
            sumSize = 0;
        }
        return sumSize;
    }

    public static long getFileSize(File file) {
        long sumSize = 0;
        if (file != null) {
            if (file.isDirectory()) {
                String[] paths = getPathInFolder(file.getPath(), null, true);
                File _file;
                if (paths != null) {
                    for (int i = 0; i < paths.length; i++) {
                        _file = new File(paths[i]);
                        sumSize += _file.length();
                    }
                } else {
                    sumSize = 0;
                }
            } else {
                sumSize = file.length();
            }
        }
        return sumSize;
    }


    public static String readRemoteFile(String filePath, boolean isNextLine) {
        StringBuilder fileContent = new StringBuilder();
        try {
            InputStream is = new FileInputStream(filePath);
            String line; // 用来保存每行读取的内容
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            line = reader.readLine(); // 读取第一行
            while (line != null) { // 如果 line 为空说明读完了
                fileContent.append(line); // 将读到的内容添加到 buffer 中
                if (isNextLine) {
                    fileContent.append("\n"); // 添加换行符
                }
                line = reader.readLine(); // 读取下一行
            }
            reader.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileContent.toString();
    }

    public static String convertCodeAndGetText(String filePath) {// 转码

        File file = new File(filePath);
        BufferedReader reader;
        String text = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(fis);
            in.mark(4);
            byte[] first3bytes = new byte[3];
            in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
            in.reset();
            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB && first3bytes[2] == (byte) 0xBF) {// utf-8
                reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFE) {
                reader = new BufferedReader(new InputStreamReader(in, "unicode"));
            } else if (first3bytes[0] == (byte) 0xFE && first3bytes[1] == (byte) 0xFF) {
                reader = new BufferedReader(new InputStreamReader(in, "utf-16be"));
            } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFF) {
                reader = new BufferedReader(new InputStreamReader(in, "utf-16le"));
            } else {
                reader = new BufferedReader(new InputStreamReader(in, "GBK"));
            }
            String str = reader.readLine();
            while (str != null) {
                text = text + str + "/n";
                str = reader.readLine();
            }
            reader.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }


    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }


    /**
     * 根据文件计算出文件的MD5
     *
     * @param file
     * @return
     */
    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }

        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    public static String getFileAttr(File file) {
        return file.getName() + file.length() + "";
    }


    /**
     * 获取文件夹中的文件的MD5值
     *
     * @param file
     * @param listChild
     * @return
     */
    public static Map<String, String> getDirMD5(File file, boolean listChild) {
        if (!file.isDirectory()) {
            return null;
        }

        Map<String, String> map = new HashMap<>();
        String md5;

        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file2 = files[i];
            if (file2.isDirectory() && listChild) {
                map.putAll(getDirMD5(file2, listChild));
            } else {
                md5 = getFileMD5(file2);
                if (md5 != null) {
                    map.put(file2.getPath(), md5);
                }
            }
        }
        return map;
    }


    /**
     * 非递归（广度优先）
     *
     * @param path
     */
    public static void traverseFolder_Breadth(String path) {
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<>();
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    System.out.println("文件夹:" + file2.getAbsolutePath());
                    list.add(file2);
                    folderNum++;
                } else {
                    System.out.println("文件:" + file2.getAbsolutePath());
                    fileNum++;
                }
            }
            File temp_file;
            while (!list.isEmpty()) {
                temp_file = list.removeFirst();
                files = temp_file.listFiles();
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        System.out.println("文件夹:" + file2.getAbsolutePath());
                        list.add(file2);
                        folderNum++;
                    } else {
                        System.out.println("文件:" + file2.getAbsolutePath());
                        fileNum++;
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
        System.out.println("文件夹共有:" + folderNum + ",文件共有:" + fileNum);
    }

    /**
     * 获取一个文件夹下所有的文件路径
     *
     * @param path         指定父类文件夹路径
     * @param filterString 文件名称过滤字段
     * @param recursion    是否迭代遍历
     * @return
     */
    public static Map<String, String> getFilePathInFolder2Map(String path, String filterString, boolean recursion) {
        Map<String, String> nameMap = new HashMap<>();
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            return null;
        }
        for (File f : files) {
            if (f.isDirectory() && recursion) {
                path = f.getAbsolutePath();
                nameMap.putAll(getFilePathInFolder2Map(path, filterString, recursion));
            } else {
                String fPath = f.getPath();
                if (filterString != null && fPath.contains(filterString)) {
                    nameMap.put(f.getName().replace(".jpg", ""), fPath);
                }
                if (filterString == null) {
                    nameMap.put(f.getName().replace(".jpg", ""), fPath);
                }
            }
        }
        return nameMap;
    }

    public static void copyFileUsingFileStreams(String sourcee, String destt)
            throws IOException {
        File source = new File(sourcee);
        if (!source.exists()) {
            return;
        }
        int lastIndex = destt.lastIndexOf("/");
        String parentPath = destt.substring(0, lastIndex);
        File f = new File(parentPath);
        if (!f.exists()) {
            f.mkdirs();
        }
        File dest = new File(destt);
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
            source.delete();
        }
    }

    public static File getSaveFile(Context context) {
        File file = new File(context.getFilesDir(), "pic.jpg");
        //File file = new File(context.getExternalFilesDir(""),"pic.jpg"  );
        return file;
    }


    /**
     * 照片转byte二进制
     * @param imagepath 需要转byte的照片路径
     * @return 已经转成的byte
     * @throws Exception
     */
    public static byte[] readStream(String imagepath) throws Exception {
        FileInputStream fs = new FileInputStream(imagepath);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while (-1 != (len = fs.read(buffer))) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        fs.close();
        return outStream.toByteArray();

    }
}
