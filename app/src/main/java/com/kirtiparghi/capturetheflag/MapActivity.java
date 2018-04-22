package com.kirtiparghi.capturetheflag;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
 import java.util.ArrayList;
        import java.util.List;
        import java.util.Timer;
        import java.util.TimerTask;

        import android.location.Location;
        import android.os.Bundle;
        import android.support.v4.app.FragmentActivity;
        import android.widget.Toast;

        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.LatLngBounds;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;



/**
 * Simple Activity that display a Map containing some Markers,
 * this helps us illustrate how we can set the appropriate zoom level
 * so we can display the desired number of POI within a given radius
 *
 * @author Nabil HACHICHA
 * http://nhachicha.wordpress.com
 */
public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static float MAP_ZOOM_MAX = 3;
    private static float MAP_ZOOM_MIN = 21;

    private GoogleMap mMap;
    private SupportMapFragment mSupportFrag;
    private final static LatLng PARIS_LATLNG = new LatLng(48.858023, 2.294855);
    private List<Marker> mListMarkers = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mSupportFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mSupportFrag.getMapAsync(this);


    }

    /**
     * Add some POI, in our case a list of some monuments inside Paris :)
     */
    private void addMarkers() {
        MarkerOptions options = new MarkerOptions();

        mListMarkers.add(mMap.addMarker(options.position(new LatLng(48.858814,2.299018)).title("American Library").snippet("American Library in Paris")));
        mListMarkers.add(mMap.addMarker(options.position(new LatLng(48.855878,2.298074)).title("Champ de Mars").snippet("Champ de Mars 7th arr")));
        mListMarkers.add(mMap.addMarker(options.position(new LatLng(48.861807,2.288933)).title("Trocadéro").snippet("Jardins du Trocadéro")));
        mListMarkers.add(mMap.addMarker(options.position(new LatLng(48.866183,2.307816)).title("Champs-Elysées").snippet("Champs-Elysées 8th arr")));
        mListMarkers.add(mMap.addMarker(options.position(new LatLng(48.845457,2.304876)).title("UNESCO").snippet("UNESCO 15th arr")));
        mListMarkers.add(mMap.addMarker(options.position(new LatLng(48.851924,2.317472)).title("Conseil Régional").snippet("Conseil Régional IDF")));
    }

    /**
     * Finds and Set the Zoom level that allow us to see the given number of POI around a location within a given radius
     * @param loc Location from which we search nearby POI
     * @param radius The radius of our search in kilometer
     * @param nbPoi The number of POI we want to find
     */
    void setProperZoomLevel(LatLng loc, int radius, int nbPoi) {
        // [1] init zoom & move camera & result
        float currentZoomLevel = MAP_ZOOM_MAX;
        int currentFoundPoi = 0;
        LatLngBounds bounds = null;
        List<Marker> foundMarkers = new ArrayList<Marker>();
        Location location = latlngToLocation(loc);

        boolean keepZoomingOut = true;
        boolean keepSearchingForWithinRadius = true;// this is true if we keep looking
        // within a radius of 100km for ex:

        while (keepZoomingOut) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, currentZoomLevel--));
            bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            keepSearchingForWithinRadius = (Math.round(location.distanceTo(latlngToLocation(bounds.northeast)) / 1000) > radius) ? false : true;

            // [2] find out if we have POI (Markers)
            for (Marker k : mListMarkers) {
                if (bounds.contains(k.getPosition())) {
                    if (!foundMarkers.contains(k)) {
                        currentFoundPoi++;
                        foundMarkers.add(k);
                    }
                }
                // [3] we stop if we have nbPoi so far
                if (keepSearchingForWithinRadius) {
                    if (currentFoundPoi > nbPoi) {
                        keepZoomingOut = false;
                        break;

                    }// else keep looking

                } else if (currentFoundPoi > 0) {// [4] We are beyond radius if we found one POI we are good
                    keepZoomingOut = false;
                    break;

                } else if (currentZoomLevel < MAP_ZOOM_MIN) {// [5] keep looking but
                    // within MIN_ZOOM
                    // limit (we don't
                    // want to go outer
                    // space do we ? :)
                    keepZoomingOut = false;
                    break;
                }
                // [6] if we didn't found nbPoi keep zooming out (within the limit of radius)
            }
            keepZoomingOut = ((currentZoomLevel > 0) && keepZoomingOut) ? true : false;

        }
    }

    private Location latlngToLocation(LatLng dest) {
        Location loc = new Location("");
        loc.setLatitude(dest.latitude);
        loc.setLongitude(dest.longitude);
        return loc;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        MAP_ZOOM_MAX = mMap.getMaxZoomLevel();
        MAP_ZOOM_MIN = mMap.getMinZoomLevel();

        addMarkers();//added some dummy Markers

        //Delay the operation of finding the appropriate zoom level by giving the Maps the time to added the Marker and initialize itself
        TimerTask task = new TimerTask() {

            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        setProperZoomLevel(PARIS_LATLNG, 7, 1);
                    }
                });

            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 1000);// schedule Map display in 1 seconds

    }
}
