package com.example.dbapp

import android.util.Log

fun getDownsamplingFilterCoeffs(dsf: Int): IIRFilterCoeffs {
    var message = ""
    val fCoeffs = when (dsf) {
        in 90 .. 101 -> getDSF100Filter()
        in 45..51 -> getDSF50Filter()
        in 13.. 16 -> getDSF15Filter()
        else -> {
            message = "WARNING: dsf=$dsf not handled! Using default dsf=100: "
            getDSF100Filter()
        }
    }
    message += "dsf=$dsf"
    Log.i("db", "message")
    return fCoeffs
}

data class IIRFilterCoeffs(
    val b: DoubleArray,
    val a: DoubleArray
)

/*
 * Lowpass filter for downsampling by 100 to 1. Generated with:
 *
 * filter-coeffs.py 4 105 | tr ' ' ',' | sed 's/^/doubleArrayOf(/' | sed 's/$/),/
 */
fun getDSF100Filter(): IIRFilterCoeffs {
    return IIRFilterCoeffs(
        doubleArrayOf(4.818075622176551e-08,1.9272302488706204e-07,2.890845373305931e-07,1.9272302488706204e-07,4.818075622176551e-08),
        doubleArrayOf(1.0,-3.9218168697111873,5.768492417699226,-3.7714653618682235,0.9247905847722856)
    )
}

/*
 * Lowpass filter for downsampling by 100 to 1. Generated with:
 *
 * filter-coeffs.py 4 52.5 | tr ' ' ',' | sed 's/^/doubleArrayOf(/' | sed 's/$/),/
 */
fun getDSF50Filter(): IIRFilterCoeffs {
    return IIRFilterCoeffs(
        doubleArrayOf(7.419928563643583e-07,2.967971425457433e-06,4.45195713818615e-06,2.967971425457433e-06,7.419928563643583e-07),
        doubleArrayOf(1.0,-3.8436422151447625,5.543034662442482,-3.5546006611617798,0.8552200857497626)
    )
}

/*
 * Lowpass filter for downsampling by 100 to 1. Generated with:
 *
 * filter-coeffs.py 4 16 | tr ' ' ',' | sed 's/^/doubleArrayOf(/' | sed 's/$/),/
 */
fun getDSF15Filter(): IIRFilterCoeffs {
    return IIRFilterCoeffs(
        doubleArrayOf(7.277254928998084e-05,0.00029109019715992335,0.00043663529573988505,0.00029109019715992335,7.277254928998084e-05),
        doubleArrayOf(1.0,-3.4873077415499,4.589291232078407,-2.6988843913407545,0.5980652616008872),
    )
}

class IIRFilter(private val b: DoubleArray, private val a: DoubleArray) {
    private val inputBuffer: DoubleArray
    private val outputBuffer: DoubleArray

    init {
        require(b.isNotEmpty() && a.isNotEmpty()) { "Filter coefficients must not be empty" }
        require(b.size == a.size) { "Filter coefficients must have the same length" }

        inputBuffer = DoubleArray(b.size)
        outputBuffer = DoubleArray(a.size - 1)
    }

    fun filter(sample: Double): Double {
        inputBuffer[0] = sample
        var output = 0.0

        for (i in b.indices) {
            output += b[i] * inputBuffer[i]
            if (i > 0) {
                output -= a[i] * outputBuffer[i - 1]
            }
        }

        for (i in inputBuffer.lastIndex downTo 1) {
            inputBuffer[i] = inputBuffer[i - 1]
        }

        for (i in outputBuffer.lastIndex downTo 1) {
            outputBuffer[i] = outputBuffer[i - 1]
        }

        outputBuffer[0] = output

        return output
    }
}
