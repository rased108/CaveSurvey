package com.astoev.cave.survey.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by astoev on 12/25/13.
 */
public class FileStorageUtil {

    private static final String EXCEL_FILE_EXTENSION = ".xls";
    private static final String PNG_FILE_EXTENSION = ".png";
    private static final String NAME_DELIMITER = "_";
    private static final String POING_PREFIX = "Point";

    private static final String CAVE_SURVEY_FOLDER = "CaveSurvey";
    private static final String TIME_PATTERN = "yyyyMMdd";
    private static final int MIN_REQUIRED_STORAGE = 50 * 1024;


    public static File getProjectHome(Project aProject) {
        File storageHome = getStorageHome();
        if (storageHome == null) {
            return null;
        }
        File projectHome = new File(storageHome, aProject.getName());
        if (!projectHome.exists()) {
            boolean projectHomeCreated = projectHome.mkdirs();
            if (!projectHomeCreated) {
                Log.e(Constants.LOG_TAG_UI, "Failed to create folder " + projectHome.getAbsolutePath());
                return null;
            }
            Log.i(Constants.LOG_TAG_SERVICE, "Project home created");
        }
        return projectHome;
    }

    @SuppressLint("SimpleDateFormat")
	public static String addProjectExport(Project aProject, InputStream aStream) {

        File projectHome = getProjectHome(aProject);
        if (projectHome == null) {
            return null;
        }

        FileOutputStream out = null;
        try {

            int index = 1;
            String exportName;
            File exportFile;
            SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);

            // ensure unique name
            while (true) {
                exportName = aProject.getName() + NAME_DELIMITER + dateFormat.format(new Date()) + NAME_DELIMITER + index;
                exportFile = new File(projectHome, exportName + EXCEL_FILE_EXTENSION);
                if (exportFile.exists()) {
                    index++;
                } else {
                    break;
                }
            }

            Log.i(Constants.LOG_TAG_SERVICE, "Store to " + exportFile.getAbsolutePath());

            out = new FileOutputStream(exportFile);
            IOUtils.copy(aStream, out);
            return exportFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to store export", e);
            return null;
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(aStream);
        }
    }
    
    /**
     * Helper method that obtains Picture's directory (api level 8) 
     * 
     * @param projectName - project's name used as an album
     * @return File created
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    private static File getDirectoryPicture(String projectName){
    	return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), projectName);
    }

    /**
     * Helper method that adds project's media content to public external storage
     * 
     * @param contextArg     - context
     * @param aProject       - project owner
     * @param activePointArg - parent point
     * @param byteArrayArg   - media content as a byte array
     * @return String for the file name created
     * @throws Exception
     */
    @SuppressLint("SimpleDateFormat")
	public static String addProjectMedia(Context contextArg, Project aProject, Point activePointArg, byte[] byteArrayArg) throws Exception {

    	if (!isExternalStorageWritable())
    	{
    		Log.e(Constants.LOG_TAG_SERVICE, "Storage not available for writing");
    		throw new Exception();
    	}
    	
    	boolean isPublicFolder = true;
    	
    	// Store in file system
    	// use the project name as an albumName
    	String projectName = aProject.getName();
    	
    	File destinationDir = null;	
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO){
    		// api 8+
            // Get the directory for the app's public pictures directory. 
            destinationDir = getDirectoryPicture(projectName);
    	} else {
    		// api level 7
    		destinationDir = new File(Environment.getExternalStorageDirectory(), projectName);
    		isPublicFolder = false;
    	}
    	
    	if (!destinationDir.isDirectory()){
	        if (!destinationDir.mkdirs()) {
	            Log.e(Constants.LOG_TAG_SERVICE, "Directory not created");
	        }
    	}
        
        Log.i(Constants.LOG_TAG_SERVICE, "Will write at: " + destinationDir.getAbsolutePath());
        
        // build filename
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
        
        StringBuilder fileName = new StringBuilder(POING_PREFIX);
        fileName.append(activePointArg.getName());
        fileName.append(NAME_DELIMITER);
        fileName.append(df.format(date));
        fileName.append(PNG_FILE_EXTENSION);
        
        File pictureFile = new File(destinationDir, fileName.toString());
        
        OutputStream os = null;
        try {
			os = new FileOutputStream(pictureFile);
			os.write(byteArrayArg);
		} catch (Exception e) {
			Log.e(Constants.LOG_TAG_SERVICE, "Unable to write file: " + pictureFile.getAbsolutePath(), e);
			throw e;
		} finally {
			closeOutputStream(os);
		}

        Log.i(Constants.LOG_TAG_SERVICE, "Just wrote: " + pictureFile.getAbsolutePath());
        
        // broadcast that picture was added to the projects if the folder is public (api level 8+)
        if (isPublicFolder){
        	notifyPictureAddedToGalery(contextArg, pictureFile);
        }
        return pictureFile.getAbsolutePath();
    }

    private static File getStorageHome() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.e(Constants.LOG_TAG_UI, "Storage unavailable");
            return null;
        }
        File extdir = Environment.getExternalStorageDirectory();
        StatFs stats = new StatFs(extdir.getAbsolutePath());
        int availableBytes = stats.getAvailableBlocks() * stats.getBlockSize();
        if (availableBytes < MIN_REQUIRED_STORAGE) {
            Log.e(Constants.LOG_TAG_UI, "No space left");
            return null;
        }

        File storageHome = new File(Environment.getExternalStorageDirectory() + File.separator + CAVE_SURVEY_FOLDER);
        if (!storageHome.exists()) {
            boolean exportFolderCreated = storageHome.mkdirs();
            if (!exportFolderCreated) {
                Log.e(Constants.LOG_TAG_UI, "Failed to create folder " + storageHome.getAbsolutePath());
                return null;
            }
            Log.i(Constants.LOG_TAG_SERVICE, "Export folder created");
        }
        return storageHome;
    }
    
    /**
     * Helper method to close safely an OutputStream
     * 
     * @param os - output stream instance
     */
    public static void closeOutputStream(OutputStream os){
		if (os != null){
			try {
				os.close();
			} catch (IOException e) {
				Log.i(Constants.LOG_TAG_SERVICE, "Error while closing output stream");
			}
		}
    }
    
    /**
     * Helper method that checks if the external storage is available for writing
     * 
     * @return  true if available for writing, otherwise false
     */
    public static boolean isExternalStorageWritable(){
    	String state = Environment.getExternalStorageState();
    	return (Environment.MEDIA_MOUNTED.equals(state));
    }
    
    /**
     * Helper method that invokes system's media scanner to add a picture to Media Provider's database
     * 
     * @param contextArg   - context to use to send a broadcast
     * @param addedFileArg - the newly created file to notify for
     */
    public static void notifyPictureAddedToGalery(Context contextArg, File addedFileArg){
    	if (addedFileArg == null){
    		return;
    	}
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(addedFileArg);
        mediaScanIntent.setData(contentUri);
        contextArg.sendBroadcast(mediaScanIntent);
    }
}