package tesla.hrishi3331studio.vnit.teslasmartcar;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.milestone.Milestone;
import com.mapbox.services.android.navigation.v5.milestone.MilestoneEventListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import java.util.List;

public class Map extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener, MapboxMap.OnMapClickListener {

    private MapView mapView;
    private LocationLayerPlugin locationLayerPlugin;
    private LocationEngine engine;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private Location origin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Mapbox.getInstance(Map.this, getString(R.string.access_token));

        mapView = (MapView)findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        MilestoneEventListener listener = new MilestoneEventListener() {
            @Override
            public void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone) {

            }
        };
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setOnMapClickListener(this);
        FirebaseDatabase.getInstance().getReference().child("Dashboard").child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null){
                    float latitude = Float.valueOf(dataSnapshot.child("latitude").getValue().toString());
                    float longitude = Float.valueOf(dataSnapshot.child("longitude").getValue().toString());
                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),16));
                    mapboxMap.addMarker(new MarkerOptions().setPosition(new LatLng(latitude, longitude)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        startLocation();
    }

    @SuppressLint("MissingPermission")
    public void startLocation(){
        if (PermissionsManager.areLocationPermissionsGranted(Map.this)){
            engine = new LocationEngineProvider(Map.this).obtainBestLocationEngineAvailable();
            engine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
            engine.activate();

            Location last_location = engine.getLastLocation();
            if (last_location != null){
                origin = last_location;
                setCameraPosition(origin);
            }else {
                engine.addLocationEngineListener(this);
            }

            locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap, engine);
            locationLayerPlugin.setLocationLayerEnabled(true);
            locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
            locationLayerPlugin.setRenderMode(RenderMode.NORMAL);

        }
        else {
            permissionsManager = new PermissionsManager(Map.this);
            permissionsManager.requestLocationPermissions(Map.this);
        }
    }

    public void setCameraPosition(Location location){

        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(),16));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected() {
        engine.requestLocationUpdates();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            origin = location;
            setCameraPosition(location);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted){
            startLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (engine != null){
            engine.deactivate();
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {

    }
}
