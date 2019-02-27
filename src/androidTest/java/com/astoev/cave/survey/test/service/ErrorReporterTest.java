package com.astoev.cave.survey.test.service;

import android.util.Log;

import com.astoev.cave.survey.service.reports.ErrorReporter;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.FileUtils;

import junit.framework.TestCase;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.io.IOException;

import static org.mockito.Mockito.doReturn;

public abstract class ErrorReporterTest extends TestCase {

    static {
        System.setProperty("org.mockito.android.target", "/data/user/0/com.astoev.cave.survey/app_target");
    }

    @Spy
    private FileStorageUtil mFileStorageUtil;

    @Test
    public void testErrorReporter() throws IOException {

        mFileStorageUtil = Mockito.mock(FileStorageUtil.class);
        doReturn(true).when(mFileStorageUtil).isExternalStorageWritable();

        assertFalse(ErrorReporter.isDebugRunning());

        ErrorReporter.startDebugSession();
        assertTrue(ErrorReporter.isDebugRunning());

        Log.i("Test", "123");
        Log.e("Exception", "error", new RuntimeException("test"));

        String logFile = ErrorReporter.closeDebugSession();
        assertFalse(ErrorReporter.isDebugRunning());

        String logBody = FileUtils.loadFileContents(logFile);
        assertEquals("alabala", logBody);
    }

}
