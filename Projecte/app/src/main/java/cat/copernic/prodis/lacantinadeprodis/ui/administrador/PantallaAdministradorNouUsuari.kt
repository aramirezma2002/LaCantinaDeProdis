package cat.copernic.prodis.lacantinadeprodis.ui.administrador

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaAdministradorNouUsuariBinding
import cat.copernic.prodis.lacantinadeprodis.utils.utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList
import java.util.regex.Pattern

class PantallaAdministradorNouUsuari : Fragment(), AdapterView.OnItemSelectedListener {

    private val db = Firebase.firestore
    private var arrUserType = ArrayList<String>()
    private lateinit var binding: FragmentPantallaAdministradorNouUsuariBinding
    private lateinit var spinner: Spinner
    private lateinit var adapter: ArrayAdapter<*>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_administrador_nou_usuari, container, false
        )

        val args = PantallaAdministradorNouUsuariArgs.fromBundle(requireArguments())
        arrUserType = args.usertypeArr as ArrayList<String>

        val context = this.requireContext()

        spinner = binding.spnPAdministradorModificarUsusariTipusUsuari
        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, arrUserType)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        binding.btnPNouUsuariGurardar.setOnClickListener {
            var usertype = ""
            when (spinner.selectedItem.toString()) {
                "Administrador" -> usertype = "admin"
                "Caixer" -> usertype = "caixer"
                "Cambrer" -> usertype = "cambrer"
                "Client" -> usertype = "client"
                "Client Remot" -> usertype = "clientR"
                "Cuiner" -> usertype = "cuiner"
            }
            if (
                datavalids(
                    binding.dtTxtPAdministradorNouUsuariPersonName.text.toString()+"",
                    binding.dtTxtPAdministradorNouUsuariPersonSurname.text.toString()+"",
                    binding.dtTxtPAdministradorNouUsuariDni.text.toString()+"",
                    binding.dtTxtPAdministradorNouUsuariEmail2.text.toString()+"",
                    binding.dtTxtPAdministradorNouUsuariPassword.text.toString()+"",
                    usertype+""
                )
            ) {

                newUser(
                    binding.dtTxtPAdministradorNouUsuariPersonName.text.toString()+"",
                    binding.dtTxtPAdministradorNouUsuariPersonSurname.text.toString()+"",
                    binding.dtTxtPAdministradorNouUsuariDni.text.toString()+"",
                    binding.dtTxtPAdministradorNouUsuariEmail2.text.toString()+"",
                    binding.dtTxtPAdministradorNouUsuariPassword.text.toString()+"",
                    usertype
                )
            }
        }

        return binding.root
    }

    private fun newUser(
        nom: String,
        cognom: String,
        dni: String,
        email: String,
        password: String,
        usertype: String
    ) {
        val passwd = password + "prodis"

        db.collection("users").document(dni).get().addOnSuccessListener { document ->
            if (!document.exists()) {
                if (usertype == "admin") {
                    var bool = false

                    //Alert per confirmar que vol afegir un usuari administrador
                    val builder = AlertDialog.Builder(this.context)
                    builder.setTitle("AVÍS")
                    builder.setMessage(
                        "Estàs intentant afegir un nou usuari administrador \n" +
                                "Estàs segur?"
                    )
                    builder.setPositiveButton("Si", DialogInterface.OnClickListener { dialog, id ->
                       makeAdminRegister()
                    })
                    builder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
                        Toast.makeText(
                            this.context,
                            "Si no vols que aquest usuari sigui de tipus Administrador, canvia el tipus d\'usuari",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                    val dialog: AlertDialog = builder.create()
                    dialog.show()

                } else if (usertype == "client") {
                    db.collection("users").document(dni).set(
                        hashMapOf("username" to nom, "usersurname" to cognom, "dni" to dni)
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            view?.findNavController()
                                ?.navigate(PantallaAdministradorNouUsuariDirections.actionPantallaAdministradorNouUsuariToPantallaAdministradorPrincipal())
                            Toast.makeText(
                                this.context,
                                "S\'ha afegit correctament l\'usuari $nom $cognom",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            utils().showAlert("ERROR", "Error a l'hora de afegir el nou usuari",this.context)
                        }
                    }
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, passwd)
                        .addOnCompleteListener { it ->
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
                                            "S\'ha afegit correctament l\'usuari $nom $cognom",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        utils().showAlert("ERROR", "Error a l\'hora de fer el guardat de dades",this.context)
                                    }
                                }

                            } else {
                                utils().showAlert("ERROR", "Error a l\'hora de afegir el nou usuari",this.context)
                            }
                        }

                }

            } else {
                utils().showAlert("ERROR", "Aquest usuari ja existeix \n" +
                        "Si vols modifiar-lo has d\'anar a la secció modificar usuari",this.context)
            }
        }

    }

    private fun datavalids(
        nom: String,
        cognom: String,
        dni: String,
        email: String,
        password: String,
        usertype: String
    ): Boolean {
        var errorMessage = ""
        var bool = true

        if (nom.isEmpty()) {
            errorMessage += "Falta introduir el nom\n"
            bool = false
        }

        if (cognom.isEmpty()) {
            errorMessage += "Falta introduir els cognoms\n"
            bool = false
        }

        if (dni.isEmpty()) {
            errorMessage += "Falta introduir el dni\n"
            bool = false
        } else if (!checkDni(dni)) {
            errorMessage += "Format DNI incorrecte.\n"
            bool = false
        }
        if (usertype == "client"){
            if (email.isEmpty()) {
                errorMessage += "Falta introduir el correu electronic.\n"
                bool = false
            } else if (!checkEmailFormat(email)) {
                errorMessage += "Format correu electronic incorrecte.\n"
                bool = false
            }

            if (password.isEmpty()) {
                errorMessage += "Falta introduir la contrasenya.\n"
                bool = false
            }
        }

        if (errorMessage != "") utils().showAlert("ERROR", errorMessage,this.context)

        return bool

    }

    private fun checkDni(dni: String): Boolean {
        val dniNum = dni.substring(0, dni.length - 1)
        if (dni.isDigitsOnly()) {
            return false
        }

        val dniLletra = dni.substring(dni.length - 1).uppercase()
        val lletraDni = "TRWAGMYFPDXBNJZSQVHLCKE"

        return dniLletra == lletraDni[dniNum.toInt() % 23].toString()
    }

    private fun checkEmailFormat(email: String): Boolean {
        val EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    private fun makeAdminRegister(){
        val dni = binding.dtTxtPAdministradorNouUsuariDni.text.toString()
        val email = binding.dtTxtPAdministradorNouUsuariEmail2.text.toString()
        val password = binding.dtTxtPAdministradorNouUsuariPassword.text.toString()+"prodis"
        val username = binding.dtTxtPAdministradorNouUsuariPersonName.text.toString()
        val usersurname = binding.dtTxtPAdministradorNouUsuariPersonSurname.text.toString()
        val usertype = "admin"

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    db.collection("users").document(dni).set(
                        hashMapOf(
                            "username" to username,
                            "usersurname" to usersurname,
                            "dni" to dni,
                            "email" to email,
                            "password" to password,
                            "usertype" to usertype
                        )
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this.context,
                                "S\'ha afegit correctament l\'usuari $username $usersurname",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            utils().showAlert("ERROR", "Error a l\'hora de fer el guardat de dades",this.context)
                        }
                    }

                } else {
                    utils().showAlert("ERROR", "Error a l\'hora de afegir el nou usuari",this.context)
                }
            }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position == 3){
            binding.dtTxtPAdministradorNouUsuariEmail2.visibility = View.INVISIBLE
            binding.txtPAdministradorNouUsuariEmail2.visibility = View.INVISIBLE
            binding.txtPAdministradorNouUsuariContrasenya2.visibility = View.INVISIBLE
            binding.dtTxtPAdministradorNouUsuariPassword.visibility = View.INVISIBLE
            binding.dtTxtPAdministradorNouUsuariPasswordT.visibility = View.INVISIBLE
        }else{
            binding.dtTxtPAdministradorNouUsuariEmail2.visibility = View.VISIBLE
            binding.txtPAdministradorNouUsuariEmail2.visibility = View.VISIBLE
            binding.txtPAdministradorNouUsuariContrasenya2.visibility = View.VISIBLE
            binding.dtTxtPAdministradorNouUsuariPassword.visibility = View.VISIBLE
            binding.dtTxtPAdministradorNouUsuariPasswordT.visibility = View.VISIBLE
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }


}