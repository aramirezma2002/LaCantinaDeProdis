package cat.copernic.prodis.lacantinadeprodis.utils

import android.app.AlertDialog
import android.content.Context
import android.telephony.TelephonyManager
import android.view.View
import cat.copernic.prodis.lacantinadeprodis.R
import java.util.*

class utils (){

    fun showAlert(title: String, msg: String, context: Context ?= null){
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(R.string.acceptar, null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun isTabablet (manager: TelephonyManager): Boolean {
        return Objects.requireNonNull(manager).phoneType == TelephonyManager.PHONE_TYPE_NONE
    }

}