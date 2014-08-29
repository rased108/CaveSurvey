package com.astoev.cave.survey.service.export.json;

import android.content.Context;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Location;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.service.export.AbstractExport;
import com.astoev.cave.survey.util.StringUtils;
import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by astoev on 8/28/14.
 */
public class OpensTopoJsonExport extends AbstractExport {

    private List<Map<String, Object>> rows;
    private Map<String, Object> row;

    public OpensTopoJsonExport(Context aContext) {
        super(aContext);
    }

    @Override
    protected void prepare(Project aProject) {
        Log.i(Constants.LOG_TAG_SERVICE, "Start JSON export ");
        rows = new ArrayList<Map<String, Object>>();
    }

    @Override
    protected void prepareEntity(int rowCounter) {
        row = new HashMap<String, Object>();
        rows.add(row);
    }

    @Override
    protected InputStream getContent() {
        String json = new Gson().toJson(rows);
        return IOUtils.toInputStream(json);
    }

    @Override
    protected String getExtension() {
        return ".json";
    }

    @Override
    protected void setValue(Entities entityType, String aLabel) {
        populateValue(entityType, aLabel);
    }

    @Override
    protected void setValue(Entities entityType, Float aValue) {
        populateValue(entityType, aValue);
    }

    private void populateValue(Entities entityType, Object aValue) {
        switch (entityType) {
            case FROM:
                row.put("from", aValue);
                break;
            case TO:
                row.put("to", aValue);
                break;
            case DISTANCE:
                row.put("len", aValue);
                break;
            case COMPASS:
                row.put("compass", aValue);
                break;
            case INCLINATION:
                row.put("clino", aValue);
                break;
            case LEFT:
                row.put("left", aValue);
                break;
            case RIGHT:
                row.put("right", aValue);
                break;
            case UP:
                row.put("top", aValue);
                break;
            case DOWN:
                row.put("bottom", aValue);
                break;
            case NOTE:
                row.put("note", aValue);
                break;
        }
    }

    @Override
    protected void setPhoto(Photo photo) {
        // not needed
    }

    @Override
    protected void setLocation(Location aLocation) {
        // not needed
    }

    @Override
    protected void setDrawing(Sketch aSketch) {
        // not needed
    }
}
