package application.mobile.smarthouse

import android.app.Activity
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import java.util.Timer
import java.util.TimerTask
import kotlin.random.Random

class GlobalFun {

    companion object {
        private var myTask: TimerTask? = null
        private var timer = Timer()
        fun randomImage(type: String): String {

            val imagesArray = when (type) {
                "Classic" -> arrayOf(
                    "Classic 1",
                    "Classic 2",
                    "Classic 3",
                    "Classic 4"
                )

                "Kitchen"-> arrayOf(
                    "Kitchen 1",
                    "Kitchen 2",
                    "Kitchen 3",
                    "Kitchen 4"
                )

                "Bathroom" -> arrayOf(
                    "Bathroom 1",
                    "Bathroom 2",
                    "Bathroom 3",
                    "Bathroom 4"
                )

                else -> emptyArray()
            }

            return if (imagesArray.isNotEmpty()) {
                imagesArray[Random.nextInt(imagesArray.size)]
            } else {
                "Error"
            }
        }

        fun iconDevice(name: String): Int{
            return when(name){
                "Bulb" -> R.drawable.new_bulb_ico
                "Chandelier" -> R.drawable.new_chandelier_ico
                "Desktop lamp"  -> R.drawable.new_lamp_desk_ico
                "Spotlight"->  R.drawable.new_spotlight_ico

                "Radiator"  -> R.drawable.new_radiator_ico
                "Cooling system"  -> R.drawable.new_conditioner_ico
                "Heating floor" -> R.drawable.new_heating_floor_ico
                "Ceiling fan" -> R.drawable.new_ceiling_fan_ico
                "Desktop fan" ->  R.drawable.new_desk_fan_ico
                "Thermo regulator" -> R.drawable.new_thermostat_ico
                "Humidifier" -> R.drawable.new_humidifier_ico

                "Switch"  -> R.drawable.new_switch_ico
                "Plug" -> R.drawable.new_plug_ico
                "Socket"-> R.drawable.new_socket_ico

                "TV" -> R.drawable.new_tv_ico
                "Radio"  -> R.drawable.new_radio_ico
                "Speaker" -> R.drawable.new_speaker_ico
                "Projector"  -> R.drawable.new_projector_ico

                "Camera" -> R.drawable.new_videocamera_ico
                "Lock"-> R.drawable.new_lock_ico

                "Fridge" -> R.drawable.new_fridge_ico
                "Coffee machine" -> R.drawable.new_coffee_machine_ico
                "Stove" -> R.drawable.new_stove_ico

                "Washing machine" -> R.drawable.new_washing_machine_ico
                "Tap" -> R.drawable.new_tap_ico
                "Shower" -> R.drawable.new_shower_ico

                "Wi-Fi"  -> R.drawable.new_wi_fi_ico
                "Moped" -> R.drawable.new_moped_ico
                "Scooter" -> R.drawable.new_scooter_ico
                "Printer" -> R.drawable.new_printer_ico
                else -> {R.drawable.smile_icon}
            }
        }

        fun getImage(imageName: String): Int{
            return  when (imageName) {
                "Classic 1" -> R.drawable.img_classic1
                "Classic 2" -> R.drawable.img_classic2
                "Classic 3" -> R.drawable.img_classic3
                "Classic 4" -> R.drawable.img_classic4
                "Kitchen 1" -> R.drawable.img_kitchen1
                "Kitchen 2" -> R.drawable.img_kitchen2
                "Kitchen 3" -> R.drawable.img_kitchen3
                "Kitchen 4" -> R.drawable.img_kitchen4
                "Bathroom 1" -> R.drawable.img_bathroom1
                "Bathroom 2" -> R.drawable.img_bathroom2
                "Bathroom 3" -> R.drawable.img_bathroom3
                "Bathroom 4" -> R.drawable.img_bathroom4
                else -> {R.drawable.smile_icon}
            }
        }

        fun updateRoomImage(id: String, image: String, callback: () -> Unit){
            val reference = GlobalObj.db.collection("rooms")
            var docId: String? = null
            reference.whereEqualTo("room_id", id)
                .get()
                .addOnSuccessListener {
                    for(document in it) {
                        docId = document.id
                    }
                    val updates = hashMapOf<String, Any>(
                        "room_image" to image
                    )
                    if(docId!=null) {
                        reference.document(docId!!)
                            .update(updates)
                            .addOnSuccessListener { callback() }
                    }
                }
        }

        fun getTypeIcon(type: String): Int{
            return when(type){
                "Classic" -> {R.drawable.room_type_classic_ico}
                "Kitchen" -> {R.drawable.room_type_kitchen_ico}
                "Bathroom" -> {R.drawable.room_type_bathroom_ico}
                else -> {-1}
            }
        }

        fun deleteDevices(roomId: String) {
            val devicesReference = GlobalObj.db.collection("devices")
            devicesReference.whereEqualTo("room_id", roomId)
                .get()
                .addOnSuccessListener { devDocuments ->
                    devDocuments.forEach { devDocument ->
                        devicesReference.document(devDocument.id).delete()
                    }
                }
        }

        fun deleteRoom(roomId: String, callback: () -> Unit) {
            val roomsReference = GlobalObj.db.collection("rooms")
            roomsReference.whereEqualTo("room_id", roomId)
                .get()
                .addOnSuccessListener { roomDocuments ->
                    roomDocuments.forEach { roomDocument ->
                        roomsReference.document(roomDocument.id).delete()
                        callback()
                    }
                }
        }



        fun deleteHome(homeId: String, callback: () -> Unit) {
            val roomsReference = GlobalObj.db.collection("homes")
            roomsReference.whereEqualTo("home_id", homeId)
                .get()
                .addOnSuccessListener { roomDocuments ->
                    roomDocuments.forEach { roomDocument ->
                        roomsReference.document(roomDocument.id).delete()
                        callback()
                    }
                }
        }
        fun stopTimer() {
            myTask?.cancel()
            timer.cancel()
        }

        fun dialogDelete(activity: Activity, text: String, callback: (delete: Boolean) -> Unit){

            val builder = AlertDialog.Builder(activity, R.style.DialogTheme)

            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.dialog_delete, null)
            builder.setView(dialogLayout)
            val dialog = builder.create()

            val textView = dialogLayout.findViewById<TextView>(R.id.dialog_text)
            val okBtn = dialogLayout.findViewById<Button>(R.id.ok_btn)
            val cancelBtn = dialogLayout.findViewById<Button>(R.id.cancel_btn)
            textView.text = text

            okBtn.setOnClickListener {
                dialog.cancel()
                    callback(true)

            }

            cancelBtn.setOnClickListener{
                dialog.cancel()
                callback(false)
            }

            dialog.show()
        }

