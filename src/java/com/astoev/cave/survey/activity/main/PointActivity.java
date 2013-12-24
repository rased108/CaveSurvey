package com.astoev.cave.survey.activity.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.draw.DrawingActivity;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.service.bluetooth.BluetoothService;
import com.astoev.cave.survey.util.PointUtil;
import com.astoev.cave.survey.util.StringUtils;
import com.j256.ormlite.misc.TransactionManager;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/17/12
 * Time: 1:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class PointActivity extends MainMenuActivity {

    private Integer mCurrLeg = null;
    private String mNewNote = null;
    private ResultReceiver receiver = new ResultReceiver(new Handler()) {

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            float aMeasure = resultData.getFloat("result");
            Constants.Measures type = Constants.Measures.valueOf(resultData.getString("type"));
            switch (type) {
                case distance:
                    Log.i(Constants.LOG_TAG_UI, "Got distance " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_distance);
                    break;

                case angle:
                    Log.i(Constants.LOG_TAG_UI, "Got angle " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_azimuth);
                    break;

                case slope:
                    Log.i(Constants.LOG_TAG_UI, "Got slope " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_slope);
                    break;

                case up:
                    Log.i(Constants.LOG_TAG_UI, "Got up " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_up);
                    break;

                case down:
                    Log.i(Constants.LOG_TAG_UI, "Got down " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_down);
                    break;

                case left:
                    Log.i(Constants.LOG_TAG_UI, "Got left " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_left);
                    break;

                case right:
                    Log.i(Constants.LOG_TAG_UI, "Got right " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_right);
                    break;

                default:
                    Log.i(Constants.LOG_TAG_UI, "Ignore type " + type);
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.point);
        mNewNote = null;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadPointData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPointData();
    }

    private void loadPointData() {
        Log.i(Constants.LOG_TAG_UI, "Initialize point view");

        try {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mCurrLeg = extras.getInt(Constants.LEG_SELECTED);
                Leg legEdited = (Leg) mWorkspace.getDBHelper().getLegDao().queryForId(mCurrLeg);

                // label
                TextView leg = (TextView) findViewById(R.id.point_curr_leg);
                leg.setText(legEdited.buildLegDescription(this));

                // up
                EditText up = (EditText) findViewById(R.id.point_up);
                setNotNull(up, legEdited.getTop());
                addOnClickListener(up, Constants.Measures.up);

                // down
                EditText down = (EditText) findViewById(R.id.point_down);
                setNotNull(down, legEdited.getDown());
                addOnClickListener(down, Constants.Measures.down);

                // left
                EditText left = (EditText) findViewById(R.id.point_left);
                setNotNull(left, legEdited.getLeft());
                addOnClickListener(left, Constants.Measures.left);

                // right
                EditText right = (EditText) findViewById(R.id.point_right);
                setNotNull(right, legEdited.getRight());
                addOnClickListener(right, Constants.Measures.right);

                // distance
                EditText distance = (EditText) findViewById(R.id.point_distance);
                setNotNull(distance, legEdited.getDistance());
                addOnClickListener(distance, Constants.Measures.distance);

                // azimuth
                EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
                setNotNull(azimuth, legEdited.getAzimuth());
                addOnClickListener(azimuth, Constants.Measures.angle);

                // slope
                EditText slope = (EditText) findViewById(R.id.point_slope);
                slope.setText("0");
                setNotNull(slope, legEdited.getSlope());
                addOnClickListener(slope, Constants.Measures.slope);

                // fill note_text with its value
                Note note = Leg.getActiveLegNote(legEdited, mWorkspace);
                TextView textView = (TextView) findViewById(R.id.point_note_text);
                if (note != null && note.getText() != null) {
                    textView.setText(note.getText());
                    textView.setClickable(true);
                } else if (mNewNote != null) {
                    textView.setText(mNewNote);
                    textView.setClickable(true);
                }

                // enable deletion for last leg
//                MenuItem deleteMenuOption = (MenuItem) findViewById(R.id.point_action_delete);
//                if (legEdited.getId().equals(mWorkspace.getLastLeg())) {
//                    deleteMenuOption.setEnabled(true);
//                } else {
//                    deleteMenuOption.setEnabled(false);
//                }
            } else {
                Log.i(Constants.LOG_TAG_UI, "PointView for new point");
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to render point", e);
            UIUtilities.showNotification(this, R.string.error);
        }
    }

    private void addOnClickListener(EditText text, final Constants.Measures aMeasure) {

        if (BluetoothService.isBluetoothSupported()) {

            if (!ensureDeviceSelected(false)) {
                return;
            }

            Log.i(Constants.LOG_TAG_UI, "Register field? " + aMeasure);
            switch (aMeasure) {
                case distance:
                case up:
                case down:
                case left:
                case right:
                    if (!Option.CODE_SENSOR_BLUETOOTH.equals(Options.getOptionValue(Option.CODE_DISTANCE_SENSOR))) {
                        return;
                    }
                    break;

                case angle:
                    if (!Option.CODE_SENSOR_BLUETOOTH.equals(Options.getOptionValue(Option.CODE_AZIMUTH_SENSOR))) {
                        return;
                    }
                    break;

                case slope:
                    if (!Option.CODE_SENSOR_BLUETOOTH.equals(Options.getOptionValue(Option.CODE_SLOPE_SENSOR))) {
                        return;
                    }
                    break;
            }
        }

        Log.i(Constants.LOG_TAG_UI, "Add BT listener");
        // supported for the measure, add the listener
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(Constants.LOG_TAG_UI, "Send command");
                triggerBluetoothMeasure(aMeasure);
            }
        });
    }

    private void setNotNull(EditText aEditText, Float aValue) {
        if (aValue != null) {
            aEditText.setText(StringUtils.floatToLabel(aValue));
        } else {
            aEditText.setText("");
        }
    }

    private boolean saveLeg() {
        try {

            // start validation
            boolean valid = true;
            final EditText distance = (EditText) findViewById(R.id.point_distance);
            valid = valid && validateNumber(distance, true);

            final EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
            valid = valid && validateNumber(azimuth, true) && checkAzimuth(azimuth);

            final EditText slope = (EditText) findViewById(R.id.point_slope);
            valid = valid && validateNumber(slope, false) && checkSlope(slope);

            final EditText up = (EditText) findViewById(R.id.point_up);
            valid = valid && validateNumber(up, false);

            final EditText down = (EditText) findViewById(R.id.point_down);
            valid = valid && validateNumber(down, false);

            final EditText left = (EditText) findViewById(R.id.point_left);
            valid = valid && validateNumber(left, false);

            final EditText right = (EditText) findViewById(R.id.point_right);
            valid = valid && validateNumber(right, false);

            if (!valid) {
                return false;
            }

            Log.i(Constants.LOG_TAG_UI, "Saving leg");

            TransactionManager.callInTransaction(mWorkspace.getDBHelper().getConnectionSource(),
                    new Callable() {
                        public Integer call() throws Exception {

                            if (mCurrLeg == null) {
                                Log.i(Constants.LOG_TAG_UI, "Create new leg");
                                Leg activeLeg = (Leg) mWorkspace.getDBHelper().getLegDao().queryForId(mWorkspace.getActiveLegId());

                                // another leg, starting from the latest in the gallery
                                Point newFrom = mWorkspace.getLastGalleryPoint(activeLeg.getGalleryId());
                                Point newTo = PointUtil.generateNextPoint(activeLeg.getGalleryId());
                                mWorkspace.getDBHelper().getPointDao().create(newTo);

                                Leg nextLeg = new Leg(newFrom, newTo, mWorkspace.getActiveProject(), activeLeg.getGalleryId());

                                mWorkspace.getDBHelper().getLegDao().create(nextLeg);
                                mCurrLeg = nextLeg.getId();
                            }

                            Leg legEdited = (Leg) mWorkspace.getDBHelper().getLegDao().queryForId(mCurrLeg);

                            // update model
                            legEdited.setDistance(StringUtils.getFromEditTextNotNull(distance));
                            legEdited.setAzimuth(StringUtils.getFromEditTextNotNull(azimuth));
                            legEdited.setSlope(StringUtils.getFromEditTextNotNull(slope));
                            legEdited.setTop(StringUtils.getFromEditTextNotNull(up));
                            legEdited.setDown(StringUtils.getFromEditTextNotNull(down));
                            legEdited.setLeft(StringUtils.getFromEditTextNotNull(left));
                            legEdited.setRight(StringUtils.getFromEditTextNotNull(right));

                            // save leg
                            mWorkspace.getDBHelper().getLegDao().update(legEdited);

                            if (mNewNote != null) {
                                // create new note
                                Note note = new Note(mNewNote);
                                note.setPoint(legEdited.getFromPoint());
                                mWorkspace.getDBHelper().getNoteDao().create(note);
                            }

                            mWorkspace.setActiveLegId(legEdited.getId());

                            Log.i(Constants.LOG_TAG_UI, "Saved");
                            return 0;
                        }
                    });
            return true;
        } catch (Exception e) {
            UIUtilities.showNotification(this, R.string.error);
            Log.e(Constants.LOG_TAG_UI, "Leg not saved", e);
        }
        return false;
    }

    private boolean validateNumber(EditText aEditField, boolean isRequired) {
        if (StringUtils.isEmpty(aEditField)) {
            if (isRequired) {
                aEditField.setError(aEditField.getContext().getString(R.string.required));
                return false;
            }
            return true;
        } else {
            try {
                Float.parseFloat(aEditField.getText().toString().trim());
                return true;
            } catch (NumberFormatException nfe) {
                aEditField.setError(aEditField.getContext().getString(R.string.invalid));
                return false;
            }
        }
    }

    private boolean checkAzimuth(EditText aEditText) {
        Float azimuth = StringUtils.getFromEditTextNotNull(aEditText);
        if (null != azimuth) {
            if (azimuth.floatValue() < 0) {
                aEditText.setError(aEditText.getContext().getString(R.string.invalid));
                return false;
            }

            String currAzimuthMeasure = Options.getOptionValue(Option.CODE_AZIMUTH_UNITS);
            int maxValue;
            if (Option.UNIT_DEGREES.equals(currAzimuthMeasure)) {
                maxValue = Option.MAX_VALUE_AZIMUTH_DEGREES;
            } else { // Option.UNIT_GRADS
                maxValue = Option.MAX_VALUE_AZIMUTH_GRADS;
            }
            if (azimuth.floatValue() > maxValue) {
                aEditText.setError(aEditText.getContext().getString(R.string.invalid));
                return false;
            }
        }

        return true;
    }

    private boolean checkSlope(EditText aEditText) {
        Float slope = StringUtils.getFromEditTextNotNull(aEditText);
        if (null != slope) {
            String currSlopeMeasure = Options.getOptionValue(Option.CODE_SLOPE_UNITS);
            int maxValue, minValue;
            if (Option.UNIT_DEGREES.equals(currSlopeMeasure)) {
                maxValue = Option.MAX_VALUE_SLOPE_DEGREES;
                minValue = Option.MIN_VALUE_SLOPE_DEGREES;
            } else { // Option.UNIT_GRADS
                maxValue = Option.MAX_VALUE_SLOPE_GRADS;
                minValue = Option.MIN_VALUE_SLOPE_GRADS;
            }
            if (slope.floatValue() > maxValue || slope.floatValue() < minValue) {
                aEditText.setError(aEditText.getContext().getString(R.string.invalid));
                return false;
            }
        }
        return true;
    }

    public void noteButton(View aView) {
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra(Constants.LEG_SELECTED, mCurrLeg);
        intent.putExtra(Constants.LEG_NOTE, mNewNote);
        startActivityForResult(intent, 2);
    }

    public void saveButton() {
        if (saveLeg()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void drawingButton() {
        Intent intent = new Intent(this, DrawingActivity.class);
        startActivity(intent);
    }

    public void coordinateButton() {
        // TODO location http://www.tutorialforandroid.com/2009/05/permissions-journey-accesscoarselocatio.html
        UIUtilities.showNotification(this, R.string.todo);
    }

    public void deleteButton() {
        try {
            TransactionManager.callInTransaction(mWorkspace.getDBHelper().getConnectionSource(),
                    new Callable() {
                        public Object call() throws Exception {
                            Log.i(Constants.LOG_TAG_UI, "Delete " + mWorkspace.getActiveLegId());

                            Leg legEdited = (Leg) mWorkspace.getDBHelper().getLegDao().queryForId(mCurrLeg);

                            Note note = Leg.getActiveLegNote(legEdited, mWorkspace);
                            if (note != null) {
                                mWorkspace.getDBHelper().getNoteDao().delete(note);
                            }

                            mWorkspace.getDBHelper().getLegDao().delete(legEdited);
                            mWorkspace.getDBHelper().getPointDao().delete(legEdited.getToPoint());

                            mWorkspace.setActiveLegId(mWorkspace.getActiveOrFirstLeg().getId());

                            return null;
                        }
                    });
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to delete point", e);
            UIUtilities.showNotification(this, R.string.error);
        }
    }

    private void triggerBluetoothMeasure(Constants.Measures aMeasure) {
        // register listeners & send command
        BluetoothService.sendReadDistanceCommand(receiver, aMeasure);
        Log.i(Constants.LOG_TAG_UI, "Command sent for " + aMeasure);
    }

    private void populateMeasure(float aMeasure, int anEditTextId) {
        EditText up = (EditText) findViewById(anEditTextId);
        setNotNull(up, aMeasure);
    }

    private boolean ensureDeviceSelected(boolean showBTOptions) {
        if (BluetoothService.isDeviceSelected()) {
            return true;
        }

        if (showBTOptions) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setMessage(R.string.bt_not_selected)
                    .setCancelable(false)
                    .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(PointActivity.this, BTActivity.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = dialogBuilder.create();
            alert.show();
        }
        return false;
    }

    public void readAzimuth(View view) {
        Intent intent = new Intent(this, ReadAzimuthActivity.class);
        startActivityForResult(intent, 1);
        EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
        azimuth.setText(intent.getStringExtra("Azimuth"));

    }

    public void readSlope(View view) {
        // TODO
        UIUtilities.showNotification(this, R.string.todo);
    }

    public void photoButton() {
        // picture http://www.tutorialforandroid.com/2010/10/take-picture-in-android-with.html

        final File path = new File(Environment.getExternalStorageDirectory(), "CaveSurvey");
        if (!path.exists()) {
            path.mkdir();
        }

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(path, "photo.tmp")));
        startActivityForResult(intent, 1);

    }

    // photo is captured
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent aData) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Log.i(Constants.LOG_TAG_SERVICE, "Got image");
                    final File file = new File(new File(Environment.getExternalStorageDirectory(), "CaveSurvey"), "photo.tmp");
                    FileInputStream in = null;
                    try {
                        in = new FileInputStream(file);
                        Photo photo = new Photo();
                        photo.setPictureBytes(IOUtils.toByteArray(in));

                        Leg legEdited = (Leg) mWorkspace.getDBHelper().getLegDao().queryForId(mCurrLeg);
                        Point currPoint = (Point) mWorkspace.getDBHelper().getPointDao().queryForId(legEdited.getFromPoint().getId());
                        photo.setPoint(currPoint);

                        mWorkspace.getDBHelper().getPhotoDao().create(photo);

                        Log.i(Constants.LOG_TAG_SERVICE, "Image stored");
                    } catch (Exception e) {
                        Log.e(Constants.LOG_TAG_UI, "Picture not saved", e);
                        UIUtilities.showNotification(this, R.string.error);
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                    break;
                case 2:
                    mNewNote = aData.getStringExtra("note");
                    TextView textView = (TextView) findViewById(R.id.point_note_text);
                    textView.setText(mNewNote);
                    textView.setClickable(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#getChildsOptionsMenu()
     */
    @Override
    protected int getChildsOptionsMenu() {
        return R.menu.pointmenu;
    }

    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Constants.LOG_TAG_UI, "Point activity's menu selected - " + item.toString());

        switch (item.getItemId()) {
            case R.id.point_action_save: {
                saveButton();
                return true;
            }
            case R.id.point_action_note: {
                noteButton(null);
                return true;
            }
            case R.id.point_action_draw: {
                drawingButton();
                return true;
            }
            case R.id.point_action_gps: {
                coordinateButton();
                return true;
            }
            case R.id.point_action_photo: {
                photoButton();
                return true;
            }
            case R.id.point_action_delete: {
                deleteButton();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
