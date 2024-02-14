package application.mobile.smarthouse
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.StorageReference
import java.io.File


object UserInfo {

    private lateinit var sharedPreferences: SharedPreferences
    var user_id : String = ""
    var name  : String = ""
    private var email : String = ""
    var profile_ph: String = ""
    var selected_home_id: String = ""
    var selected_home_name: String =""

    @SuppressLint("SuspiciousIndentation")
    fun initializationUser(context:Context, currentUser: FirebaseUser, callback: () -> Unit){

        sharedPreferences = context.getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)

        user_id = currentUser.uid.toString()
        name = currentUser.displayName.toString()
        email = currentUser.email.toString()

        selected_home_id = sharedPreferences.getString("homeId","").toString()
        selected_home_name = sharedPreferences.getString("homeName","").toString()
        profile_ph = sharedPreferences.getString("imagePath", "").toString()



            if(selected_home_name == "") {
                GlobalObj.db.collection("homes")
                    .whereEqualTo("user_id", user_id)
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            selected_home_id = document["home_id"].toString()
                            selected_home_name = document["home_name"].toString()
                            break
                        }
                        callback()
                    }
            }
        else{
            callback()
        }

    }

    fun changeSelectedHome(){
        val editor = sharedPreferences.edit()
        editor.putString("homeId", selected_home_id)
        editor.putString("homeName", selected_home_name)
        editor.apply()
    }

    fun saveProfileImage(context: Context, image: StorageReference, callback: () -> Unit) {

            val filePath = File(context.filesDir, "profileph.jpg")

            image.getFile(filePath).addOnSuccessListener {
                profile_ph = filePath.absolutePath
                val editor = sharedPreferences.edit()
                editor.putString("imagePath", profile_ph)
                editor.apply()
                callback()
            }
                .addOnFailureListener {
                    Toast.makeText(context, ""+ it.message, Toast.LENGTH_SHORT).show()
                    callback()
            }

    }

    fun clearInfo(){
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        user_id = ""
        name   = ""
        email  = ""
        profile_ph = ""
        selected_home_id =""
    }
}
