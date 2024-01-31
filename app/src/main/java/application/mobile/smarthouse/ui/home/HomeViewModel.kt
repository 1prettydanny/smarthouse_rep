package application.mobile.smarthouse.ui.home

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import application.mobile.smarthouse.GlobalObj
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private var sharedPreferences: SharedPreferences =
        application.getSharedPreferences("room_prefs", AppCompatActivity.MODE_PRIVATE)
    private val db = GlobalObj.db

    fun getRooms(homeId: String) = liveData(Dispatchers.IO) {
        val result = db.collection("rooms")
            .whereEqualTo("home_id", homeId)
            .get()
            .await()

        val rooms = mutableListOf<Room>()
        for (document in result) {
            val id = document["room_id"].toString()
            val name = document["room_name"].toString()
            val image = document["room_image"].toString().toInt()
            val type = document["room_type"].toString().toInt()
            rooms.add(Room(id, name, image, type, false))
        }

        emit(rooms)
    }

    fun loadRoomsOrderFromSharedPreferences(): List<String> {
        val orderString = sharedPreferences.getString("room_order", "")
        return if(orderString == "")
            emptyList()
        else
            orderString?.split(",") ?: emptyList()
    }

    fun saveOrderToSharedPreferences(roomIds: List<String>) {
        val editor = sharedPreferences.edit()
        val orderString = roomIds.joinToString(",")
        editor.putString("room_order", orderString)
        editor.apply()
    }
}