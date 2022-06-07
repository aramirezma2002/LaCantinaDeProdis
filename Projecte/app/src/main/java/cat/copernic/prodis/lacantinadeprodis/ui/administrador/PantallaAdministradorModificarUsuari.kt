package cat.copernic.prodis.lacantinadeprodis.ui.administrador

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaAdministradorModificarUsuariBinding
import cat.copernic.prodis.lacantinadeprodis.utils.utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class PantallaAdministradorModificarUsuari : Fragment(), AdapterView.OnItemSelectedListener {

    private val db = Firebase.firestore
    private var auth = FirebaseAuth.getInstance()
    private var arrUser = ArrayList<String>()
    private var arrUserId = ArrayList<String>()
    private var arrUserType = ArrayList<String>()
    private lateinit var spinner: Spinner
    private lateinit var spinnerUserType: Spinner
    private lateinit var adapter: ArrayAdapter<*>
    private lateinit var adapterUType: ArrayAdapter<*>
    private lateinit var binding: FragmentPantallaAdministradorModificarUsuariBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pantalla_administrador_modificar_usuari, container, false
        )

        val args = PantallaAdministradorModificarUsuariArgs.fromBundle(requireArguments())
        arrUser = args.userArr as ArrayList<String>
        arrUserId = args.userArrId as ArrayList<String>
        arrUserType = args.usertypeArr as ArrayList<String>


        spinner = binding.spinUsuariModificar

        val context = this.requireContext()

        adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item, arrUser
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        spinnerUserType = binding.spinTipusUsuari
        adapterUType = ArrayAdapter(
            context, android.R.layout.simple_spinner_item, arrUserType
        )
        adapterUType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUserType.adapter = adapterUType
        spinnerUserType.onItemSelectedListener = this


        binding.btnPAdministradorModificarUsuariGuardar.setOnClickListener {
            var usertype = ""
            when (spinnerUserType.selectedItem.toString()) {
                "Administrador" -> usertype = "admin"
                "Caixer" -> usertype = "caixer"
                "Cambrer" -> usertype = "cambrer"
                "Client" -> usertype = "client"
                "Client Remot" -> usertype = "clientR"
                "Cuiner" -> usertype = "cuiner"
            }

            saveUser(usertype)
        }

        binding.btnPAdministradorModificarUsuariElimiarUsuari.setOnClickListener {
            val user = binding.dtTxtPAdministradorModificarUsuariPersonName.text.toString() +
                    " " +
                    binding.dtTxtPAdministradorModificarUsuariPersonSurname.text.toString()
            val dni = binding.dtTxtPAdministradorModificarUsuariDtDni.text.toString()
            val builder = androidx.appcompat.app.AlertDialog.Builder(this.requireContext())
            builder.setTitle(getString(R.string.avis))
            builder.setMessage(getString(R.string.estas_segur_que_vols_eliminar_a_usuari) + user)
            builder.setPositiveButton(getString(R.string.acceptar)) { _, _ ->
                deleteUser(dni)
            }
            builder.setNegativeButton(getString(R.string.cancelar), null)
            val dialog: androidx.appcompat.app.AlertDialog = builder.create()
            dialog.show()
        }


        return binding.root
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val spinnerPosition = spinner.selectedItemPosition

        if (parent == spinner) {
            //spinnerPosition = position
            db.collection("users").document(arrUserId[position]).get()
                .addOnSuccessListener { document ->
                    val pasword = document.get("password").toString()

                    val pswd = pasword.replace("prodis", "")

                    binding.dtTxtPAdministradorModificarUsuariPersonName.setText(
                        document.get("username").toString()
                    )

                    binding.dtTxtPAdministradorModificarUsuariPersonSurname.setText(
                        document.get("usersurname").toString()
                    )

                    binding.dtTxtPAdministradorModificarUsuariDtDni.setText(
                        document.get("dni").toString()
                    )

                    if (document.get("email").toString() != "null") {
                        binding.txtPAdministradorModificarUsuariEmail.visibility = View.VISIBLE
                        binding.dtTxtPAdministradorModificarUsuariEmail.visibility = View.VISIBLE
                        binding.dtTxtPAdministradorModificarUsuariEmail.setText(
                            document.get("email").toString()
                        )
                    } else {
                        binding.txtPAdministradorModificarUsuariEmail.visibility = View.INVISIBLE
                        binding.dtTxtPAdministradorModificarUsuariEmail.visibility = View.INVISIBLE

                    }

                    if (pswd == "null") {
                        binding.dtTxtPAdministradorModificarUsuariPasswordL.visibility =
                            View.INVISIBLE

                    } else {
                        binding.dtTxtPAdministradorModificarUsuariPasswordL.visibility =
                            View.VISIBLE
                        binding.dtTxtPAdministradorModificarUsuariPassword.setText(pswd)
                    }

                    if (document.get("usertype").toString() == "null") {
                        spinnerUserType.visibility = View.INVISIBLE
                        binding.txtPAdministradorModificarUsuariUsertype.visibility = View.INVISIBLE
                    } else {
                        spinnerUserType.visibility = View.VISIBLE
                        binding.txtPAdministradorModificarUsuariUsertype.visibility = View.VISIBLE
                    }

                    when (document.get("usertype").toString()) {
                        "admin" -> spinnerUserType.setSelection(0)
                        "caixer" -> spinnerUserType.setSelection(1)
                        "cambrer" -> spinnerUserType.setSelection(2)
                        "client" -> spinnerUserType.setSelection(3)
                        "clientR" -> spinnerUserType.setSelection(4)
                        "cuiner" -> spinnerUserType.setSelection(5)
                        "null" -> null
                        else -> {
                            Toast.makeText(
                                this.context,
                                getString(R.string.error_en_el_tipus_de_usuari),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        } else if (parent == spinnerUserType) {

            if (position == 3) {
                binding.txtPAdministradorModificarUsuariEmail.visibility = View.INVISIBLE
                binding.dtTxtPAdministradorModificarUsuariEmail.visibility = View.INVISIBLE
                binding.dtTxtPAdministradorModificarUsuariPasswordL.visibility = View.INVISIBLE
            } else {
                db.collection("users").document(arrUserId[spinnerPosition]).get()
                    .addOnSuccessListener { document ->
                        val nom =
                            binding.dtTxtPAdministradorModificarUsuariPersonName.text.toString()
                        val cognom =
                            binding.dtTxtPAdministradorModificarUsuariPersonSurname.text.toString()
                        if (document.get("usertype") == "client" && position != 3) {
                            val builder =
                                androidx.appcompat.app.AlertDialog.Builder(this.requireContext())
                            builder.setTitle(getString(R.string.atencio))
                            builder.setMessage(
                                getString(
                                    R.string.si_vols_Que_l_usuari_deixi_Se_ser_client1) + nom +
                                    cognom + getString(R.string.si_vols_Que_l_usuari_deixi_Se_ser_client2)

                            )
                            builder.setPositiveButton(getString(R.string.si)) { _, _ ->
                                view?.findNavController()?.navigate(
                                    PantallaAdministradorModificarUsuariDirections.actionPantallaAdministradorModificarUsuariToPantallaAdministradorNouUsuari(
                                        arrUserType
                                    )
                                )
                            }
                            builder.setNegativeButton(getString(R.string.no)) { _, _ ->
                                spinnerUserType.setSelection(3)
                            }
                            val dialog: androidx.appcompat.app.AlertDialog = builder.create()
                            dialog.show()
                        } else if (document.get("usertype") == "client") {
                            binding.txtPAdministradorModificarUsuariEmail.visibility =
                                View.INVISIBLE
                            binding.dtTxtPAdministradorModificarUsuariEmail.visibility =
                                View.INVISIBLE
                            binding.dtTxtPAdministradorModificarUsuariPasswordL.visibility =
                                View.INVISIBLE
                            /*binding.dtTxtPAdministradorModificarUsuariEmail.text = ""
                            binding.dtTxtPAdministradorModificarUsuariPassword.setText("")
                            binding.dtTxtPAdministradorModificarUsuariEmail.isEnabled = true
                            binding.dtTxtPAdministradorModificarUsuariEmail.inputType*/
                        } else {
                            binding.txtPAdministradorModificarUsuariEmail.visibility = View.VISIBLE
                            binding.dtTxtPAdministradorModificarUsuariEmail.visibility =
                                View.VISIBLE
                            binding.dtTxtPAdministradorModificarUsuariPasswordL.visibility =
                                View.VISIBLE
                        }
                    }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        utils().showAlert(
            getString(R.string.error),
            getString(R.string.has_de_seleccionar_un_tipus_d_usuari),
            this.context
        )
    }


    private fun showAlert(message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this.requireContext())
        builder.setTitle(getString(R.string.error))
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.acceptar), null)
        val dialog: androidx.appcompat.app.AlertDialog = builder.create()
        dialog.show()
    }

    private fun deleteUser(dni: String) {
        db.collection("users").document(dni).get().addOnSuccessListener { result ->
            auth.signInWithEmailAndPassword(
                result.get("email").toString(),
                result.get("password").toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.delete()
                        ?.addOnCompleteListener {
                            db.collection("users").document(dni).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this.context,
                                        getString(R.string.usuari_eliminat_correctament),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    view?.findNavController()
                                        ?.navigate(
                                            PantallaAdministradorModificarUsuariDirections.actionPantallaAdministradorModificarUsuariToPantallaAdministradorPrincipal()
                                        )
                                }
                                .addOnFailureListener {
                                    showAlert(getString(R.string.usuari_no_s_ha_pogut_eliminar))
                                }
                        }

                } else {
                    showAlert(getString(R.string.error_en_intentar_eliminar_usuari))
                }


            }
        }

    }

    private fun saveUser(usertype: String) {

        val dni = binding.dtTxtPAdministradorModificarUsuariDtDni.text.toString()
        val username = binding.dtTxtPAdministradorModificarUsuariPersonName.text.toString()
        val usersurname = binding.dtTxtPAdministradorModificarUsuariPersonSurname.text.toString()
        val email = binding.dtTxtPAdministradorModificarUsuariEmail.text.toString()
        val passwd = binding.dtTxtPAdministradorModificarUsuariPassword.text.toString() + "prodis"
        db.collection("users").document(dni).get()
            .addOnSuccessListener { document ->
                //Si ha canviat i es de tipus Client
                if (usertype == "client") {
                    //El nou tipus d'usuari és client
                    db.collection("users").document(dni).update(
                        hashMapOf(
                            "username" to username,
                            "usersurname" to usersurname,
                            "usertype" to usertype
                        ) as Map<String, Any>
                    )
                } else if (usertype == "admin") {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(this.requireContext())
                    builder.setTitle("¡¡¡AVIS!!!")
                    builder.setMessage(
                        getString(
                            R.string.estas_a_punt_assignar_usuari_com_administrador1)+ username + " " +
                            usersurname + getString(R.string.estas_a_punt_assignar_usuari_com_administrador2)

                    )
                    builder.setPositiveButton(getString(R.string.si)) { _, _ ->
                        //El nou tipus d'usuari ha canbiat, ara és de tipus Administrador
                        val password = document.get("password")
                        if (password != passwd) {
                            changePassword(dni, passwd)
                            db.collection("users").document(dni).update(
                                hashMapOf(
                                    "email" to email,
                                    "username" to username,
                                    "usersurname" to usersurname,
                                    "usertype" to usertype
                                ) as Map<String, Any>
                            )
                        } else {
                            db.collection("users").document(dni).update(
                                hashMapOf(
                                    "email" to email,
                                    "password" to passwd,
                                    "username" to username,
                                    "usersurname" to usersurname,
                                    "usertype" to usertype
                                ) as Map<String, Any>
                            )
                        }
                    }
                    builder.setNegativeButton(getString(R.string.no), null)
                } else {
                    //El nou tipus d'usuari ha canbiat, NO és client
                    val password = document.get("password")
                    if (password != passwd) {
                        changePassword(dni, passwd)
                        db.collection("users").document(dni).update(
                            hashMapOf(
                                "email" to email,
                                "username" to username,
                                "usersurname" to usersurname,
                                "usertype" to usertype
                            ) as Map<String, Any>
                        )
                    } else {
                        db.collection("users").document(dni).update(
                            hashMapOf(
                                "email" to email,
                                "password" to passwd,
                                "username" to username,
                                "usersurname" to usersurname,
                                "usertype" to usertype
                            ) as Map<String, Any>
                        )
                    }

                }

            }
    }

    private fun changePassword(dni: String, psswd: String) {

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
                } else {
                    showAlert(getString(R.string.error_en_canviar_la_contrasenya))
                }
            }
        }
            .addOnFailureListener {
                showAlert(getString(R.string.l_usuari_no_esta_registrat))
            }

    }

    private fun deleteUserI(dni: String): Boolean {
        var correct = false
        db.collection("users").document(dni).delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this.context,
                    "S\'ha fet el canvi de dni",
                    Toast.LENGTH_SHORT
                ).show()
                correct = true
            }
            .addOnFailureListener {
                showAlert("No s'ha pogut fer el canvi de dni")
                correct = false

            }

        return correct
    }

    private fun makeregister(
        nom: String,
        cognom: String,
        dni: String,
        email: String,
        password: String,
        usertype: String
    ) {
        val passwd = password + "prodis"

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, passwd)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    db.collection("users").document(dni).set(
                        hashMapOf(
                            "username" to nom,
                            "usersurname" to cognom,
                            "dni" to dni,
                            "email" to email,
                            "password" to passwd,
                            "usertype" to usertype
                        )
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this.context,
                                "T\'has registrat correctament",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showAlert("Error a l\'hora de fer el guardat de dades")
                        }
                    }

                } else {
                    showAlert("Error a l\'hora de fer l\'autenticació")
                }
            }
    }

}