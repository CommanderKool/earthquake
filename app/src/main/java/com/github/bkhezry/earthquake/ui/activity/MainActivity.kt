package com.github.bkhezry.earthquake.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.github.bkhezry.earthquake.R
import com.github.bkhezry.earthquake.model.EarthquakeHourResponse
import com.github.bkhezry.earthquake.service.ApiService
import com.github.bkhezry.earthquake.util.ApiClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    @BindView(R.id.bar)
    lateinit var bar: BottomAppBar
    @BindView(R.id.coordinator_layout)
    lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var bottomDrawerBehavior: BottomSheetBehavior<View>
    private lateinit var mMap: GoogleMap
    private lateinit var apiService: ApiService
    private val disposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        setSupportActionBar(bar)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setupBottomDrawer()
        apiService = ApiClient.getClient()!!.create(ApiService::class.java)
    }

    private fun setupBottomDrawer() {
        val bottomDrawer = coordinatorLayout.findViewById<View>(R.id.bottom_drawer)
        bottomDrawerBehavior = BottomSheetBehavior.from(bottomDrawer)
        bottomDrawerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bar.setNavigationOnClickListener {
            bottomDrawerBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
        bar.setNavigationIcon(R.drawable.ic_menu_black_24dp)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        getHourlyEarthquake()
    }

    private fun getHourlyEarthquake() {
        val subscribe = apiService.getHourlyEarthquake()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleHourlyResponse, this::handleHourlyError)
        disposable.add(subscribe)

    }

    private fun handleHourlyResponse(earthquakeHourResponse: EarthquakeHourResponse) {
        Log.d("response:", earthquakeHourResponse.features.size.toString())
    }

    private fun handleHourlyError(error: Throwable) {
        Log.d("message:", error.localizedMessage)

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}
