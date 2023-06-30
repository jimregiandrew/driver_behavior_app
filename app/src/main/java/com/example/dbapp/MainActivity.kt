package com.example.dbapp

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dbapp.ui.theme.DBAppTheme

private val ipSensorMicroSec = SensorManager.SENSOR_DELAY_GAME
class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor
    private lateinit var accelFilter: AccelFilter
    private lateinit var periodCounter: SensorPeriodCounter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        periodCounter = SensorPeriodCounter(outputNS = 1_000_000_000) { dsf ->
            if (this::accelFilter.isInitialized)
                return@SensorPeriodCounter
            Log.i("db", "Detected dsf=$dsf (downsampling factor)")
            //sensorManager.unregisterListener(periodCounter)
            accelFilter = AccelFilter(
                dsf = dsf / 2,
                getDownsamplingFilterCoeffs(dsf)
            ) {
                updateDisplay(this, it)
            }
            sensorManager.registerListener(
                accelFilter,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                ipSensorMicroSec
            )
        }
        sensorManager.registerListener(
            periodCounter,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            ipSensorMicroSec
        )
        updateDisplay(this, "starting")
    }
}

fun updateDisplay(componentActivity: ComponentActivity, accelData: String) {
    componentActivity.setContent {
        DBAppTheme {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AccelView(accelData)
            }
        }
    }
}

@Composable
fun AccelView(accelData: String, modifier: Modifier = Modifier) {
    Text(
        text = accelData,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    DBAppTheme {
        AccelView("starting")
    }
}
