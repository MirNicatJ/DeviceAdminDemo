package com.crocusoft.deviceadmindemo

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.UserManager
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.crocusoft.deviceadmindemo.databinding.ActivityMainBinding

//dpm set-device-owner com.crocusoft.deviceadmindemo/.AdminReceiver

class MainActivity : AppCompatActivity() {

    private lateinit var dpm: DevicePolicyManager
    private var resultLauncher: ActivityResultLauncher<Intent>? = null
    private var activityMainBinding: ActivityMainBinding? = null
    private lateinit var activateIntent: Intent
    private lateinit var adminReceiver: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding?.root)
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminReceiver = ComponentName(this@MainActivity, AdminReceiver::class.java)
        activityMainBinding?.setListeners()
        activateIntent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                adminReceiver
            )
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.add_admin_extra_app_text)
            )
        }
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                Log.e(
                    "Tag",
                    "Result: \n$result \n ${result.resultCode} \n${result.data?.extras}"
                )
            }
    }

    private fun ActivityMainBinding.setListeners() {
        buttonActivate.setOnClickListener {
            if (dpm.isAdminActive(adminReceiver)
                    .not()
            ) {
                resultLauncher?.launch(activateIntent)
            }
        }

        buttonMakeDeviceOwner.setOnClickListener {
            try {
                Runtime.getRuntime()
                    .exec("dpm set-device-owner com.crocusoft.deviceadmindemo/.AdminReceiver")
            } catch (e: Exception) {
                Log.e("TAG", "device owner not set")
                Log.e("TAG", e.toString())
                e.printStackTrace()
            }
        }

        buttonSetNewPassword.setOnClickListener {
//            Log.e(dpm.isAdminActive(adminReceiver))
            if (dpm.isAdminActive(adminReceiver)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dpm.requiredPasswordComplexity = DevicePolicyManager.PASSWORD_COMPLEXITY_HIGH
                } else {
                    dpm.setPasswordQuality(
                        adminReceiver,
                        DevicePolicyManager.PASSWORD_QUALITY_COMPLEX
                    )
                }
                Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD).also { intent ->
                    startActivity(intent)
                }
            }
        }

        buttonLock.setOnClickListener {
            dpm.lockNow()
        }

        buttonDisableCamera.setOnClickListener {
            dpm.setCameraDisabled(adminReceiver, dpm.getCameraDisabled(adminReceiver).not())
        }

        buttonDisableSS.setOnClickListener {
            dpm.setScreenCaptureDisabled(
                adminReceiver,
                !dpm.getScreenCaptureDisabled(adminReceiver)
            )
        }

        buttonDisableAdjustVolume.setOnClickListener {
            if (dpm.getUserRestrictions(adminReceiver)
                    .get(UserManager.DISALLOW_ADJUST_VOLUME) as? Boolean == true
            ) {
                dpm.clearUserRestriction(adminReceiver, UserManager.DISALLOW_ADJUST_VOLUME)
            } else dpm.addUserRestriction(adminReceiver, UserManager.DISALLOW_ADJUST_VOLUME)
        }

        buttonDisableOutgoingCalls.setOnClickListener {
            if (dpm.getUserRestrictions(adminReceiver)
                    .get(UserManager.DISALLOW_OUTGOING_CALLS) as? Boolean == true
            ) {
                dpm.clearUserRestriction(adminReceiver, UserManager.DISALLOW_OUTGOING_CALLS)
            } else dpm.addUserRestriction(adminReceiver, UserManager.DISALLOW_OUTGOING_CALLS)
        }

        buttonMuteSound.setOnClickListener {
            dpm.setMasterVolumeMuted(adminReceiver, dpm.isMasterVolumeMuted(adminReceiver).not())
        }

        buttonSms.setOnClickListener {
            if (dpm.getUserRestrictions(adminReceiver)
                    .get(UserManager.DISALLOW_SMS) as? Boolean == true
            ) {
                dpm.clearUserRestriction(adminReceiver, UserManager.DISALLOW_SMS)
            } else dpm.addUserRestriction(adminReceiver, UserManager.DISALLOW_SMS)
        }

        buttonDisableMaps.setOnClickListener {
            dpm.setPackagesSuspended(
                adminReceiver,
                arrayOf("com.google.android.apps.maps"),
                dpm.isPackageSuspended(adminReceiver, "com.google.android.apps.maps").not()
            )
        }

        buttonDisableFactoryReset.setOnClickListener {
            if (dpm.getUserRestrictions(adminReceiver)
                    .get(UserManager.DISALLOW_FACTORY_RESET) as? Boolean == true
            ) {
                dpm.clearUserRestriction(adminReceiver, UserManager.DISALLOW_FACTORY_RESET)
            } else dpm.addUserRestriction(adminReceiver, UserManager.DISALLOW_FACTORY_RESET)
        }

        buttonConfigBrightness.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (dpm.getUserRestrictions(adminReceiver)
                        .get(UserManager.DISALLOW_CONFIG_BRIGHTNESS) as? Boolean == true
                ) {
                    dpm.clearUserRestriction(adminReceiver, UserManager.DISALLOW_CONFIG_BRIGHTNESS)
                } else dpm.addUserRestriction(adminReceiver, UserManager.DISALLOW_CONFIG_BRIGHTNESS)
            }
        }

        buttonSetMessages.setOnClickListener {
            dpm.setShortSupportMessage(adminReceiver, "Icazə verilmir")
            dpm.setLongSupportMessage(adminReceiver, "Bu əməliyyata icazə verilmir")
        }

        buttonWipeData.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dpm.wipeData(
                    DevicePolicyManager.WIPE_EXTERNAL_STORAGE,
                    "Kredit odenmediyi ucun melumatlar silinir"
                )
            }
        }
    }
}