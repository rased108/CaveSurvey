package com.astoev.cave.survey.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.model.Location;
import com.astoev.cave.survey.util.LocationUtil;

/**
 * Fragment that shows information for particular Location. It expects the the Location to be set as argument
 * with key LOCATION.
 * 
 * @author jmitrev
 */
public class LocationFragment extends Fragment {

    /** Key where the Location is expected */
    public static final String LOCATION_KEY = "LOCATION";
    
    protected TextView latitudeView;
    protected TextView longitudeView;
    protected TextView altitudeView;
    protected TextView accuracyView;

    /**
     * @see Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflaterArg,
            ViewGroup containerArg, Bundle savedInstanceStateArg) {

        View view = inflaterArg.inflate(R.layout.location_fragment, containerArg, false);
        
        latitudeView  = view.findViewById(R.id.gps_latitude);
        longitudeView = view.findViewById(R.id.gps_longitude);
        altitudeView  = view.findViewById(R.id.gps_altitude);
        accuracyView  = view.findViewById(R.id.gps_accuracy);
        
        Bundle arguments = getArguments();
        if (arguments != null && arguments.get(LOCATION_KEY) != null){
            Location location = (Location)arguments.get(LOCATION_KEY);
            
            String latitude = LocationUtil.formatLatitude(location.getLatitude());
            latitudeView.setText(latitude);
            
            String longitude = LocationUtil.formatLongitude(location.getLongitude());
            longitudeView.setText(longitude);
            
            // altitude
            altitudeView.setText(String.valueOf(location.getAltitude()));
            
            // accuracy
            accuracyView.setText(String.valueOf(location.getAccuracy()));
        }
        
        return view;
    }
    
}
