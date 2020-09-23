package com.example.ejemplo_geolocaactualapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var mensaje1 : TextView
    private lateinit var mensaje2 : TextView

    private var REQUEST_LOCATION = 1002

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mensaje1 = findViewById(R.id.mensaje1_id)
        mensaje2 = findViewById(R.id.mensaje2_id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
            ) {
                //pedir permiso al usuario
                val permisosLocation = arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
                requestPermissions(permisosLocation, REQUEST_LOCATION)
                Log.e("FINE_LOCATION: ", (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED).toString())
                Log.e("COARSE_LOCATION: ", (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED).toString())
            } else {
                locationStart()
            }
        }
    }

    private fun locationStart() {
        val mlocManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val Local = Localizacion()
        Local.mainActivity = this
        val gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsEnabled) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            return
        }

        mlocManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0,
            0f,
            (Local as LocationListener)
        )

        mlocManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            (Local as LocationListener)
        )

        mensaje1.text = "Localización agregada"
        mensaje2.text = ""
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart()
                return
            }
        }
    }

    fun setLocation(loc: Location) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() !== 0.0 && loc.getLongitude() !== 0.0) {
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val list: List<Address> = geocoder.getFromLocation(
                    loc.getLatitude(), loc.getLongitude(), 1
                )
                if (!list.isEmpty()) {
                    val DirCalle: Address = list[0]
                    mensaje2.text = "Mi direccion es: ${DirCalle.getAddressLine(0)}".trimIndent()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /* Aqui empieza la Clase Localizacion */
    class Localizacion : LocationListener {
        var mainActivity: MainActivity? = null

        override fun onLocationChanged(loc: Location) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            loc.latitude
            loc.longitude
            val Text = """Mi ubicacion actual es: Lat = ${loc.latitude} Long = ${loc.longitude}"""
            mainActivity!!.mensaje1.setText(Text)
            mainActivity!!.setLocation(loc)
        }

        override fun onProviderDisabled(provider: String) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            mainActivity!!.mensaje1.setText("GPS Desactivado")
        }

        override fun onProviderEnabled(provider: String) {
            // Este metodo se ejecuta cuando el GPS es activado
            mainActivity!!.mensaje1.setText("GPS Activado")
        }

        override fun onStatusChanged(
            provider: String,
            status: Int,
            extras: Bundle
        ) {
            when (status) {
                LocationProvider.AVAILABLE -> Log.d(
                    "debug",
                    "LocationProvider.AVAILABLE"
                )
                LocationProvider.OUT_OF_SERVICE -> Log.d(
                    "debug",
                    "LocationProvider.OUT_OF_SERVICE"
                )
                LocationProvider.TEMPORARILY_UNAVAILABLE -> Log.d(
                    "debug",
                    "LocationProvider.TEMPORARILY_UNAVAILABLE"
                )
            }
        }
    }
}