package application.mobile.smarthouse

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuInflater
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import application.mobile.smarthouse.databinding.ActivityRoomBinding

data class Device(val id: String, var name: String, val category: String){
    companion object {
        private val countersMap = mutableMapOf<String, Int>()

        fun getNextId(name: String): Int {
            val counter = countersMap.getOrDefault(name, 0) + 1
            countersMap[name] = counter
            return counter
        }
        fun getLastId(name: String): Int {
            val counter = countersMap.getOrDefault(name, 0) - 1
            countersMap[name] = counter
            return counter
        }

        fun countersClear(){
            countersMap.clear()
        }
    }
}
class RoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        val roomId = intent.getStringExtra("room_id").toString()
        var roomImage = intent.getStringExtra("room_image").toString()
        var roomName = intent.getStringExtra("room_name").toString()
        val roomType = intent.getStringExtra("room_type").toString()


        binding.roomName.text = roomName
        binding.roomTypeIcon.setImageDrawable(ContextCompat.getDrawable(this, GlobalFun.getTypeIcon(roomType)))

        binding.roomHeaderImage.setImageDrawable(ContextCompat.getDrawable(this, GlobalFun.getImage(roomImage)))


        val categoryArr = mutableListOf<String>()
        val devicesArr = mutableListOf<Device>()

        GlobalObj.db.collection("devices")
            .whereEqualTo("room_id", roomId)
            .get()
            .addOnSuccessListener {documents ->
                for(document in documents){
                    val id = document["device_id"].toString()
                    val name = document["device_name"].toString()
                    val category = document["device_category"].toString()

                    devicesArr.add(Device(id,name + " ${Device.getNextId(name)}",category))

                    if (!categoryArr.contains(category)) {
                        categoryArr.add(category)
                    }
                }

            }


        binding.cancelBtn.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        binding.roomOtherBtn.setOnClickListener { view ->
            val popup = PopupMenu(this, view, Gravity.END)
            MenuInflater(this).inflate(R.menu.room_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {

                    R.id.changePicture -> {

                        var image: String
                        do {
                            image = GlobalFun.randomImage(roomType)
                        } while (image == roomImage)

                        GlobalFun.updateRoomImage(roomId, image){
                            roomImage = image
                            binding.roomHeaderImage.setImageDrawable(ContextCompat.getDrawable(this, GlobalFun.getImage(roomImage)))
                        }
                        true
                    }

                    R.id.renameRoom -> {
                        val text = ContextCompat.getString(this,R.string.dialog_rename_room_text)
                        GlobalFun.renameItem(this, roomName,text){ newName ->
                            if(newName != roomName){
                                GlobalFun.updateRoomName(roomId, newName){
                                    roomName = newName
                                    binding.roomName.text = roomName
                                }
                            }
                        }
                        true
                    }

                    R.id.deleteRoom -> {
                        val text = ContextCompat.getString(this, R.string.dialog_delete_room_text) + " \"$roomName\" ?"
                        GlobalFun.dialogDelete(this, text) {delete->
                            if (delete) {
                                GlobalFun.deleteDevices(roomId)
                                GlobalFun.deleteRoom(roomId) {
                                    onBackPressedCallback.handleOnBackPressed()
                                    finish()
                                }
                            }
                        }
                        true
                    }

                    else -> false
                }

            }
            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popup)
                mPopup.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true)
            } catch (_: Exception) {

            } finally {
                popup.show()
            }
        }
    }
    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(this@RoomActivity, BaseActivity::class.java)
            val anim = ActivityOptions.makeCustomAnimation(this@RoomActivity, R.anim.slide_in_down, R.anim.stay)
            startActivity(intent, anim.toBundle())
        }
    }
}