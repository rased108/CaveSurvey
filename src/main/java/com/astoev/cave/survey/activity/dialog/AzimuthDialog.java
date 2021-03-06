package com.astoev.cave.survey.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.R;

/**
 * Azimuth dialog shows a progress bar that waits 3 seconds and reads an azimuth. It notifies the parent
 * activity for the changed value if it implements AzimuthChangedListener.
 *
 * @author jmitrev
 */
public class AzimuthDialog extends BaseBuildInMeasureDialog {

    /**
     * @see DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // parent initialization
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.azimuth));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.azimuth_dialog, null);
        builder.setView(view);

        // add key listener to handle the back button
        builder.setOnKeyListener(new BackKeyListener(this));

        // progress bar view
        progressBar = view.findViewById(R.id.azimuth_progress);
        progressBar.setMax(progressMaxValue);

        initCameraPreview(view);

        TextView azimuthView = view.findViewById(R.id.azimuth_value);
        TextView accuracyView = view.findViewById(R.id.azimuth_accuracy);

        // create the Dialog
        AlertDialog alertDialg = builder.create();

        startAzimuthProcessor(azimuthView, accuracyView);

        return alertDialg;
    }

}
