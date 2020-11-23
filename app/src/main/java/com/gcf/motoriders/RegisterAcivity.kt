package com.gcf.motoriders

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONException
import org.json.JSONObject


class RegisterAcivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    private lateinit var lat:String
    private lateinit var long:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()
        val regButton: Button = findViewById(R.id.regButton)
        auth = FirebaseAuth.getInstance()
        if (SharedPrefManager.getInstance(this).isLoggedIn) {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
            return
        }
        lat = ""
        long = " "
        mFusedLocationClient = LocationServices
                .getFusedLocationProviderClient(this)
        getLastLocation()
        regButton.setOnClickListener { //if user pressed on button register
            registerUser()
        }
    }
    private fun registerUser() {
        val isEmail = intent.extras?.getBoolean("isEmail")
        val data = intent.extras?.getString("data")
        val nameField: EditText = findViewById(R.id.regName)
        val phoneField: EditText = findViewById(R.id.regPhone)
        val emailField: EditText = findViewById(R.id.regEmail)
        val radioGroupGender: RadioGroup = findViewById(R.id.radioGender)
        val progressBar:ProgressBar = findViewById(R.id.progressBar)
        val radioBut: RadioButton  =  findViewById(radioGroupGender.checkedRadioButtonId)
        val gender:String = radioBut.text.toString()
        if (isEmail != true) {
            emailField.visibility = View.GONE
            phoneField.visibility = View.VISIBLE
        }
        class RegisterUser : AsyncTask<Void, Void, String>() {
             override fun doInBackground(vararg params: Void): String? {
                val requestHandler = RequestHandler()
                val params: HashMap<String, String> = HashMap()
                 if(isEmail==true) {
                     params["username"] = nameField.text.toString()
                     params["email"] = data.toString()
                     params["user_type"] = "2"
                     params["token"] = auth.currentUser?.uid.toString()
                     params["latitude"] = lat
                     params["longitude"] = long
                     params["user_id"] = auth.currentUser?.uid.toString()
                     params["status"] = "true"
                     params["phone"] = phoneField.text.toString()
                     params["gender"] = gender
                     return requestHandler.sendPostRequest(URLs.URL_REGISTER, params,"POST")
                 }else{
                     params["username"] = nameField.text.toString()
                     params["email"] = emailField.text.toString()
                     params["phone"] = data.toString()
                     params["user_type"] = "2"
                     params["token"] = auth.currentUser?.uid.toString()
                     params["latitude"] = lat
                     params["longitude"] = long
                     params["user_id"] = auth.currentUser?.uid.toString()
                     params["status"] = "true"
                     params["gender"] = gender
                     return requestHandler.sendPostRequest(URLs.URL_REGISTER, params,"POST")
                 }
            }

            override fun onPreExecute() {
                super.onPreExecute()
                progressBar.visibility = View.VISIBLE;

            }

            override fun onPostExecute(s: String) {
                super.onPostExecute(s)
                progressBar.setVisibility(View.GONE);
                try {
                    //converting response to json object
                    val obj = JSONObject(s)

                    //if no error in response
                    if (obj.getBoolean("status")) {
                        Toast.makeText(this@RegisterAcivity, obj.getString("message"), Toast.LENGTH_SHORT).show()
                        //getting the user from the response
                        val userJson = obj.getJSONObject("details")
                        //creating a new user object
                        val user = User(
                                userJson.getString("phone"),
                                userJson.getString("username"),
                                userJson.getString("email"),
                                userJson.getString("gender")
                        )
                        //storing the user in shared preferences
                        SharedPrefManager.getInstance(this@RegisterAcivity).userLogin(user)
                        finish()
                        startActivity(Intent(this@RegisterAcivity, MainActivity::class.java))
                    } else {
                        auth.signOut()
                        Toast.makeText(this@RegisterAcivity, obj.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    auth.signOut()
                    e.printStackTrace()
                }
            }
        }

        //executing the async task

        //executing the async task
        val ru = RegisterUser()
        ru.execute()
    }
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient
                        .lastLocation
                        .addOnCompleteListener { task ->
                            val location: Location? = task.result
                            if (location == null) {
                                requestNewLocationData()
                            } else {
                                lat = location.latitude.toString()
                                long = location.longitude.toString()
                            }
                        }
            } else {
                Toast
                        .makeText(
                                this, "Please turn on"
                                + " your location...",
                                Toast.LENGTH_LONG)
                        .show()
                val intent = Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,Looper.myLooper())
    }
    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(
                locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
           lat = mLastLocation.latitude.toString()
           long = mLastLocation.longitude.toString()

        }
    }

    // method to check for permissions
    private fun checkPermissions(): Boolean {
        return (ActivityCompat
                .checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat
                .checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        /* ActivityCompat
                .checkSelfPermission(
                    this,
                    Manifest.permission
                        .ACCESS_BACKGROUND_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        */
    }

    // method to requestfor permissions
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
                this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
                44)
    }

    // method to check
    // if location is enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(
                Context.LOCATION_SERVICE) as LocationManager
        return (locationManager
                .isProviderEnabled(
                        LocationManager.GPS_PROVIDER)
                || locationManager
                .isProviderEnabled(
                        LocationManager.NETWORK_PROVIDER))
    }

    // If everything is alright then
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray) {
        super
                .onRequestPermissionsResult(
                        requestCode,
                        permissions,
                        grantResults)
        if (requestCode == 44) {
            if ((grantResults.isNotEmpty()
                            && grantResults[0]
                            == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            getLastLocation()
        }
    }
}

