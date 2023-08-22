package com.droidcon.geotracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.droidcon.geotracker.ui.theme.GeoTrackerTheme
import com.droidcon.geotracker.ui.theme.Purple40
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    LocationPreview()
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LocationPreview() {

        val latitude = remember {
            mutableStateOf("")
        }

        val longitude = remember {
            mutableStateOf("")
        }

        val address = remember {
            mutableStateOf("")
        }

        val mapButtonVisibility = remember {
            mutableStateOf(false)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        Scaffold(topBar = { AppBar() }) {
            it
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),

                verticalArrangement = Arrangement.Center,

                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                DisplayTextView(value = "Latitude : ", data = latitude)

                DisplaySpacer(height = 10)

                DisplayTextView(value = "Longitude : ", data = longitude)

                DisplaySpacer(height = 10)

                DisplayTextView(value = "", data = address)

                DisplaySpacer(height = 20)

                DisplayRequestLocationButton(latitude, longitude, address, mapButtonVisibility)

                DisplaySpacer(height = 20)

                if (mapButtonVisibility.value) {
                    DisplayShowOnMapButton(latitude, longitude)
                }
            }
        }
    }


    @Composable
    fun DisplayTextView(value: String, data: MutableState<String>) {
        Text(
            text = value + data.value,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
            fontSize = 15.sp,
            modifier = Modifier.padding(5.dp),
            textAlign = TextAlign.Center
        )
    }

    @Composable
    fun DisplaySpacer(height: Int) {
        Spacer(modifier = Modifier.height(height.dp))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppBar() {
        TopAppBar(
            title = {
                Text(
                    text = "Geo Tracker", color = Color.White, fontWeight = FontWeight.Bold
                )
            }, colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Purple40)
        )
    }

    @Composable
    fun DisplayShowOnMapButton(latitude: MutableState<String>, longitude: MutableState<String>) {
        Button(onClick = {
            val lat: Double = (latitude.value).toDouble()
            val long: Double = (longitude.value).toDouble()
            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:<lat>,<long>?q=${lat},${long}${"My current location "}")
                )
            startActivity(intent)

        }) {
            Text(
                text = "Display on Google Map",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                fontSize = 15.sp,
                modifier = Modifier.padding(5.dp)
            )
        }
    }

    @Composable
    fun DisplayRequestLocationButton(
        latitude: MutableState<String>,
        longitude: MutableState<String>,
        address: MutableState<String>,
        mapButtonVisibility: MutableState<Boolean>
    ) {

        val context = LocalContext.current
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val launcherMultiplePermissions = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap ->
            val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
            if (areGranted) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                mapButtonVisibility.value = true
                startLocationUpdates(this, latitude, longitude, address)
            } else {
                Toast.makeText(
                    context,
                    "Location permissions are required to use this application..",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }


        Button(onClick = {
            if (permissions.all {
                    ContextCompat.checkSelfPermission(
                        context, it
                    ) == PackageManager.PERMISSION_GRANTED
                }) {
                mapButtonVisibility.value = true
                startLocationUpdates(context, latitude, longitude, address)
            } else {
                launcherMultiplePermissions.launch(permissions)
            }

        }) {
            Text(
                text = "Request Current Location",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                fontSize = 15.sp,
                modifier = Modifier.padding(5.dp)
            )
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(
        context: Context,
        latitude: MutableState<String>,
        longitude: MutableState<String>,
        address: MutableState<String>
    ) {

        if (isLocationEnabled()) {
            fusedLocationClient?.lastLocation?.addOnCompleteListener {
                val location: Location? = it.result
                getAddressFromLocation(location, context, latitude, longitude, address)
            }

        } else {
            Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }


    private fun getAddressFromLocation(
        location: Location?,
        context: Context,
        latitude: MutableState<String>,
        longitude: MutableState<String>,
        address: MutableState<String>
    ) {
        if (location != null) {

            val geocoder = Geocoder(context, Locale.getDefault())
            val list: MutableList<Address>? =
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (list!!.isNotEmpty()) {

                latitude.value = list[0].latitude.toString()
                longitude.value = list[0].longitude.toString()
                address.value =
                    "Country : ${list[0].countryName} \nLocality : ${list[0].locality} \nAddress : ${
                        list[0].getAddressLine(
                            0
                        )
                    }"
            }
        } else {
            Toast.makeText(context, "Location not found..", Toast.LENGTH_SHORT).show()
        }
    }
}

