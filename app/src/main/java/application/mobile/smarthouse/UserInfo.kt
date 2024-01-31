package application.mobile.smarthouse
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.StorageReference
import java.io.File


object UserInfo {

    var user_id : String = ""
    var name  : String = ""
    private var email : String = ""
    var selected_home: String = ""
    var profile_ph: String = ""

    fun initializationUser(context:Context, callback: () -> Unit){

        sharedPreferences = context.getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)

        val userId = sharedPreferences.getString("userId", "")
        val userName = sharedPreferences.getString("userName", "")
        val userEmail = sharedPreferences.getString("userEmail", "")
        profile_ph = sharedPreferences.getString("imagePath", "").toString()
        if (userId != "" && userName!="" && userEmail!="") {
            GlobalObj.db.collection("users")
                .whereEqualTo("user_id", userId)
                .whereEqualTo("name", userName)
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        user_id = document["user_id"].toString()
                        name = document["name"].toString()
                        email = document["email"].toString()
                        if(document["selected_home"] != null)
                        selected_home = document["selected_home"].toString()
                    }

                    callback()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "errInitU: " + exception.message, Toast.LENGTH_SHORT)
                        .show()
                    callback()
                }

        }
        else {
            callback()
        }
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
        user_id = ""
        name   = ""
        email  = ""
        profile_ph = ""
        selected_home =""
    }
}
private lateinit var sharedPreferences: SharedPreferences