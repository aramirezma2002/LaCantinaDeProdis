package cat.copernic.prodis.lacantinadeprodis.ui.principal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaRecuperarContrasenya2Binding
import cat.copernic.prodis.lacantinadeprodis.utils.utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PantallaRecuperarContrasenya2 : Fragment() {

    private val db = Firebase.firestore
    private var auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPantallaRecuperarContrasenya2Binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_recuperar_contrasenya2, container, false
        )
        val args = PantallaRecuperarContrasenya2Args.fromBundle(requireArguments())
        val usertype = args.usertype
        val dni = args.dni

        binding.btnPRecuperarContrasenya2Continuar.setOnClickListener {
            // si els dos camps de contrasenyes no están buits es faran les següents comprovacions
            if (!binding.dtTxtPRegistrePasswordText.text.isNullOrEmpty() &&
                !binding.dtTxtPRegistreRepeteixPasswordText.text.isNullOrEmpty()
            ) {
                // Es comprova si les contrasenyes coincideixen
                if (binding.dtTxtPRegistrePasswordText.text.toString() ==
                    binding.dtTxtPRegistreRepeteixPasswordText.text.toString()
                ) {
                    // si totes les comprovacions son correctes es crida a la funció encarregada de fer el canvi de contrasenya
                    val psswd =
                        binding.dtTxtPRegistreRepeteixPasswordText.text.toString() + "prodis"
                    changePassword(dni, psswd, usertype)

                } else {
                    // En cas que les contrasenyes no coincideixin s'informa a l'usuari
                    utils().showAlert(getString(R.string.error), getString(R.string.les_contrasenyes_no_coincideixen), this.context)
                }
            } else {
                // En cas que algun dels camps estigui buit, s'informa a l'usuari
                utils().showAlert(getString(R.string.error), getString(R.string.els_camps_no_estan_plens), this.context)
            }
        }

        return binding.root
    }

    //Funció que relitza el canvi de contrasenya
    private fun changePassword(dni: String, psswd: String, usertype: String) {
        db.collection("users").document(dni).get().addOnSuccessListener { result ->
            auth.signInWithEmailAndPassword(
                result.get("email").toString(),
                result.get("password").toString(),
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.updatePassword(psswd)?.addOnSuccessListener {
                        auth.signOut()
                        Toast.makeText(
                            this.context,
                            getString(R.string.s_ha_canviat_la_contrasenya),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    val pas = hashMapOf("password" to psswd)
                    db.collection("users").document(dni).update(
                        pas as Map<String, Any>
                    )
                    view?.findNavController()?.navigate(
                        PantallaRecuperarContrasenya2Directions.actionPantallaRecuperarContrasenya2ToPantallaIniciSessioClientAdmin(
                            usertype
                        )
                    )
                } else {
                    utils().showAlert(getString(R.string.error), getString(R.string.error_en_recuperar_contrasenya), this.context)
                }
            }
        }
            .addOnFailureListener {
                utils().showAlert(getString(R.string.error), getString(R.string.l_usuari_no_esta_registrat), this.context)
            }

    }

}
