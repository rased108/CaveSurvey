package com.astoev.cave.survey.util;

import android.net.Uri;
import android.support.v4.content.FileProvider;

import org.apache.poi.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * A set of tools for file operations
 */
public class FileUtils {

    public static void deleteQuietly(File aFile) {
        if (aFile != null) {
            try {
                aFile.delete();
            } catch (Exception e) {
                // noop
            }
        }
    }

    public static Uri getFileUri(File file) {
        return FileProvider.getUriForFile(ConfigUtil.getContext(),  "CaveSurvey.provider", file);
    }

    public static String loadFileContents(String aFile) throws IOException {
        BufferedReader br = null;
        StringBuilder text = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(aFile));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }

            return text.toString();
        } finally {
            IOUtils.closeQuietly(br);
        }
    }

}
