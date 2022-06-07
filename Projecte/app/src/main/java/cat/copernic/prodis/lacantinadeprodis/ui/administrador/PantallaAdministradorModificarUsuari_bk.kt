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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaAdministradorModificarUsuariBinding
import cat.copernic.prodis.lacantinadeprodis.utils.utils
import cat.copernic.prodis.lacantinadeprodis.viewmodel.PantallaAdministradorModificarUsuariVM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class PantallaAdministradorModificarUsuari_bk : Fragment(), AdapterView.OnItemSelectedListener {

    private val db = Firebase.firestore
    private var auth = FirebaseAuth.getInstance()

    /* private var arrUser = ArrayList<String>()
    private var arrUserId = ArrayList<String>()
    private var arrUserType = ArrayList<String>()*/

    private lateinit var vwMdl: PantallaAdministradorModificarUsuariVM

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
        /* arrUser = args.userArr as ArrayList<String>
        arrUserId = args.userArrId as ArrayList<String>
        arrUserType = args.usertypeArr as ArrayList<String>*/

        vwMdl = ViewModelProvider(this.requireActivity())[PantallaAdministradorModificarUsuariVM::class.java]

        spinner = binding.spinUsuariModificar

        val context = this.requireContext()

        /* adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item, arrUser
        )*/
        vwMdl.getArrUser.observe(this.requireActivity(), Observer { arrUser ->
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item, arrUser
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.onItemSelectedListener = this
        })



        spinnerUserType = binding.spinTipusUsuari

        /*adapterUType = ArrayAdapter(
            context, android.R.layout.simple_spinner_item, arrUserType
        )*/
        vwMdl.getArrUserType.observe(this.requireActivity(), Observer {
            adapterUType = ArrayAdapter(
                context, android.R.layout.simple_spinner_item, it as ArrayList<String>
            )
            adapterUType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerUserType.adapter = adapterUType
            spinnerUserType.onItemSelectedListener = this
        })




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
            builder.setTitle("¡¡¡AVIS!!!")
            builder.setMessage("Estas segur que vols eliminar a l'usuari: $user")
            builder.setPositiveButton("Acceptar") { _, _ ->
                deleteUser(dni)
            }
            builder.setNegativeButton("Cancelar", null)
            val dialog: androidx.appcompat.app.AlertDialog = builder.create()
            dialog.show()
        }


        return binding.root
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        if (parent == spinner) {

            vwMdl.getUsername.observe(viewLifecycleOwner, Observer {
                binding.dtTxtPAdministradorModificarUsuariPersonName.setText(
                    it[position]
                )
            })

            vwMdl.getUserSurname.observe(viewLifecycleOwner, Observer {
                binding.dtTxtPAdministradorModificarUsuariPersonSurname.setText(
                    it[position]
                )
            })

            vwMdl.getDni.observe(viewLifecycleOwner, Observer {
                binding.dtTxtPAdministradorModificarUsuariDtDni.setText(
                    it[position]
                )
            })

            vwMdl.getEmail.observe(viewLifecycleOwner, Observer {
                if (it[position] != "null") {
                    binding.txtPAdministradorModificarUsuariEmail.visibility = View.VISIBLE
                    binding.dtTxtPAdministradorModificarUsuariEmail.visibility = View.VISIBLE
                    binding.dtTxtPAdministradorModificarUsuariEmail.setText(
                        it[position]
                    )
                } else {
                    binding.txtPAdministradorModificarUsuariEmail.visibility = View.INVISIBLE
                    binding.dtTxtPAdministradorModificarUsuariEmail.visibility = View.INVISIBLE

                }
            })

            vwMdl.getPassword.observe(viewLifecycleOwner, Observer {
                if (it[position] == "null") {
                    binding.dtTxtPAdministradorModificarUsuariPasswordL.visibility =
                        View.INVISIBLE

                } else {
                    binding.dtTxtPAdministradorModificarUsuariPasswordL.visibility =
                        View.VISIBLE
                    binding.dtTxtPAdministradorModificarUsuariPassword.setText(it[position])
                }
            })

            vwMdl.getUsertype.observe(viewLifecycleOwner, Observer {

                if (it[position] == "null") {
                    spinnerUserType.visibility = View.INVISIBLE
                    binding.txtPAdministradorModificarUsuariUsertype.visibility = View.INVISIBLE
                } else {
                    spinnerUserType.visibility = View.VISIBLE
                    binding.txtPAdministradorModificarUsuariUsertype.visibility = View.VISIBLE
                }

                when (it[position]) {
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
                            "ERROR EN EL TIPUS DE USUARI",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            })




            /*db.collection("users").document(arrUserId[position]).get()
                    .addOnSuccessListener { document ->
                        val pasword = document.get("password").toString()
                        println("PASSWORD  = $pasword")
                        val pswd = pasword.replace("prodis", "")
                        println("PSWD = $pswd")

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
                                    "ERROR EN EL TIPUS DE USUARI",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }*/
        } else if (parent == spinnerUserType) {
            if (position == 3) {
                binding.txtPAdministradorModificarUsuariEmail.visibility = View.INVISIBLE
                binding.dtTxtPAdministradorModificarUsuariEmail.visibility = View.INVISIBLE
                binding.dtTxtPAdministradorModificarUsuariPasswordL.visibility = View.INVISIBLE
            } else {
                vwMdl.getArrUserId.observe(viewLifecycleOwner, Observer {
                    val nom =
                        binding.dtTxtPAdministradorModificarUsuariPersonName.text.toString()
                    val cognom =
                        binding.dtTxtPAdministradorModificarUsuariPersonSurname.text.toString()
                    if (it[position] == "client" && position != 3){
                        vwMdl.getArrUserType.observe(viewLifecycleOwner, Observer {
                            val builder =
                                androidx.appcompat.app.AlertDialog.Builder(this.requireContext())
                            builder.setTitle("¡¡¡ATENCIÓ!!!")
                            builder.setMessage(
                                "Si vols que l'usuari: $nom $cognom deixi de ser client, " +
                                        "l'has de tornar a afegir desde l'apartat \"Afegir nou usuari\".\n" +
                                        "Vols anar a \"Afegir nou usuari\" per canviar el tipus d'usuari?"
                            )
                            builder.setPositiveButton("Si") { _, _ ->
                                view?.findNavController()?.navigate(
                                    PantallaAdministradorModificarUsuariDirections.actionPantallaAdministradorModificarUsuariToPantallaAdministradorNouUsuari(
                                        it as ArrayList<String>
                                    )
                                )
                            }
                            builder.setNegativeButton("No") { _, _ ->
                                spinnerUserType.setSelection(3)
                            }
                            val dialog: androidx.appcompat.app.AlertDialog = builder.create()
                            dialog.show()
                        })
                    }else if (it[position] == "client"){
                        binding.txtPAdministradorModificarUsuariEmail.visibility =
                            View.INVISIBLE
                        binding.dtTxtPAdministradorModificarUsuariEmail.visibility =
                            View.INVISIBLE
                        binding.dtTxtPAdministradorModificarUsuariPasswordL.visibility =
                            View.INVISIBLE
                    }else{
                        binding.txtPAdministradorModificarUsuariEmail.visibility = View.VISIBLE
                        binding.dtTxtPAdministradorModificarUsuariEmail.visibility =
                            View.VISIBLE
                        binding.dtTxtPAdministradorModificarUsuariPasswordL.visibility =
                            View.VISIBLE
                    }
                })
                /* db.collection("users").document(arrUserId[position]).get()
                         .addOnSuccessListener { document ->
                             val nom =
                                 binding.dtTxtPAdministradorModificarUsuariPersonName.text.toString()
                             val cognom =
                                 binding.dtTxtPAdministradorModificarUsuariPersonSurname.text.toString()
                             if (document.get("usertype") == "client" && position != 3) {
                                 vwMdl.getArrUserType.observe(viewLifecycleOwner, Observer {
                                     val builder =
                                         androidx.appcompat.app.AlertDialog.Builder(this.requireContext())
                                     builder.setTitle("¡¡¡ATENCIÓ!!!")
                                     builder.setMessage(
                                         "Si vols que l'usuari: $nom $cognom deixi de ser client, " +
                                                 "l'has de tornar a afegir desde l'apartat \"Afegir nou usuari\".\n" +
                                                 "Vols anar a \"Afegir nou usuari\" per canviar el tipus d'usuari?"
                                     )
                                     builder.setPositiveButton("Si") { _, _ ->
                                         view?.findNavController()?.navigate(
                                             PantallaAdministradorModificarUsuariDirections.actionPantallaAdministradorModificarUsuariToPantallaAdministradorNouUsuari(
                                                 it as ArrayList<String>
                                             )
                                         )
                                     }
                                     builder.setNegativeButton("No") { _, _ ->
                                         spinnerUserType.setSelection(3)
                                     }
                                     val dialog: androidx.appcompat.app.AlertDialog = builder.create()
                                     dialog.show()
                                 })
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
                         }*/
            }
        }
    }


    override fun onNothingSelected(parent: AdapterView<*>) {
        utils().showAlert("ERROR", "Has de seleccionar un tipus d\'usuari", this.context)
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
                                        "Usuari eliminat correctament",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    view?.findNavController()
                                        ?.navigate(
                                            PantallaAdministradorModificarUsuariDirections.actionPantallaAdministradorModificarUsuariToPantallaAdministradorPrincipal()
                                        )
                                }
                                .addOnFailureListener {
                                    utils().showAlert(
                                        "ERROR",
                                        "L'usuari no s'ha pogut eliminar",
                                        this.context
                                    )
                                }
                        }

                } else {
                    utils().showAlert(
                        "ERROR",
                        "Hi ha hagut un error en intentar eliminar a l'usuari",
                        this.context
                    )
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
                        "Estas a punt d\'asignar l'usuari $username $usersurname com a administrador\n"
                                + "Estàs segur d'aquesta acció?"
                    )
                    builder.setPositiveButton("Si") { _, _ ->
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
                    builder.setNegativeButton("No", null)
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
                            "S'ha canbiat la contrasenya",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    val pas = hashMapOf("password" to psswd)
                    db.collection("users").document(dni).update(
                        pas as Map<String, Any>
                    )
                } else {
                    utils().showAlert("ERROR", "Error en canviar la contrasenya", this.context)
                }
            }
        }
            .addOnFailureListener {
                utils().showAlert("ERROR", "L\'usuari no està registrat", this.context)
            }

    }
}
