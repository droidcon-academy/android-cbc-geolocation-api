package com.droidcon.geotracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
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

    /*
        Creating a variable for Fused Location Provider Client to get user location updates.
     */
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

    /*
    Creating a Location Preview method to display the UI of the application.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LocationPreview() {

        /*
        Creating the variables for latitude, longitude, address as well as boolean variable to hide and show display location on maps button.
         */
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

        /*
        initializing the variable for fused location provider client.
         */
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        /*
        Using Scaffold widget displaying the UI for the application.
         */
        Scaffold(topBar = { AppBar() }) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(padding),

                verticalArrangement = Arrangement.Center,

                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                /*
                    Inside this scaffold widget
                    1) Calling display text view method to display all the text's within our application.
                    2) Calling display spacer method to display the spacer between two text views.
                    3) Calling DisplayRequestLocationButton to display the request location button.
                    4) Calling DisplayShowOnMapButton to display show user location on google maps button.
                 */
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


    /*
    Creating a composable function to display the text views.
     */
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

    /*
    Creating a composable function to display spacer.
     */
    @Composable
    fun DisplaySpacer(height: Int) {
        Spacer(modifier = Modifier.height(height.dp))
    }

    /*
    Creating a composable function to display app bar.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppBar() {
        TopAppBar(
            title = {
                Text(
                    text = "Geo Tracker", color = Color.White, fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = Purple40
            )
        )
    }

    /*
    Creating a composable function to create a button to show user location on Google Maps.
     */
    @Composable
    fun DisplayShowOnMapButton(latitude: MutableState<String>, longitude: MutableState<String>) {
        Button(onClick = {
            val lat: Double = (latitude.value).toDouble()
            val long: Double = (longitude.value).toDouble()
            // TODO : Creating and initializing the intent to open the user's location in Google Maps and display the marker on that location. 

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

    /*
    Creating a composable function to display request location button.
     */
    @Composable
    fun DisplayRequestLocationButton(
        latitude: MutableState<String>,
        longitude: MutableState<String>,
        address: MutableState<String>,
        mapButtonVisibility: MutableState<Boolean>
    ) {
        
       //TODO : Checking and Requesting runtime permissions for getting user location        
        
        Button(onClick = {
          // TODO : Check and request runtime permission for user location inside on click method. 
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

    /*
    Creating a function to check weather the user's device GPS is enabled or not to get location.
     */
    private fun isLocationEnabled(): Boolean {
        //TODO : Write code in video to check location provider is enabled or not.
        return false
    }

    /*
    Creating a method to get user location's latitude and longitude.
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(
        context: Context,
        latitude: MutableState<String>,
        longitude: MutableState<String>,
        address: MutableState<String>
    ) {
        // TODO : Write code in video which is will use Fused Location Provider to get user location
    }

    /*
    Creating a method to get address from user location.
     */
    private fun getAddressFromLocation(
        location: Location?,
        context: Context,
        latitude: MutableState<String>,
        longitude: MutableState<String>,
        address: MutableState<String>
    ) {
        //TODO : Write code in video to get address from user location and set the data to our variables to display in the UI.
    }
}




