package com.astoev.cave.survey.activity.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.dto.ProjectConfig;
import com.astoev.cave.survey.fragment.ProjectFragment;
import com.astoev.cave.survey.manager.ProjectManager;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.openstopo.WebViewActivity;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.export.excel.ExcelExport;
import com.astoev.cave.survey.service.export.json.OpensTopoJsonExport;
import com.astoev.cave.survey.service.export.vtopo.VisualTopoExport;
import com.astoev.cave.survey.service.orientation.MagneticOrientationProcessor;
import com.astoev.cave.survey.service.orientation.OrientationDeprecatedProcessor;
import com.astoev.cave.survey.service.orientation.OrientationProcessor;
import com.astoev.cave.survey.service.orientation.OrientationProcessorFactory;
import com.astoev.cave.survey.service.orientation.RotationOrientationProcessor;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.FileUtils;
import com.astoev.cave.survey.util.ProjectInfo;
import com.astoev.cave.survey.util.StringUtils;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/12/12
 * Time: 11:37 AM
 *
 * @author astoev
 * @author jmitrev
 */
public class InfoActivity extends MainMenuActivity {

    public static final String MIME_OPEN_DIRECTORY = "vnd.android.document/directory";

    @Override
    protected boolean showBaseOptionsMenu() {
        return false;
    }

