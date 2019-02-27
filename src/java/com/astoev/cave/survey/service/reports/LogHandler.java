package com.astoev.cave.survey.service.reports;

import android.util.Log;

import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.StreamUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.astoev.cave.survey.Constants.LOG_TAG_SERVICE;

public class LogHandler extends Handler {

    private static final String LOG_FILE_NAME = "CaveSurvey.log";

    private File logFile;
    private OutputStream out = null;
    private FileStorageUtil mFileStorageUtil;

    public LogHandler() throws FileNotFoundException {
        Log.i(LOG_TAG_SERVICE, "Starting debug session");
        logFile = new File(FileStorageUtil.getStorageHome(), LOG_FILE_NAME);
        out = new FileOutputStream(logFile);
    }

    @Override
    public void publish(LogRecord aLogRecord) {
        try {
            StringBuilder msg = new StringBuilder();
            msg.append(aLogRecord.getLevel());
            out.write(msg.toString().getBytes());
        } catch (IOException aE) {
            Log.e(LOG_TAG_SERVICE, "Failed to write message", aE);
        }
    }

    @Override
    public void flush() {
        try {
            out.flush();
        } catch (IOException aE) {
            Log.e(LOG_TAG_SERVICE, "Failed to flush", aE);
        }
    }

    @Override
    public void close() throws SecurityException {
        StreamUtil.closeQuietly(out);
    }

    public String getLogFile() {
        return logFile.getPath();
    }
}