        fun renameItem(activity: Activity, name: String, text: String, callback: (newName: String) -> Unit){
            val builder = AlertDialog.Builder(activity, R.style.DialogTheme)

            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.dialog_edit_room, null)
            builder.setView(dialogLayout)
            val dialog = builder.create()

            val textView = dialogLayout.findViewById<TextView>(R.id.dialog_text)
            val error = dialogLayout.findViewById<TextView>(R.id.dialog_error)
            val editText  = dialogLayout.findViewById<EditText>(R.id.dialog_edit_text)
            val okBtn = dialogLayout.findViewById<Button>(R.id.ok_btn)
            val cancelBtn = dialogLayout.findViewById<Button>(R.id.cancel_btn)
            textView.text = text
            editText.setText(name)
            editText.addTextChangedListener(GlobalFun.getTextWatcher(editText,error, activity))

            okBtn.setOnClickListener {
                if(editText.text.length < 3 || editText.text.length > 20) {
                    error.visibility = View.VISIBLE
                }
                else {
                        stopTimer()
                        dialog.cancel()
                        callback(editText.text.toString())
                }
            }

            cancelBtn.setOnClickListener{
                stopTimer()
                dialog.cancel()
                callback(name)
            }

            dialog.show()
        }
        fun updateHomeName(id: String, newName: String, callback: () -> Unit){
            val reference = GlobalObj.db.collection("homes")
            var docId: String? = null
            reference.whereEqualTo("home_id", id)
                .get()
                .addOnSuccessListener {
                    for (document in it) {
                        docId = document.id
                    }
                    val updates = hashMapOf<String, Any>(
                        "home_name" to newName
                    )
                    if (docId != null) {
                        reference.document(docId!!)
                            .update(updates)
                            .addOnCompleteListener {
                                callback()
                            }
                    }
                }
        }
        fun updateRoomName(id: String, newName: String, callback: () -> Unit){
            val reference = GlobalObj.db.collection("rooms")
            var docId: String? = null
            reference.whereEqualTo("room_id", id)
                .get()
                .addOnSuccessListener {
                    for (document in it) {
                        docId = document.id
                    }
                    val updates = hashMapOf<String, Any>(
                        "room_name" to newName
                    )
                    if (docId != null) {
                        reference.document(docId!!)
                            .update(updates)
                            .addOnCompleteListener {
                                callback()
                            }
                    }
                }
        }
        fun getTextWatcher(editText: EditText, error: TextView,  activity: Activity): TextWatcher {

            val shortText: String
            val longText: String

            if(activity is CreateHomeActivity){
                shortText = getString(activity, R.string.short_home_name_error)
                longText = getString(activity, R.string.long_home_name_error)
            }
            else{
                 shortText = getString(activity, R.string.short_room_name_error)
                 longText = getString(activity, R.string.long_room_name_error)
            }
            timer = Timer()
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    try {
                        if ((s!!.length < 3) || (s.length >= 25)) {
                            myTask?.cancel()

                            myTask = object : TimerTask() {
                                override fun run() {
                                    activity.runOnUiThread {
                                        if (s.length <= 3)
                                            error.text = shortText
                                        else
                                            error.text = longText

                                        error.visibility = View.VISIBLE
                                        editText.background = ContextCompat.getDrawable(
                                            activity,
                                            R.drawable.rounded_edit_text
                                        )
                                    }
                                }
                            }
                            timer.schedule(myTask, 1000)
                        } else {
                            myTask?.cancel()
                            error.visibility = View.GONE
                            editText.background = ContextCompat.getDrawable(
                                activity,
                                R.drawable.rounded_selected_edit_text
                            )
                        }
                    } catch (_: Exception) {

                    }

                }

                override fun afterTextChanged(s: Editable?) {}

            }
            return textWatcher
        }
    }
}