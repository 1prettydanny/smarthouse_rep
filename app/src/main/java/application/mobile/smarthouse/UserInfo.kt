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
    var email : String = ""
    var homes : MutableList<String> = mutableListOf()
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
                        if (document["homes"] != null)
                            homes = document["homes"] as MutableList<String>
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


    fun addhome(context: Context, home_id: String,callback: () -> Unit){

        homes.add(home_id)
        val userReference = GlobalObj.db.collection("users").document(user_id)

        // Обновление конкретного поля в документе
        userReference.update("homes", homes)
            .addOnCompleteListener { updateTask ->
                if (updateTask.isSuccessful) {
                    // Успешное обновление поля
                    callback()
                } else {
                    // Обработка ошибок при обновлении
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, ""+e.message, Toast.LENGTH_SHORT).show()
                val i =0
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
            }


    }

    fun clearInfo(){
        user_id = ""
        name   = ""
        email  = ""
        homes  = mutableListOf()
        profile_ph = ""
    }
}
private lateinit var sharedPreferences: SharedPreferences