    public static final String MIME_RESOURCE_FOLDER = "resource/folder";
    public static final String MIME_RESOURCE_FILE = "file/*";
    public static final String MIME_TYPE_ANY = "*/*";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        try {
            if (findViewById(R.id.projectInfoContainer) != null){
                if (savedInstanceState == null){
                    ProjectConfig projectConfig = DaoUtil.getProjectConfig();
                    ProjectFragment projectFragment = ProjectFragment.newInstance(projectConfig);

                    getSupportFragmentManager().beginTransaction().add(R.id.projectInfoContainer, projectFragment).commit();
                }
            }

            ProjectInfo projectInfo = DaoUtil.getProjectInfo();

            TextView projectCreated = findViewById(R.id.infoProjectCreated);
            projectCreated.setText(projectInfo.getCreationDate());

            TextView projectNumGalleries = findViewById(R.id.infoNumGalleries);
            projectNumGalleries.setText(String.valueOf(projectInfo.getGalleries()));

            TextView projectNumLegs = findViewById(R.id.infoNumLegs);
            projectNumLegs.setText(String.valueOf(projectInfo.getLegs()));

            TextView projectTotalLength = findViewById(R.id.infoRawDistance);

            String lengthLabel = StringUtils.floatToLabel(projectInfo.getLength()) + " " + Options.getOptionValue(Option.CODE_DISTANCE_UNITS);
            projectTotalLength.setText(lengthLabel);

            TextView projectNumNotes = findViewById(R.id.infoNumNotes);
            projectNumNotes.setText(StringUtils.intToLabel(projectInfo.getNotes()));

            TextView projectNumDrawings = findViewById(R.id.infoNumDrawings);
            projectNumDrawings.setText(StringUtils.intToLabel(projectInfo.getSketches()));

            TextView projectNumCoordinates = findViewById(R.id.infoNumCoordinates);
            projectNumCoordinates.setText(StringUtils.intToLabel(projectInfo.getLocations()));

            TextView projectNumPhotos = findViewById(R.id.infoNumPhotos);
            projectNumPhotos.setText(StringUtils.intToLabel(projectInfo.getPhotos()));

            TextView projectNumVectors = findViewById(R.id.infoNumVectors);
            projectNumVectors.setText(StringUtils.intToLabel(projectInfo.getVectors()));

            // set the value for azimuth build in processor
            if (Option.CODE_SENSOR_INTERNAL.equals(Options.getOption(Option.CODE_AZIMUTH_SENSOR).getValue())) {
                TextView azimuthSensor = findViewById(R.id.info_azimuth_sensor);
                OrientationProcessor orientationProcessor = OrientationProcessorFactory.getOrientationProcessor(this, null);
                if (orientationProcessor.canReadOrientation()) {
                    if (orientationProcessor instanceof RotationOrientationProcessor) {
                        azimuthSensor.setText(getString(R.string.rotation_sensor));
                    } else if (orientationProcessor instanceof MagneticOrientationProcessor) {
                        azimuthSensor.setText(getString(R.string.magnetic_sensor));
                    } else if (orientationProcessor instanceof OrientationDeprecatedProcessor) {
                        azimuthSensor.setText(getString(R.string.orientation_sensor));
                    }
                }
            }

            TextView projectHome = findViewById(R.id.info_project_home);
            String projectHomeFolder = FileStorageUtil.getProjectHome(projectInfo.getName()).getAbsolutePath();
            projectHome.setText(projectHomeFolder);

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to render info activity", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void exportProjectXls() {
        try {
            Log.i(Constants.LOG_TAG_SERVICE, "Start excel export");

            // export legs

            ExcelExport export = new ExcelExport(this);
            String exportPath = export.runExport(getWorkspace().getActiveProject());
            if (StringUtils.isEmpty(exportPath)) {
                UIUtilities.showNotification(this, R.string.export_io_error, exportPath);
            } else {
                UIUtilities.showNotification(this, R.string.export_done, exportPath);
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to export project", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void exportProjectVTopo() {
        try {
            Log.i(Constants.LOG_TAG_SERVICE, "Start vtopo_logo export");

            // export legs

            VisualTopoExport export = new VisualTopoExport(this);
            String exportPath = export.runExport(getWorkspace().getActiveProject());
            if (StringUtils.isEmpty(exportPath)) {
                UIUtilities.showNotification(this, R.string.export_io_error, exportPath);
            } else {
                UIUtilities.showNotification(this, R.string.export_done, exportPath);
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to export project", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    /**
     * Handles click on Files button. Tries to find an activity to show project's folder by sending
     * ACTION_VIEW intent. Sequentially tries to find a compatible activity. First looks for activity to just
     * handle a file path. Second tries to for mime-type "resource/folder" and finally executes a search for
     * mime-type "*.*" and leaves the user to select a matching application.
     */
    private void onViewFiles() {
        String projectName = getWorkspace().getActiveProject().getName();

        Log.i(Constants.LOG_TAG_SERVICE, "View files selected for project:" + projectName);

        File projectHome = FileStorageUtil.getProjectHome(projectName);

        if (projectHome != null) {
            Uri contentUri = FileUtils.getFileUri(projectHome);

            Log.i(Constants.LOG_TAG_SERVICE, "Uri:" + contentUri);

            String chooserTitle = getResources().getString(R.string.info_chooser_open_folder);

            Intent intent = new Intent(Intent.ACTION_VIEW);

            PackageManager packageManager = getPackageManager();

            // Works with ES file manager
            intent.setDataAndType(contentUri, MIME_RESOURCE_FOLDER);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (intent.resolveActivity(packageManager) != null) {
                Log.i(Constants.LOG_TAG_SERVICE, "ACTION_VIEW with resource/folder resolved");

                startActivity(Intent.createChooser(intent, chooserTitle));
                return;
            }

            // works with OI File manager
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(contentUri);
            if (intent.resolveActivity(packageManager) != null) {
                Log.i(Constants.LOG_TAG_SERVICE, "ACTION_VIEW resolved");

                startActivity(Intent.createChooser(intent, chooserTitle));
                return;
            }

            // another test
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(contentUri);
            intent.putExtra("org.openintents.extra.ABSOLUTE_PATH", projectHome.getPath());
            if (intent.resolveActivity(packageManager) != null) {
                Log.i(Constants.LOG_TAG_SERVICE, "ACTION_VIEW with extra resolved");
                startActivity(Intent.createChooser(intent, chooserTitle));
                return;
            }

            // generic folder
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, MIME_RESOURCE_FOLDER);
            if (intent.resolveActivity(packageManager) != null) {
                Log.i(Constants.LOG_TAG_SERVICE, "ACTION_VIEW with folder resolved");
                startActivity(Intent.createChooser(intent, chooserTitle));
                return;
            }

            // open intent
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, MIME_OPEN_DIRECTORY);
            if (intent.resolveActivity(packageManager) != null) {
                Log.i(Constants.LOG_TAG_SERVICE, "ACTION_VIEW with directory resolved");
                startActivity(Intent.createChooser(intent, chooserTitle));
                return;
            }

            // brute force to choose a file manager
            intent = new Intent(Intent.ACTION_VIEW);
            Log.i(Constants.LOG_TAG_SERVICE, "ACTION_VIEW with */* resolved");
            intent.setDataAndType(contentUri, MIME_TYPE_ANY);
            startActivity(Intent.createChooser(intent, chooserTitle));

        } else {
            Log.e(Constants.LOG_TAG_SERVICE, "No project folder for project:" + projectName);
            UIUtilities.showNotification(R.string.io_error);
        }
    }//end of onViewFiles

    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#getChildsOptionsMenu()
     */
    @Override
    protected int getChildsOptionsMenu() {
        return R.menu.infomenu;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        boolean flag = super.onCreateOptionsMenu(menu);

        // disable openstopo for older devices that will not render the data properly
        // and HONEYCOMB for the content access property ...
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            MenuItem opensTopoItem = menu.findItem(R.id.info_action_openstopo);
            if (opensTopoItem != null) {
                opensTopoItem.setVisible(true);
            }
//        }

        return flag;
    }

    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Constants.LOG_TAG_UI, "Info activity's menu selected - " + item.toString());

        switch (item.getItemId()) {
            case R.id.info_action_export_xls: {
                exportProjectXls();
                return true;
            }
            case R.id.info_action_export_vtopo: {
                exportProjectVTopo();
                return true;
            }

            case R.id.info_action_openstopo: {

                try {
                    // export
                    Log.i(Constants.LOG_TAG_SERVICE, "Start json export");
                    OpensTopoJsonExport export = new OpensTopoJsonExport(this);
                    String exportPath = export.runExport(getWorkspace().getActiveProject());
                    if (StringUtils.isEmpty(exportPath)) {
                        UIUtilities.showNotification(this, R.string.export_io_error, exportPath);
                    } else {
                        // load ui
                        Log.i(Constants.LOG_TAG_SERVICE, "exported to " + exportPath);
                        Intent intent = new Intent(InfoActivity.this, WebViewActivity.class);
                        intent.putExtra("path", exportPath);
                        intent.putExtra("projectName", getWorkspace().getActiveProject().getName());
                        startActivity(intent);
                    }

                    return true;

                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed to export project", e);
                    UIUtilities.showNotification(R.string.error);
                }


            }
            case R.id.info_action_view_files: {
                onViewFiles();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Button action method to update a project configuration
     *
     * @param viewArg - view
     */
    public void update(View viewArg){
        Log.v(Constants.LOG_TAG_UI, "Updating project");

        ProjectFragment projectFragment = (ProjectFragment)getSupportFragmentManager().findFragmentById(R.id.projectInfoContainer);
        final ProjectConfig projectConfig = projectFragment.getProjectConfig();

        // project name is not editable for the moment, so not checking for empty string
//        EditText projectNameField = (EditText) findViewById(R.id.new_projectname);
//        final String newProjectName = projectNameField.getText().toString();
//        if (newProjectName.trim().equals("")) {
//            projectNameField.setError(getString(R.string.project_name_required));
//            return;
//        }

        try {
            ProjectManager.instance().updateProject(projectConfig);

            finish();
            UIUtilities.showNotification(R.string.message_project_udpated);

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to update project", e);
            UIUtilities.showNotification(R.string.error);
        }

    }
}
