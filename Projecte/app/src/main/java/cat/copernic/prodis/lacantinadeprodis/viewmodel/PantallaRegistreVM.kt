package cat.copernic.prodis.lacantinadeprodis.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cat.copernic.prodis.lacantinadeprodis.R

class PantallaRegistreVM(application: Application): AndroidViewModel(application) {

    private val _txtPRegistre = MutableLiveData<String>()
    val txtPRegistre: LiveData<String>
    get() = _txtPRegistre

    private val _textPRegistre = MutableLiveData<String>()
    val textPRegistre: LiveData<String>
    get() = _textPRegistre

    private val _dtTxtPRegistrePersonSurname = MutableLiveData<String>()
    val dtTxtPRegistrePersonSurname: LiveData<String>
    get() = _dtTxtPRegistrePersonSurname

    private val _dtTxtPRegistrePersonName = MutableLiveData<String>()
    val dtTxtPRegistrePersonName: LiveData<String>
    get() = _dtTxtPRegistrePersonName

    private val _textPRegistreSurname = MutableLiveData<String>()
    val textPRegistreSurname: LiveData<String>
    get() = _textPRegistreSurname

    private val _txtPRegistreDni = MutableLiveData<String>()
    val txtPRegistreDni: LiveData<String>
    get() = _txtPRegistreDni

    private val _dtTxtPRegistreDni = MutableLiveData<String>()
    val dtTxtPRegistreDni: LiveData<String>
    get() = _dtTxtPRegistreDni

    private val _txtPRegistreEmail = MutableLiveData<String>()
    val txtPRegistreEmail: LiveData<String>
    get() = _txtPRegistreEmail

    private val _dtTxtPRegistreEmail = MutableLiveData<String>()
    val dtTxtPRegistreEmail: LiveData<String>
    get() = _dtTxtPRegistreEmail

    private val _txtPRegistreContrasenya = MutableLiveData<String>()
    val txtPRegistreContrasenya: LiveData<String>
    get() = _txtPRegistreContrasenya

    private val _dtTxtPRegistrePassword = MutableLiveData<String>()
    val dtTxtPRegistrePassword: LiveData<String>
    get() = _dtTxtPRegistrePassword

    private val _dtTxtPRegistreRepeteixPassword = MutableLiveData<String>()
    val dtTxtPRegistreRepeteixPassword: LiveData<String>
    get() = _dtTxtPRegistreRepeteixPassword

    private val _checkBox = MutableLiveData<String>()
    val checkBox: LiveData<String>
    get() = _checkBox

    private val _btnPregistre = MutableLiveData<String>()
    val btnPregistre: LiveData<String>
    get() = _btnPregistre

    private val _textPregistre_tensUnCompte = MutableLiveData<String>()
    val textPregistre_tensUnCompte: LiveData<String>
    get() = _textPregistre_tensUnCompte

    private val _textPregistre_iniciaSessio = MutableLiveData<String>()
    val textPregistre_iniciaSessio: LiveData<String>
    get() = _textPregistre_iniciaSessio


    init {
        _txtPRegistre.value = application.applicationContext.getString(R.string.registrat)
        _textPRegistre.value = application.applicationContext.getString(R.string.nom)
        _dtTxtPRegistrePersonName.value = application.applicationContext.getString(R.string.juan)
        _dtTxtPRegistrePersonSurname.value = application.applicationContext.getString(R.string.rodriguez_gonz_lez)
        _textPRegistreSurname.value = application.applicationContext.getString(R.string.cognoms)
        _txtPRegistreDni.value = application.applicationContext.getString(R.string.dni)
        _dtTxtPRegistreDni.value = application.applicationContext.getString(R.string.editDni)
        _txtPRegistreEmail.value = application.applicationContext.getString(R.string.correu_electronic)
        _dtTxtPRegistreEmail.value = application.applicationContext.getString(R.string.jrodirezgonzalez_prodiscat)
        _txtPRegistreContrasenya.value = application.applicationContext.getString(R.string.contrasenya)
        _dtTxtPRegistrePassword.value = application.applicationContext.getString(R.string.contrasenya)
        _dtTxtPRegistreRepeteixPassword.value = application.applicationContext.getString(R.string.contrasenya)
        _checkBox.value = application.applicationContext.getString(R.string.condicions_servei_privacitat)
        _btnPregistre.value = application.applicationContext.getString(R.string.continuar)
        _textPregistre_tensUnCompte.value = application.applicationContext.getString(R.string.tens_un_compte)
        _textPregistre_iniciaSessio.value = application.applicationContext.getString(R.string.iniciarSessio)

    }
}