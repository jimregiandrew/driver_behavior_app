package com.example.dbapp

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import kotlin.math.roundToInt

class SensorPeriodCounter(
    private val outputNS: Long,
    private val updateCount: (Int) -> Unit
) : SensorEventListener {

    var numSamples = 0
    var lastOutputNS = 0L
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        numSamples++
        if (lastOutputNS == 0L) {
            lastOutputNS = event.timestamp
            numSamples = 0
        }
        if (event.timestamp - lastOutputNS >= outputNS) {
            val sec = nanosecondsToSeconds(event.timestamp - lastOutputNS)
            val ipSamplesPerSec = numSamples / sec
            lastOutputNS = event.timestamp
            //if (ipSamplesPerSec.roundToInt() != numSamples)
                Log.i("db", "SensorPeriodCounter: " +
                    "ipSamplesPerSec=${ipSamplesPerSec.format(2)}, " +
                    "numSamples=$numSamples, sec=${sec.format(4)}")
            updateCount(ipSamplesPerSec.roundToInt())
            numSamples = 0
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

//data class AccelFilterConfig(
//    val dsf: Int,
//    val filterCoefs: IIRFilterCoeffs
//)

class AccelFilter(
    private val dsf: Int,
    filterCoeffs: IIRFilterCoeffs,
    private val updateAccelFn: (String) -> Unit
): SensorEventListener {

    private val b = filterCoeffs.b
    private val a = filterCoeffs.a
    private val xFilter = IIRFilter(b, a)
    private val yFilter = IIRFilter(b, a)
    private val zFilter = IIRFilter(b, a)

    private var numSamples = 0
    private var lastOutputNS = 0L

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.i("db","sensor=${sensor.name} accuracy has changed to $accuracy")
    }

    override fun onSensorChanged(event: SensorEvent) {
        numSamples++
        if (lastOutputNS == 0L) {
            lastOutputNS = event.timestamp
            numSamples = 0
        }
        val x: Double = xFilter.filter(event.values[0].toDouble())
        val y: Double = yFilter.filter(event.values[1].toDouble())
        val z: Double = zFilter.filter(event.values[2].toDouble())

        if (numSamples >= dsf) {
            val sec = nanosecondsToSeconds(event.timestamp - lastOutputNS)
            val ipSamplesPerSec = numSamples / sec
            val accelData = "x=%.1f, y=%.1f, z=%.1f, numSamples=%d, ipSamplesPerSec=%.1f, sec=%.5f".format(
                x,
                y,
                z,
                numSamples,
                ipSamplesPerSec,
                sec
            )
            //Log.i("db", accelData)
            updateAccelFn(accelData)
            lastOutputNS = event.timestamp
            numSamples = 0
        }
    }
}

private fun nanosecondsToSeconds(nanoseconds: Long) : Double {
    return nanoseconds /1000000000.0
}
