package com.astoev.cave.survey.service;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Project;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/12/12
 * Time: 12:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class Options {

    public static String getOptionValue(String aCode) {

        Option o = getOption(aCode);
        if (null != o) {
            return o.getValue();
        } else {
            return null;
        }
    }

    public static Option getOption(String aCode) {

        try {
            QueryBuilder<Option, Integer> query = Workspace.getCurrentInstance().getDBHelper().getOptionsDao().queryBuilder();
            Where where = query.where().eq(Option.COLUMN_CODE, aCode);
            Project currentProject = Workspace.getCurrentInstance().getActiveProject();
            if (currentProject != null) {
                where.and().eq(Option.COLUMN_PROJECT_ID, currentProject.getId());
            }

            return Workspace.getCurrentInstance().getDBHelper().getOptionsDao().queryForFirst(query.prepare());
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG_DB, "Failed to get option " + aCode, e);
            return null;
        }
    }

    public static void createOption(String aCode, String aValue) throws SQLException {
        createOption(aCode, aValue, Workspace.getCurrentInstance().getActiveProject());
    }

    public static void createOption(String aCode, String aValue, Project aProject) throws SQLException {
        Option o = new Option(aCode, aValue, aProject);
        Workspace.getCurrentInstance().getDBHelper().getOptionsDao().create(o);
    }

    /**
     * Helper method to update a option for the active project. Will try to update the option if the
     * new value is different than the old one
     *
     * @param codeArg - option's code
     * @param valueArg - option's new value
     * @throws SQLException if problem during update occurs
     */
    public static void updateOption(String codeArg, String valueArg) throws SQLException{
        Option option = getOption(codeArg);
        if (option == null){
            Log.e(Constants.LOG_TAG_DB, "Unknown option: " + codeArg);
            return;
        }

        if (!option.getValue().equals(valueArg)){
            option.setValue(valueArg);
            Workspace.getCurrentInstance().getDBHelper().getOptionsDao().update(option);
            Log.i(Constants.LOG_TAG_DB, "Updated option: " + codeArg + " with value:" + valueArg);
        }
    }

}
