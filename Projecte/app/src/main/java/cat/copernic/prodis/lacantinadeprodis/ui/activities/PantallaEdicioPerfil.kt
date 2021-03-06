package cat.copernic.prodis.lacantinadeprodis.ui.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaEdicioPerfilBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import androidx.core.view.isGone
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cat.copernic.prodis.lacantinadeprodis.viewmodel.PantallaEdicioPerfilViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import cat.copernic.prodis.lacantinadeprodis.MainActivity
import cat.copernic.prodis.lacantinadeprodis.utils.utils
import java.util.*

class PantallaEdicioPerfil : AppCompatActivity(), LifecycleOwner {

    //Definim les variables globals
    private var dni = ""

    private val db = FirebaseFirestore.getInstance()

    lateinit var binding: FragmentPantallaEdicioPerfilBinding
    private var latestTmpUri: Uri? = null
    val takeImageResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    binding.userIcon.setImageURI(uri)
                }
            }
        }

    private val NOTIFICATION_ID = 0

    private lateinit var notificationChannel: NotificationChannel
    private lateinit var build: Notification.Builder

    lateinit var storageRef: StorageReference

    private lateinit var viewModel: PantallaEdicioPerfilViewModel

    private var idiomaR = ""

    //Comenn??a el onCreate
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<FragmentPantallaEdicioPerfilBinding>(
            this,
            R.layout.fragment_pantalla_edicio_perfil
        )

        title = "Edici?? Perfil"

        if(utils().isTabablet(applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        //Fem que al pr??mer el bot?? de per cambiar la foto cridi a la funci?? per triar si volem agafar la foto desde la c??mera o desde la galeria
        binding.btnCambiarFoto.setOnClickListener() { view: View ->
            triaCamGaleria()
        }

        //Declrem el view model
        viewModel = ViewModelProvider(this)[PantallaEdicioPerfilViewModel::class.java]

        viewModel.dni.observe(this, Observer {
            dni = it
            //Cridem a la funci?? per agafar l'imatge del usuari
            agafarImatgeUsuari(dni)

        })


        //Fem que desde el view model s'obvervi els canvis del camp de text del nom i del cognom
        viewModel.nom.observe(this, Observer {
            binding.editTxtNom.setText(it.toString())

        })

        viewModel.cognom.observe(this, Observer {
            binding.editTxtCognom.setText(it.toString())

        })

        //Cridem a la funci?? per guardar les dades
        guardarDades()

        //Funci?? per saber quin idioma ha sigut seleccionat
        seleccionaIdioma()

        supportActionBar?.title = ""
    }

    //Aquesta funci?? far?? que es comprovi si hi han dades en els camps indicats
    private fun datavalids(nom: String, cognom: String): Boolean {
        var error = ""
        var bool = true
        if (nom.isEmpty()) {
            error += getString(R.string.has_d_introduir_el_nom) + "\r"
            bool = false
        }
        if (cognom.isEmpty()) {
            error += getString(R.string.has_d_introduir_el_cognom) + "\r"
            bool = false
        }
        if (error != "") {
            showAlert(error)
        }
        return bool
    }

    //Aquesta funci?? mirar?? si el email t?? el format correcte
    private fun checkEmailFormat(email: String): Boolean {
        val EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    //Aquesta funci?? crea un alert amb els errors de la funci?? dataValids
    private fun showAlert(message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.error))
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.acceptar), null)
        val dialog: androidx.appcompat.app.AlertDialog = builder.create()
        dialog.show()
    }

    //Aquesta f?? que l'imatge seleccionada en la galeria sigui la foto del usuari
    private val startForActivityGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.data
            //setImageUri nom??s funciona per rutes locals, no a internet
            binding.userIcon.setImageURI(data)
        }
    }

    //Aquesta funci?? fa que s'obri la galeria
    private fun obrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startForActivityGallery.launch(intent)
    }

    //Funci?? que fa que s'obri la camera
    private fun obrirCamera() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri

                takeImageResult.launch(uri)
            }
        }
    }

    //Amb aquesta funci?? agafarem l'Uri de l'imatge
    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            applicationContext,
            "cat.copernic.prodis.lacantinadeprodis.provider",
            tmpFile
        )
    }

    //Es crea un dialog amb dos botons per seleccionar d'on ses vol triar l'imatge
    fun triaCamGaleria() {

        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(getString(R.string.d_on_vols_treure_la_foto))
        alertDialog.setMessage(getString(R.string.selecciona_un))

        //Indiquem al bot?? positiu que ser?? el que obrir?? la galeria
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, getString(R.string.galeria)
        ) { dialog, which -> obrirGaleria() }

        //Indiquem al bot?? negatiu que ser?? el que obrir?? la c??mera
        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, getString(R.string.camara)
        ) { dialog, which -> obrirCamera() }
        alertDialog.show()


        //Incialitzem i posem els botons dins del alert
        val btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        val layoutParams = btnPositive.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        btnPositive.layoutParams = layoutParams
        btnNegative.layoutParams = layoutParams
    }

    fun pujarImatge(view: View) {
        // pujar imatge al Cloud Storage de Firebase
        // https://firebase.google.com/docs/storage/android/upload-files?hl=es

        // Creem una refer??ncia amb el path i el nom de la imatge per pujar la imatge
        val pathReference = storageRef.child("users/" + dni + ".jpg")


        val bitmap =
            (binding.userIcon.drawable as BitmapDrawable).bitmap // agafem la imatge del imageView
        val baos = ByteArrayOutputStream() // declarem i inicialitzem un outputstream

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            baos
        ) // convertim el bitmap en outputstream
        val data = baos.toByteArray() //convertim el outputstream en array de bytes.

        val uploadTask = pathReference.putBytes(data)
        uploadTask.addOnFailureListener {
            Snackbar.make(
                view,
                getString(R.string.error_al_pujar_la_foto),
                Snackbar.LENGTH_LONG
            )
                .show()
            it.printStackTrace()

        }.addOnSuccessListener {
            Snackbar.make(view, getString(R.string.exit_al_pujar_la_foto), Snackbar.LENGTH_LONG)
                .show()
        }
    }

    //Aquesta funci?? agafar?? l'imatge del usuari desde la base de dades
    private fun agafarImatgeUsuari(dni: String) {
        //Declarem la referencia del fire strorage
        storageRef = FirebaseStorage.getInstance().getReference()

        //Creem una referencia a l'imatge del usuari
        var imgRef = Firebase.storage.reference.child("users/" + dni + ".jpg")

        //Agafem l'imatge i la posem en l'imatge del usuari
        imgRef.downloadUrl.addOnSuccessListener { Uri ->
            val imgUrl = Uri.toString()

            Glide.with(this).load(imgUrl).into(binding.userIcon)
        }

    }

    //Funci?? per guardar les dades en la base de dades
    private fun guardarDades() {
        //Escoltem al bot?? de guardar
        binding.btnGuardar.setOnClickListener() { view: View ->
            //Cridem a la funci?? per pujar la foto del usuari
            if (binding.userIcon.drawable.constantState != ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_baseline_account_circle_24
                )?.constantState
            ) {
                pujarImatge(view)
            }
            //Comprovem si les dades del nom i del cognom son correctes
            if (datavalids(
                    binding.editTxtNom.text.toString(),
                    binding.editTxtCognom.text.toString()
                )
            ) {
                //Anem a l acolecci?? d'usuaris i cambiem les dades del usuari on el dni es el que agafem per parametres
                db.collection("users").document(dni).update(
                    hashMapOf(
                        "username" to binding.editTxtNom.text.toString(),
                        "usersurname" to binding.editTxtCognom.text.toString(),
                    ) as Map<String, Any>
                )

                //Quan acaba d'actualizar les dades surt un toast indicant que els canvis s'han fet amb ??xit
                Toast.makeText(this, getString(R.string.canvis_amb_exit), Toast.LENGTH_SHORT)
                    .show()

            }

            //Cridem a la funci?? createChannel per crear un canal per poder enviar una notificaci?? en el cas de que l'api sigui major a 26
            createChannel(
                //Agagem del fitxer de strings un id i un nom per el nostre canal
                getString(R.string.channel_id),
                getString(R.string.channel_name)
            )

            //Definim i inicialitzem una variable per pasarli el NotificationManager
            val notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            ) as NotificationManager

            //Indiquem que notificationManager envi?? una notificaci?? amb un text que agafara del fitxer de strings i en aquest context
            notificationManager.sendNotification(
                this.getString(R.string.enhorabona_canvis),
                this
            )

            //Cridem a la funci?? posaIdioma
            posaIdioma()
        }
    }


    //Funci?? per el Notifaction manager que tindr?? per parametres el missatge de la notificaci?? i el context de l'app
    private fun NotificationManager.sendNotification(
        messageBody: String,
        applicationContext: Context
    ) {

        //Builder per crear la notificaci?? m??s tard
        val builder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.channel_id)
        )
            //Indiquem quin ser?? l'icona que sortir?? en la notificaci??
            .setSmallIcon(R.drawable.logo_foreground)
            //Indiquem quin ser?? el text principal de la notificaci??
            .setContentTitle(
                applicationContext
                    .getString(R.string.els_canvis_s_han_fet_amb_exit)
            )
            //Aquest ser?? el text de la notificiac??
            .setContentText(messageBody)

        //Creem la notificaci?? amb un id i amb el builder que hem creat abans
        notify(NOTIFICATION_ID, builder.build())


    }

    //Funci?? per crear el canal que tindr?? in channelId i un channelName
    private fun createChannel(channelId: String, channelName: String) {
        //Fem un if per comprobar si ela versi?? del sdk es correcte
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Indiquem que el canal de la notificaci?? sera de tipus NotificationChannel agafant els valors de channelId, de channelName i agafant l'importancia de la
            // notificaci??
            notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )

            //Indiquem que s'activi la llum del nostre dispositiu al rebre la notificaci??
            notificationChannel.enableLights(true)
            //Indiquem el color de la llum del nostre dispositiu, en aquest cas ser?? blanc
            notificationChannel.lightColor = Color.WHITE
            //Indiquem que volem que el nostre dispositiu vibri al rebre la notificaci??
            notificationChannel.enableVibration(true)
            //Indiquem la descripci?? de la notificaci??
            notificationChannel.description = getString(R.string.descripcio_notificacio)

            //Definim i inicialitzem una variable notificationManager que ser?? de tipus NotificationManager
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )

            //Amb la variable que acabem de crear li indicarem que cre?? un canal amb els par??metres que li hem indicat abans
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    //Funci?? per canviar l'idioma
    private fun idioma(lenguage: String, country: String) {
        val localitzacio = Locale(lenguage, country)

        Locale.setDefault(localitzacio)

        var config = Configuration()

        config.locale = localitzacio
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    //Funci?? per saber quin idioma s'ha seleccionat
    private fun seleccionaIdioma() {
        //Escoltem el radioGroup de l'idioma
        binding.radioGroup
            .setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    //Fem que si el radioCat est?? marcat el valor d'idiomaR ser?? "cat"
                    R.id.radioCat -> {
                        idiomaR = "cat"
                    }
                    //Fem que si el radioEsp est?? marcat el valor d'idiomaR ser?? "esp"
                    R.id.radioEsp -> {
                        idiomaR = "esp"
                    }
                    //Fem que si el radioEng est?? marcat el valor d'idiomaR ser?? "eng"
                    R.id.radioEng -> {
                        idiomaR = "eng"
                    }
                }
            }
    }

    //Funci?? per posar l'idioma al que es canviar??
    private fun posaIdioma() {
        //Si el valor de idiomaR es "cat" el valors que es pasaran per canviar d'idomoa ser??n "ca" i "ES"
        if (idiomaR == "cat") {
            idioma("ca", "ES")
            val intent = Intent(this, PantallaEdicioPerfil::class.java).apply {
            }
            finish()
            startActivity(intent)
            //Si el valor de idiomaR es "es" el valors que es pasaran per canviar d'idomoa ser??n "es" i "ES"
        } else if (idiomaR == "esp") {
            idioma("es", "ES")
            val intent = Intent(this, PantallaEdicioPerfil::class.java).apply {
            }
            finish()
            startActivity(intent)
            //Si el valor de idiomaR es "eng" el valors que es pasaran per canviar d'idomoa ser??n "eng" i ""
        } else if (idiomaR == "eng") {
            idioma("en", "")
            val intent = Intent(this, PantallaEdicioPerfil::class.java).apply {
            }
            finish()
            startActivity(intent)
        }
    }
}