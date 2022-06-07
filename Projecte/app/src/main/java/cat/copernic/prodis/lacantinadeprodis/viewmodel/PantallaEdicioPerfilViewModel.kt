package cat.copernic.prodis.lacantinadeprodis.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*



class PantallaEdicioPerfilViewModel() : ViewModel() {
    private val _dni = MutableLiveData<String>()
    val dni: LiveData<String>
        get() = _dni

    private val _nom = MutableLiveData<String>()
    val nom: LiveData<String>
        get() = _nom

    private val _cognom = MutableLiveData<String>()
    val cognom: LiveData<String>
        get() = _cognom

    init{
        var tasca1: Job? = crearCorrutina()

        tasca1.let {}
    }

    fun crearCorrutina()= GlobalScope.launch(Dispatchers.Main) {
        withContext(Dispatchers.IO){
            val db = FirebaseFirestore.getInstance()

            val currentUser =
                FirebaseAuth.getInstance().currentUser

            db.collection("users").get().addOnSuccessListener { result ->
                for (document in result) {
                    if (currentUser?.email.toString() == document.get("email").toString()) {
                        _dni.value = document.id
                        db.collection("users").document(dni.value.toString()).get()
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                    _nom.value = document.get("username").toString()
                                    _cognom.value = document.get("usersurname").toString()
                                }
                            }
                    }
                }
            }
        }
    }
}