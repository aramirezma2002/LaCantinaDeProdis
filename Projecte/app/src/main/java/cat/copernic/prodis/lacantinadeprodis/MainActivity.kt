package cat.copernic.prodis.lacantinadeprodis

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.ActivityMainBinding
import cat.copernic.prodis.lacantinadeprodis.utils.utils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this,
            R.layout.activity_main
        )

        //val navController = this.findNavController(R.id.myNavHostFragment)

        //Fletxa enderrere i faltaria metode onSupportNavigateUp
        //NavigationUI.setupActionBarWithNavController(this, navController)
        supportActionBar?.hide()
        if(utils().isTabablet(applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }
}