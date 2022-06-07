package cat.copernic.prodis.lacantinadeprodis

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cat.copernic.prodis.lacantinadeprodis.ui.activities.AdministradorActivity
import cat.copernic.prodis.lacantinadeprodis.ui.activities.CaixerActivity
import cat.copernic.prodis.lacantinadeprodis.ui.activities.ComandesActivity
import cat.copernic.prodis.lacantinadeprodis.ui.activities.CuinerActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    lateinit var dni: String
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Start Activity

        if (auth.currentUser == null){
            startActivity(Intent(this, MainActivity::class.java))
        }else{
            val email = auth.currentUser?.email.toString()
            db.collection("users").get().addOnSuccessListener { result ->
                for(dc in result){
                    if (dc.get("email").toString() == email){
                        val usertype = dc.get("usertype").toString()
                        dni = dc.get("dni").toString()
                        startactivity(usertype)
                        this.finish()
                    }
                }
            }
        }
        finishActivity(finish().hashCode())
    }

    private fun startactivity(usertype: String) {
        when (usertype) {
            "clientR" -> showCambrerClient(usertype, dni)
            "cambrer" -> showCambrerClient(usertype, dni)
            "caixer" -> showCaixer(dni)
            "cuiner" -> showCuiner(dni)
            "admin" -> showAdmin(dni)
        }
    }

    //funció per iniciar l'Activity de Client i Cambrer
    private fun showCambrerClient(username: String, dni: String) {
        val intent = Intent(this, ComandesActivity::class.java).apply {
            putExtra("usertype", username)
            putExtra("dni", dni)
        }
        //utils().idComanda = "N"
        startActivity(intent)
        this.finish()
    }

    //Funció per inciar l'Activity de caixer
    private fun showCaixer(dni: String) {
        val intent = Intent(this, CaixerActivity::class.java).apply {
            putExtra("dni", dni)
        }

        startActivity(intent)
        this.finish()
    }

    //Funció per inciar l'Activity de cuiner
    private fun showCuiner(dni: String) {
        val intent = Intent(this, CuinerActivity::class.java).apply {
            putExtra("dni", dni)
        }
        startActivity(intent)
        this.finish()
    }

    //Funció per inciar l'Activity de administrador
    private fun showAdmin(dni: String) {
        val intent = Intent(this, AdministradorActivity::class.java).apply {
            putExtra("dni", dni)
        }
        startActivity(intent)
        this.finish()
    }
}