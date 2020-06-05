package com.excuta.musictagger.permission

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.collection.ArraySet
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "PermissionFragment"

class PermissionFragment : Fragment() {
    private var requestPermissionCode: Int = 0
    private val permissionsMutableLiveData = MutableLiveData<PermissionRequest>()
    val permissionLiveData: LiveData<PermissionRequest> = permissionsMutableLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        generateRequestCode()
    }

    private fun generateRequestCode() {
        if (requestPermissionCode == 0) {
            var i = Random().nextInt(100)
            while (requestCodes.contains(i)) {
                i = Random().nextInt(100)
            }
            requestPermissionCode = i
            requestCodes.add(i)
        }
    }

    fun requestPermissions(vararg permissions: String) {
        if (permissions.isNotEmpty()) requestPermissions(permissions, requestPermissionCode)
    }

    fun hasPermission(permission: String): Boolean {
        val permissionState = context?.let {
            ActivityCompat.checkSelfPermission(it, permission)
        }
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    fun hasAllPermissions(vararg permissions: String): Boolean {
        permissions.forEach { permission ->
            val permissionState = context?.let {
                ActivityCompat.checkSelfPermission(it, permission)
            }
            if (permissionState != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == requestPermissionCode) {
            if (grantResults.isEmpty()) {
                //cancelled, Very difficult to happen as prompt is not user cancellable
                permissionsMutableLiveData.setValue(Cancelled())
            } else {
                val permissionRequests = createPermissionRequests(grantResults, permissions)
                permissionRequests.forEach { permissionsMutableLiveData.value = it }
            }
        }
    }

    private fun createPermissionRequests(grantResults: IntArray, permissions: Array<String>):
        ArrayList<PermissionRequest> {
        val permissionRequests = ArrayList<PermissionRequest>()
        grantResults.forEachIndexed { index, result ->
            if (result == PackageManager.PERMISSION_GRANTED)
                permissionRequests.add(
                    Granted(
                        permissions[index]
                    )
                )
            else
                permissionRequests.add(
                    Denied(
                        permissions[index]
                    )
                )
        }
        return permissionRequests
    }

    override fun onDestroy() {
        super.onDestroy()
        requestCodes.remove(requestPermissionCode)
    }

    companion object {

        private val requestCodes = ArraySet<Int>()
    }

    class Provider constructor(private val fragmentManager: FragmentManager) {
        fun get(): PermissionFragment {
            fragmentManager.findFragmentByTag(TAG)?.let { return it as PermissionFragment }

            val permissionFragment = PermissionFragment()

            fragmentManager.beginTransaction().add(
                permissionFragment,
                TAG
            ).commit()

            return permissionFragment
        }
    }
}
