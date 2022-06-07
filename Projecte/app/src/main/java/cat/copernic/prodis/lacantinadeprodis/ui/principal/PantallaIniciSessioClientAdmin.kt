package cat.copernic.prodis.lacantinadeprodis.ui.principal

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import cat.copernic.prodis.lacantinadeprodis.ui.activities.AdministradorActivity
import cat.copernic.prodis.lacantinadeprodis.ui.activities.CaixerActivity
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.ui.activities.ComandesActivity
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaIniciSessioClientAdminBinding
import cat.copernic.prodis.lacantinadeprodis.ui.activities.CuinerActivity
import cat.copernic.prodis.lacantinadeprodis.utils.utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PantallaIniciSessioClientAdmin : Fragment() {

    private val db = Firebase.firestore
    private var auth = FirebaseAuth.getInstance()
    private lateinit var dni: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPantallaIniciSessioClientAdminBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_inici_sessio_client_admin, container, false
        )

        val args = PantallaIniciSessioClientAdminArgs.fromBundle(requireArguments())
        val usertype = args.usertype

        //Si l'usuari no es de tipus client, ocultem el botó de registre
        if (usertype != "clientR") {
            binding.txtPiniciarSessioClientRegistre.visibility = View.INVISIBLE
        }

        //botó per inciciar sessió on es realitzen totes les comprovacions abans de poguer inciar sessió
        binding.btnPiniciarSessioClient.setOnClickListener {
            dni = binding.dtTxtPIniciarSessioClientDni.text.toString().uppercase()
            var passwd = binding.dtTxtPIniciarSessioClientPassword.text.toString()
            if (binding.dtTxtPIniciarSessioClientDni.text.isNotEmpty() &&
                !binding.dtTxtPIniciarSessioClientPassword.text.isNullOrEmpty()
            ) {
                passwd += "prodis"
                db.collection("users").document(dni).get().addOnSuccessListener { result ->
                    if (result.get("usertype").toString() == usertype) {
                        auth.signInWithEmailAndPassword(
                            result.get("email").toString(),
                            passwd,
                        ).addOnCompleteListener {
                            if (it.isSuccessful) {
                                startActivity(usertype)
                            } else {
                                utils().showAlert(
                                    getString(R.string.error),
                                    getString(R.string.error_en_inici_de_sessio),
                                    this.context
                                )
                            }
                        }
                            .addOnFailureListener {
                                utils().showAlert(
                                    getString(R.string.error),
                                    getString(R.string.l_usuari_no_esta_registrat),
                                    this.context
                                )
                            }
                    } else {
                        if (usertype.equals("clientR")) {
                            utils().showAlert(
                                getString(R.string.error),
                                getString(R.string.aquest_usuari_no_es_de_tipus) + getString(R.string.clientLowerCase),
                                this.context
                            )
                        } else if (usertype.equals("cambrer")) {
                            utils().showAlert(
                                getString(R.string.error),
                                getString(R.string.aquest_usuari_no_es_de_tipus) + getString(R.string.cambrerLowerCase),
                                this.context
                            )
                        } else if (usertype.equals("cuiner")) {
                            utils().showAlert(
                                getString(R.string.error),
                                getString(R.string.aquest_usuari_no_es_de_tipus) + getString(R.string.cuinerLowerCase),
                                this.context
                            )
                        } else if (usertype.equals("caixer")) {
                            utils().showAlert(
                                getString(R.string.error),
                                getString(R.string.aquest_usuari_no_es_de_tipus) + getString(R.string.caixerLowerCase),
                                this.context
                            )
                        }

                    }
                }

            } else {
                Toast.makeText(
                    this.context,
                    getString(R.string.falta_omplir_els_camps),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // botó que navega fins la pantalla de recuperar contrasenya
        binding.txtPiniciarSessioClientOblidatContrasenya.setOnClickListener {
            view?.findNavController()?.navigate(
                PantallaIniciSessioClientAdminDirections.actionPantallaIniciSessioClientAdminToPantallaRecuperarContrasenya1(
                    usertype
                )
            )
        }
        // botó que navega fins la pantalla de registre
        binding.txtPiniciarSessioClientRegistre.setOnClickListener {
            view?.findNavController()?.navigate(
                PantallaIniciSessioClientAdminDirections.actionPantallaIniciSessioClientAdminToPantallaRegistre(
                    usertype
                )
            )
        }

        return binding.root
    }

    //En funció del tipus d'usuari que vulgui iniciar sessió, s'obrirà l'Activity corresponent cridant a la funciño que pertoqui
    private fun startActivity(usertype: String) {
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
        val intent = Intent(this.context, ComandesActivity::class.java).apply {
            putExtra("usertype", username)
            putExtra("dni", dni)
        }
        //utils().idComanda = "N"
        startActivity(intent)
    }

    //Funció per inciar l'Activity de caixer
    private fun showCaixer(dni: String) {
        val intent = Intent(this.context, CaixerActivity::class.java).apply {
            putExtra("dni", dni)
        }

        startActivity(intent)
    }

    //Funció per inciar l'Activity de cuiner
    private fun showCuiner(dni: String) {
        val intent = Intent(this.context, CuinerActivity::class.java).apply {
            putExtra("dni", dni)
        }
        startActivity(intent)
    }

    //Funció per inciar l'Activity de administrador
    private fun showAdmin(dni: String) {
        val intent = Intent(this.context, AdministradorActivity::class.java).apply {
            putExtra("dni", dni)
        }
        startActivity(intent)
    }

}