package cat.copernic.prodis.lacantinadeprodis.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

class PantallaAdministradorModificarUsuariVM() : ViewModel() {

    private val db = Firebase.firestore

    private var arrUser = MutableLiveData<ArrayList<String>>() //java.util.ArrayList<String>()
    val getArrUser: LiveData<ArrayList<String>>
        get() = arrUser

    private var arrUserId = MutableLiveData<ArrayList<String>>()
    val getArrUserId: LiveData<java.util.ArrayList<String>>
        get() = arrUserId

    private var arrUsertype = MutableLiveData<ArrayList<String>>()
    val getArrUserType: LiveData<java.util.ArrayList<String>>
        get() = arrUsertype


    private val usertype = MutableLiveData<ArrayList<String>>()
    val getUsertype: LiveData<java.util.ArrayList<String>>
        get() = usertype

    private val username = MutableLiveData<ArrayList<String>>()
    val getUsername: LiveData<java.util.ArrayList<String>>
        get() = username

    val usersurname = MutableLiveData<ArrayList<String>>()
    val getUserSurname: LiveData<java.util.ArrayList<String>>
        get() = usersurname

    val dni = MutableLiveData<ArrayList<String>>()
    val getDni: LiveData<java.util.ArrayList<String>>
        get() = dni

    val email = MutableLiveData<ArrayList<String>>()
    val getEmail: LiveData<java.util.ArrayList<String>>
        get() = email

    val pswd = MutableLiveData<ArrayList<String>>()
    val getPassword: LiveData<java.util.ArrayList<String>>
        get() = pswd


    init {
        // Log.i("ModificarUsuari-VM", "PantallaAdministradorModificarUsuariVM created!")

        /*loadArrUser()
        loadArrUsertype()*/

        val task1: Job = crearCorrutina()
        task1.let { }



    }

    @DelicateCoroutinesApi
    private fun crearCorrutina() = GlobalScope.launch(Dispatchers.Main) {
        withContext(Dispatchers.IO) {
            usertype.value?.clear()
            username.value?.clear()
            usersurname.value?.clear()
            dni.value?.clear()
            email.value?.clear()
            pswd.value?.clear()

            arrUser.value?.clear()
            arrUserId.value?.clear()
            arrUsertype.value?.clear()

            db.collection("users").get().addOnSuccessListener { result ->
                for (document in result) {
                    if (document.id != "usertypes") {
                        val user =
                            document.get("username").toString() + " " + document.get("usersurname")
                                .toString()
                        arrUser.value?.add(user)
                        arrUserId.value?.add(document.id)

                        dni.value?.add(document.get("dni").toString())
                        email.value?.add(document.get("email").toString())
                        pswd.value?.add(document.get("password").toString())
                        username.value?.add(document.get("username").toString())
                        usersurname.value?.add(document.get("usersurname").toString())
                        usertype.value?.add(document.get("usertype").toString())

                    } else if (document.id == "usertypes") {
                        val admin = document.get("admin").toString()
                        val caixer = document.get("caixer").toString()
                        val cambrer = document.get("cambrer").toString()
                        val client = document.get("client").toString()
                        val clientR = document.get("clientR").toString()
                        val cuiner = document.get("cuiner").toString()

                        arrUsertype.value?.add(admin)
                        arrUsertype.value?.add(caixer)
                        arrUsertype.value?.add(cambrer)
                        arrUsertype.value?.add(client)
                        arrUsertype.value?.add(clientR)
                        arrUsertype.value?.add(cuiner)
                    }
                }
            }

        }
    }

    /*private fun loadArrUser(){
        db.collection("users").get().addOnSuccessListener { result ->
            for (document in result) {
                if (document.id != "usertypes") {
                    val user =
                        document.get("username").toString() + " " + document.get("usersurname")
                            .toString()
                    arrUser.value?.add(user)
                    arrUserId.value?.add(document.id)
                }
            }
        }
    }

    private fun loadArrUsertype(){
        db.collection("users").document("usertypes").get().addOnSuccessListener { document ->
            val admin = document.get("admin").toString()
            val caixer = document.get("caixer").toString()
            val cambrer = document.get("cambrer").toString()
            val client = document.get("client").toString()
            val clientR = document.get("clientR").toString()
            val cuiner = document.get("cuiner").toString()

            arrUsertype.value?.add(admin)
            arrUsertype.value?.add(caixer)
            arrUsertype.value?.add(cambrer)
            arrUsertype.value?.add(client)
            arrUsertype.value?.add(clientR)
            arrUsertype.value?.add(cuiner)
        }
    }*/

}