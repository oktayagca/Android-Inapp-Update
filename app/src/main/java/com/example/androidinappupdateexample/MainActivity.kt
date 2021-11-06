package com.example.androidinappupdateexample

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

private const val RC_APP_UPDATE = 100
class MainActivity : AppCompatActivity() {

    private lateinit var mAppUpdateManager:AppUpdateManager
    private lateinit var registerUpdateLauncher:ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerLauncher()
        checkUpdate()
    }

    private fun checkUpdate() {
        mAppUpdateManager = AppUpdateManagerFactory.create(this)
        Log.d("updateTry","CheckingUpdate")
        mAppUpdateManager.appUpdateInfo.addOnSuccessListener {appUpdateInfo ->
            if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)){
                Log.d("updateTry","updateAvailable")
                mAppUpdateManager.startUpdateFlowForResult(appUpdateInfo,AppUpdateType.FLEXIBLE,this,
                    RC_APP_UPDATE)
            }else{
                Log.d("updateTry","No Update Available")
            }
        }
        mAppUpdateManager.registerListener(installStateUpdatedListener)
    }

    private var installStateUpdatedListener = InstallStateUpdatedListener{installState->
        if(installState.installStatus() == InstallStatus.DOWNLOADED){
            Log.d("updateTry","An update has been download")
            showCompletedUpdate()
        }
    }

    override fun onStop() {
        mAppUpdateManager.unregisterListener(installStateUpdatedListener)
        super.onStop()
    }

    private fun showCompletedUpdate() {
        val snackbar:Snackbar = Snackbar.make(findViewById(android.R.id.content),"New App Is Ready!",Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("Install") {
            mAppUpdateManager.completeUpdate()
        }.show()
    }

    private fun registerLauncher(){
         registerUpdateLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            when(result.resultCode){
                RESULT_OK ->{Toast.makeText(this,"Cancel",Toast.LENGTH_LONG).show()}
                RESULT_CANCELED ->{Log.d("updateTry","Result Cancelled")}
            }
        }
    }

}