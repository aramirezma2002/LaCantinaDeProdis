package cat.copernic.prodis.lacantinadeprodis.ui.activities

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cat.copernic.prodis.lacantinadeprodis.MainActivity
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.ActivityAdministradorBinding
import cat.copernic.prodis.lacantinadeprodis.utils.utils
import com.google.firebase.auth.FirebaseAuth

class AdministradorActivity : AppCompatActivity() {

    private lateinit var dni: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityAdministradorBinding>(this,
            R.layout.activity_administrador
        )

        //val navController = this.findNavController(R.id.myNavHostFragment)

        //Fletxa enderrere i faltaria metode onSupportNavigateUp
        //NavigationUI.setupActionBarWithNavController(this, navController)

        title = ""

        var bundle = intent.extras
        dni = bundle?.getString("dni").toString()

        if(utils().isTabablet(applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

    }

    //Funció per inflar el menú
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    //Funció per indicar que fará cada botó del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.arrowBackBtn -> {
                //Anirá a la pantalla anterior
                this.onBackPressed()
                true
            }
            R.id.homeBtn -> {
                //Anirá a la pantalla principal
                val intent = Intent(this, MainActivity::class.java).apply {
                }
                startActivity(intent)
                finish()
                true
            }
            R.id.profileBttn -> {
                //Anirá a la pantalla d'edició de perfil
                val intent = Intent(this, PantallaEdicioPerfil::class.java).apply {
                }
                startActivity(intent)
                true
            }
            //Tancará sessió
            R.id.logOutBttn -> {
                FirebaseAuth.getInstance().signOut()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}