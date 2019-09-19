package rs.co.stopscanactivity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.NetworkScanRequest
import android.telephony.TelephonyScanManager
import android.telephony.CellInfo
import android.util.Log
import android.widget.Button
import android.os.AsyncTask
import android.telephony.AccessNetworkConstants
import android.telephony.RadioAccessSpecifier
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(){

    lateinit var scan : Button
    private val RECORD_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scan = findViewById(R.id.button)
        scan.setOnClickListener {
            performScan()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.MODIFY_PHONE_STATE),
                RECORD_REQUEST_CODE)
    }

    private fun performScan() {
            val permission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.MODIFY_PHONE_STATE)

            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.d("djevtic", "Permission denied")
                makeRequest()
            } else {
                Log.d("djevtic", "Permission GRANTED")
                doScan()
            }
    }

    private fun doScan() {
        val networkScanRequest: NetworkScanRequest
        val radioAccessSpecifiers: Array<RadioAccessSpecifier?> = arrayOfNulls(1)
        val bands: IntArray = intArrayOf(2000)
        val PLMNIds = ArrayList<String>(Arrays.asList("42501"))


        val telephonyManager = applicationContext
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        bands[0] = AccessNetworkConstants.UtranBand.BAND_1
        radioAccessSpecifiers[0] = RadioAccessSpecifier(
                AccessNetworkConstants.AccessNetworkType.UTRAN,
                bands,
                null)

        networkScanRequest = NetworkScanRequest(
                NetworkScanRequest.SCAN_TYPE_ONE_SHOT,
                radioAccessSpecifiers,
                0,
                60,
                false,
                0,
                PLMNIds)

        telephonyManager.requestNetworkScan(networkScanRequest, AsyncTask.SERIAL_EXECUTOR, RadioCallback())

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.d("djevtic", "Permission has been denied by user")
                } else {
                    Log.d("djevtic", "Permission has been granted by user")
                    doScan()
                }
            }
        }
    }

    private inner class RadioCallback : TelephonyScanManager.NetworkScanCallback() {
        private var mCellInfoResults: List<CellInfo>? = null
        private var mScanError: Int = 0

        override fun onResults(cellInfoResults: List<CellInfo>) {
            mCellInfoResults = cellInfoResults
            this@MainActivity.runOnUiThread(Runnable {
                for (cellInfo in mCellInfoResults!!) {
                    Log.d("djevtic"," $cellInfo ")
                }
            })
        }

        override fun onError(error: Int) {
            mScanError = error
            this@MainActivity.runOnUiThread(Runnable { Log.d("djevtic"," Error: $mScanError") })
        }

        override fun onComplete() {
            this@MainActivity.runOnUiThread(Runnable { Log.d("djevtic"," Scan Completed! ") })
        }
    }
}
