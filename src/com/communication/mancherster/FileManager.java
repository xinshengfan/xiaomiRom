package com.communication.mancherster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class FileManager {

    private String FILE_PATH = "/sportdata/";
    
    private String OTHER_FILE_PATH = "/otherdata/";

    /**
     * 
     * @param context
     * @return
     */
    public String getMsgPath(Context context) {
        // "/sdcard/codoonmsg"+MESSAGE_PATH;//
        String path = context.getFilesDir().getAbsolutePath() + FILE_PATH;
        File file=new File(path);
        if (!file.exists()) {
            file.mkdir();
            Log.d("debugfile", "create file dir");
        }
        return path;
    }
    
    /**
     * 
     * @param context
     * @return
     */
    public String getOtherPath(Context context) {
        // "/sdcard/codoonmsg"+MESSAGE_PATH;//
        String path = context.getFilesDir().getAbsolutePath() + OTHER_FILE_PATH;
        File file=new File(path);
        if (!file.exists()) {
            file.mkdir();
            Log.d("debugfile", "create file dir");
        }
        return path;
    }
    
    /**
     * 
     * @param context
     * @return
     */
    public String getMsgPathSD(Context context) {
        // "/sdcard/codoonmsg"+MESSAGE_PATH;//
        String path = context.getFilesDir().getAbsolutePath() + FILE_PATH;
        File file=new File(path);
        if (!file.exists()) {
            file.mkdir();
            Log.d("debugfile", "create file dir");
        }
        return path;
    }

    /**
     * 
     * @param context
     * @param fileName
     * @param content
     */
    public void saveContent(Context context, String userID,String fileName, String content) {
        String path = getMsgPath(context) + userID+"_"+fileName + ".txt";
        File file = new File(path);

        try {
            if (!file.exists()) {
              //  file.createNewFile();
               file.createNewFile();
               Log.d("debugfile", "create file filename");
            }
            FileOutputStream fileOutput = new FileOutputStream(file);
            fileOutput.write(content.getBytes());
            fileOutput.flush();
            fileOutput.close();

            Log.d("debugfile", "save:" + file.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @param context
     * @param fileName
     * @param content
     */
    public void saveContentOther(Context context, String fileName, String content) {
        String path = getOtherPath(context) + fileName + ".txt";
        File file = new File(path);

        try {
            if (!file.exists()) {
              //  file.createNewFile();
               file.createNewFile();
               Log.d("debugfile", "create file filename");
            }
            FileOutputStream fileOutput = new FileOutputStream(file);
            fileOutput.write(content.getBytes());
            fileOutput.flush();
            fileOutput.close();

            Log.d("debugfile", "save:" + file.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveContentSD(Context context, String fileName, String content) {
        String path = getMsgPathSD(context) + fileName + ".txt";
        File file = new File(path);

        try {
            if (!file.exists()) {
              //  file.createNewFile();
               file.createNewFile();
               Log.d("debugfile", "create file filename");
            }
            FileOutputStream fileOutput = new FileOutputStream(file);
            fileOutput.write(content.getBytes());
            fileOutput.flush();
            fileOutput.close();

            Log.d("debugfile", "save:" + file.getPath());
        } catch (FileNotFoundException e) {
            Log.d("debugfile", "error:"+e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("debugfile", "error:"+e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void saveXmlToSD(Context context, String fileName, String content) {
        String path = getMsgPathSD(context) + fileName ;
        File file = new File(path);

        try {
            if (!file.exists()) {
              //  file.createNewFile();
               file.createNewFile();
               Log.d("debugfile", "create file filename");
            }
            FileOutputStream fileOutput = new FileOutputStream(file);
            fileOutput.write(content.getBytes());
            fileOutput.flush();
            fileOutput.close();

            Log.d("debugfile", "save:" + file.getPath());
        } catch (FileNotFoundException e) {
            Log.d("debugfile", "error:"+e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("debugfile", "error:"+e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @param context
     * @param fileName
     * @return
     */
    public String getContentSD(Context context, String fileName) {

        String path = getMsgPathSD(context) + (fileName.endsWith(".txt") ? fileName : (fileName + ".txt"));
        File file = new File(path);

        try {
            if (!file.exists()) {
                return null;
            }

            InputStream inputStream = new FileInputStream(file);

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            BufferedReader buffer = new BufferedReader(inputStreamReader);
            StringBuilder strApp = new StringBuilder();

            String line = null;
            while ((line = buffer.readLine()) != null) {
                strApp.append(line);

            }
            return strApp.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 
     * @param context
     * @param fileName
     * @return
     */
    public String getOtherContent(Context context, String fileName) {

        String path = getOtherPath(context) + (fileName.endsWith(".txt") ? fileName : (fileName + ".txt"));
        File file = new File(path);

        try {
            if (!file.exists()) {
                return null;
            }

            InputStream inputStream = new FileInputStream(file);

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            BufferedReader buffer = new BufferedReader(inputStreamReader);
            StringBuilder strApp = new StringBuilder();

            String line = null;
            while ((line = buffer.readLine()) != null) {
                strApp.append(line);

            }
            return strApp.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @param context
     * @param fileName
     * @return
     */
    public String getContent(Context context,String fileName) {

        String path = getMsgPath(context) +( (fileName.endsWith(".txt") ? fileName : (fileName + ".txt")));
        File file = new File(path);

        try {
            if (!file.exists()) {
                return null;
            }

            InputStream inputStream = new FileInputStream(file);

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            BufferedReader buffer = new BufferedReader(inputStreamReader);
            StringBuilder strApp = new StringBuilder();

            String line = null;
            while ((line = buffer.readLine()) != null) {
                strApp.append(line);

            }
            return strApp.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @param context
     * @param fileName
     */
    public boolean deleteFile(Context context,String fileName) {
        String path = getMsgPath(context) + (fileName.endsWith(".txt") ? fileName : (fileName + ".txt"));
        File file = new File(path);

        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 
     * @param context
     */
    public ArrayList<File> getNeebUploadData(Context context,String userID) {
        try {
            String path = getMsgPath(context);
            File file = new File(path);

            if (file.exists()) {
                ArrayList<File> files = new ArrayList<File>();
                for (File f : file.listFiles()) {
                    if (f.getName().endsWith(".txt") && f.getName().contains(userID+"_")) {
                        files.add(f);
                    }
                    // Log.d("debug", f.getPath());
                }
                if (files.size() > 0) {
                    return files;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

}
