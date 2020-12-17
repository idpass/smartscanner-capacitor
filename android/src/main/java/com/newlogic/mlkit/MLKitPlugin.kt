package com.newlogic.mlkit

import android.app.Activity
import android.content.Intent
import com.getcapacitor.*
import com.google.gson.Gson
import org.idpass.smartscanner.lib.SmartScannerActivity
import org.idpass.smartscanner.lib.config.ScannerOptions
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

@NativePlugin(requestCodes = [MLKitPlugin.REQUEST_OP_SCANNER])
class MLKitPlugin : Plugin() {

    companion object {
        const val REQUEST_OP_SCANNER = 1001
    }

    @PluginMethod
    fun executeScanner(call: PluginCall) {
        val action = call.getString("action")
        val options: JSONObject = call.getObject("options")
        saveCall(call)
        if (action == "START_SCANNER") {
            Timber.d("executeScanner %s", action)
            val activity: Activity = activity
            val intent = Intent(activity, SmartScannerActivity::class.java)
            val scannerOptions = Gson().fromJson(options.toString(), ScannerOptions::class.java)
            intent.putExtra(SmartScannerActivity.SCANNER_OPTIONS, scannerOptions)
            startActivityForResult(call, intent, REQUEST_OP_SCANNER)
        } else {
            call.error("\"$action\" is not a recognized action.")
        }
    }

    override fun handleOnActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.handleOnActivityResult(requestCode, resultCode, data)
        val savedCall = savedCall ?: return
        if (requestCode == REQUEST_OP_SCANNER) {
            Timber.d("Plugin post SmartScannerActivity resultCode %d", resultCode)
            if (resultCode == Activity.RESULT_OK) {
                val returnedResult = data.getStringExtra(SmartScannerActivity.SCANNER_RESULT)
                Timber.d("Plugin post SmartScannerActivity result %s", returnedResult)
                try {
                    val result = JSONObject(returnedResult)
                    val ret = JSObject()
                    ret.put(SmartScannerActivity.SCANNER_RESULT, result)
                    savedCall.success(ret)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Timber.d("Plugin post SmartScannerActivity RESULT CANCELLED")
                savedCall.error("Scanning Cancelled.")
            } else {
                savedCall.error("Scanning Failed.")
            }
        } else {
            savedCall.error("Unknown Request Code!")
        }
    }
}
