package cat.copernic.prodis.lacantinadeprodis.ui.principal

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cat.copernic.prodis.lacantinadeprodis.MainActivity
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.ui.activities.PantallaEdicioPerfil


class MenuActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    //Sobreescribim la funció onOptionItemSelected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            //Fem que al polsar el botó de home, ens porti a la pantalla principal
            R.id.homeBtn -> {
                val intent = Intent(this, MainActivity::class.java).apply {
                }
                startActivity(intent)
                true
            }
            //Fem que al polsar el botó del perfil, ens porti a la pantalla d'edició de perfil
            R.id.profileBttn -> {
                val intent = Intent(this, PantallaEdicioPerfil::class.java).apply {
                }
                startActivity(intent)
                true
            }
            //Fem que al polsar el botó de log out, ens tanqui la sessió
            R.id.logOutBttn -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}