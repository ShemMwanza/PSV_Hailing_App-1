package com.georgina.psvhailingapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PassengerMapsFragment extends Fragment {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    FusedLocationProviderClient client;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    //private String driver_id;
    private TextView mMatatuPlate;
    private Button mSearch;
    private TextInputLayout mFrom;
    private TextInputLayout mWhere;
    public static String EXTRA_SOURCE = "source";
    public static String EXTRA_DEST = "dest";

    //    private TextView mDriverNumber;
//    private TextView mStart;
//    private TextView mDestination;
//    private TextView mDriverName;
    private ImageView mHeaderArrow;
    private ConstraintLayout mRoutesBottomSheet;
    private LinearLayout mHeaderLayout;
    private LinearLayout mInputLayout;
    private BottomSheetBehavior mBottomSheetBehavior;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference driverDatabaseReference;
    private DatabaseReference user_driverDatabaseReference;
    private RecyclerView routesRecyclerView;
    private DriverRouteAdapter adapter;
    private ArrayList<DriverDetails> list;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger_maps, container, false);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mMatatuPlate = view.findViewById(R.id.no_plate);
//        mDriverNumber = view.findViewById(R.id.driver_number);
//        mStart = view.findViewById(R.id.Start);
//        mDestination = view.findViewById(R.id.destination);
//        mDriverName = view.findViewById(R.id.driver_name);
        mRoutesBottomSheet = view.findViewById(R.id.available_routes_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mRoutesBottomSheet);
        mBottomSheetBehavior.setHideable(false);
        mHeaderLayout = view.findViewById(R.id.header_layout);
        mHeaderArrow = view.findViewById(R.id.arrow);
        mInputLayout = view.findViewById(R.id.input_location);

        mSearch = view.findViewById(R.id.btn_search);
        mFrom = view.findViewById(R.id.from);
        mWhere = view.findViewById(R.id.where_to);

        //driver_id = "lWzaj102lsZEupT5WERAQS3GmUB2";
        firebaseDatabase = FirebaseDatabase.getInstance();
        driverDatabaseReference = firebaseDatabase.getReference("Drivers");
        routesRecyclerView = view.findViewById(R.id.recycler_routes);
        routesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list = new ArrayList<>();
        adapter = new DriverRouteAdapter(list,getContext());
        routesRecyclerView.setAdapter(adapter);
        initializeRouteData();
        mSearch.setVisibility(View.GONE);
        mFrom.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v.isFocused()){
                    mSearch.setVisibility(View.VISIBLE);
                }
            }
        });
        mWhere.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v.isFocused()){
                    mSearch.setVisibility(View.VISIBLE);
                }
            }
        });

//        driverDatabaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                    DriverDetails driverDetails = dataSnapshot.getValue(DriverDetails.class);
//                    list.add(driverDetails);
//                }
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//
//            }
//        });

//        getAvailableRoutes();
        //driverDatabaseReference = firebaseDatabase.getReference("Users").child("Driver").child(driver_id);
        //user_driverDatabaseReference = firebaseDatabase.getReference("Users").child(driver_id);
        //getAvailableRoutes();
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        //Toast toast = Toast.makeText(getContext(),driver_id,Toast.LENGTH_SHORT);
        //toast.show();
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
            mBottomSheetBehavior.setHideable(false);

        }
        client = LocationServices.getFusedLocationProviderClient(getContext());
        getCurrentLocation();

        mHeaderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    mInputLayout.setVisibility(View.GONE);
                }
                else {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    mInputLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull @NotNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull @NotNull View bottomSheet, float slideOffset) {
                mHeaderArrow.setRotation(slideOffset * 180);
            }
        });
        return view;
    }

    private void initializeRouteData() {
        list.clear();
        list.add(new DriverDetails("DL-1234567","KBC 778C","Madaraka",4,"active"));
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Small Change", Toast.LENGTH_SHORT);
    }

    @Override
    public void onStart(){
        super.onStart();
        if (mCurrentUser == null){
            Intent intent = new Intent(getContext(),LoginActivity.class);
            startActivity(intent);
        }
    }
//    public void onStop() {
//
//        super.onStop();
//    }
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        } else {
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location != null) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
                            mBottomSheetBehavior.setHideable(false);
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                                    .title("Your Location");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                            googleMap.addMarker(markerOptions);

                            mSearch.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String source = mFrom.getEditText().getText().toString();
                                    String destination = mWhere.getEditText().getText().toString();
                                    Geocoder geocoder = new Geocoder(getContext());

                                    if (!validateSource() || !validateDestination()) {
                                        return;
                                    }
                                    else {
                                        List<Address> addressList = null;
                                        try {
                                            addressList = geocoder.getFromLocationName(source, 1);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        assert addressList != null;
                                        Address sourceAddress = addressList.get(0);
                                        LatLng sLatlng = new LatLng(sourceAddress.getLatitude(), sourceAddress.getLongitude());
                                        MarkerOptions markerOptions1 = new MarkerOptions().position(sLatlng)
                                                .title(source);
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sLatlng, 20));
                                        mMap.addMarker(markerOptions1);

                                        List<Address> addressList1 = null;
                                        try {
                                            addressList1 = geocoder.getFromLocationName(destination, 1);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        assert addressList1 != null;
                                        Address destAddress = addressList1.get(0);
                                        LatLng dLatlng = new LatLng(destAddress.getLatitude(), destAddress.getLongitude());
                                        MarkerOptions markerOptions2 = new MarkerOptions().position(dLatlng)
                                                .title(destination);
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dLatlng, 20));
                                        mMap.addMarker(markerOptions2);

                                        Intent bookingIntent = new Intent(getContext(),SelectTimeandDateActivity.class);
                                        bookingIntent.putExtra(EXTRA_SOURCE,source);
                                        bookingIntent.putExtra(EXTRA_DEST,destination);
                                        startActivity(bookingIntent);
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }
    private boolean validateSource(){
        String routes = mFrom.getEditText().getText().toString().trim();
        if(routes.isEmpty()){
            mFrom.setError(getString(R.string.empty_field));
            return false;
        }
        else {
            mFrom.setError(null);
            return true;
        }

    }
    private boolean validateDestination(){
        String routes =  mWhere.getEditText().getText().toString().trim();
        if(routes.isEmpty()){
            mWhere.setError(getString(R.string.empty_field));
            return false;
        }
        else {
            mWhere.setError(null);
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 44){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }
//    private void getAvailableRoutes(){
//       driverDatabaseReference.addValueEventListener(new ValueEventListener() {
//           @Override
//           public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//               for(DataSnapshot dataSnapshot : snapshot.getChildren()){
//                   DriverDetails driverDetails = dataSnapshot.getValue(DriverDetails.class);
//                    list.add(driverDetails);
//               }
//               adapter = new DriverRouteAdapter(getContext(),list);
//               recyclerView.setAdapter(adapter);
//               adapter.notifyDataSetChanged();
//           }
//
//           @Override
//           public void onCancelled(@NonNull @NotNull DatabaseError error) {
//
//           }
//       });
//    }
}