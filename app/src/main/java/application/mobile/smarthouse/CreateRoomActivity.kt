package application.mobile.smarthouse

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import application.mobile.smarthouse.databinding.ActivityCreateRoomBinding
import java.util.UUID


class CreateRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRoomBinding
    var isFirstRun = true
    var checkRoom: String? =  null

    data class RoomType(val typeName: String, val icon: Int)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val roomTypes = arrayListOf(
            RoomType("Choose your room type", R.drawable.arrow_down),
            RoomType("Classic", R.drawable.room_type_classic_ico),
            RoomType("Kitchen", R.drawable.room_type_kitchen_ico),
            RoomType("Bathroom", R.drawable.room_type_bathroom_ico),
        )


        val arrType = mutableListOf<RoomType>()
        arrType.addAll(roomTypes)
        val adapter = RoomTypeAdapter(this, arrType)
        val roomTypeSpinner= binding.roomTypeSpinner
        roomTypeSpinner.adapter = adapter


        checkRoom = intent.getStringExtra("room_id")
        if(checkRoom!= null) {
            isFirstRun=false
            val nameOfRoom = intent.getStringExtra("room_name")
            binding.roomNameInput.setText(nameOfRoom)

            val typeOfRoom = intent.getStringExtra("room_type").toString()

            arrType.removeAt(0)
            adapter.notifyDataSetChanged()


            val index = when (typeOfRoom) {
                "Classic" -> 0
                "Kitchen" -> 1
                "Bathroom" -> 2
                else -> -1
            }
            val selectedRoomType = arrType.removeAt(index)
            arrType.add(0, selectedRoomType)
            adapter.notifyDataSetChanged()
            roomTypeSpinner.setSelection(0)
        }

        roomTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?,
                position: Int, id: Long
            ) {
                if (position !=0) {
                    val selectedRoomType = arrType.removeAt(position)
                    arrType.add(0, selectedRoomType)
                    if (isFirstRun) {
                        arrType.removeAt(1)
                        isFirstRun=false
                        binding.roomTypeErrorInput.visibility = View.GONE
                    }
                    adapter.notifyDataSetChanged()
                    roomTypeSpinner.setSelection(0)
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }


        binding.roomNameInput.addTextChangedListener(GlobalFun.getTextWatcher(binding.roomNameInput,binding.roomErrorInput,this@CreateRoomActivity))
        binding.cancelBtn.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }




        binding.createRoomBtn.setOnClickListener {

            val roomName: String = binding.roomNameInput.text.toString().trim()

            val roomType = binding.roomTypeSpinner.selectedItem as RoomType

            if (roomName.length < 3) {
                binding.roomErrorInput.visibility = View.VISIBLE
                binding.roomErrorInput.text = getString(R.string.short_room_name_error)
                return@setOnClickListener
            }

            if (roomName.length > 25) {
                binding.roomErrorInput.visibility = View.VISIBLE
                binding.roomErrorInput.text = getString(R.string.long_room_name_error)
                return@setOnClickListener
            }

            if (arrType.size == 4) {
                binding.roomTypeErrorInput.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val randomImageResource = GlobalFun.randomImage(roomType.typeName)

            if(checkRoom!=null) {

                var docId: String? = null
                val reference = GlobalObj.db.collection("rooms")
                reference.whereEqualTo("room_id", checkRoom)
                    .get()
                    .addOnSuccessListener {
                        for(document in it) {
                            docId = document.id
                        }
                        val updates = hashMapOf<String, Any>(
                            "room_name" to roomName,
                            "room_type" to roomType.typeName
                        )
                        if(docId!=null) {
                            reference.document(docId!!)
                                .update(updates)
                        }

                    }
                nextActivity(roomName, randomImageResource, roomType.typeName)
            }
            else{
                val roomId = UUID.randomUUID().toString()

                val homeId = UserInfo.selected_home_id


                val room = hashMapOf(
                    "room_id" to roomId,
                    "home_id" to homeId,
                    "room_name" to roomName,
                    "room_type" to roomType.typeName,
                    "room_image" to randomImageResource
                )
                checkRoom = roomId
                GlobalObj.db.collection("rooms").add(room)
                nextActivity(roomName, randomImageResource, roomType.typeName)

            }

        }
    }

    private fun nextActivity(roomName: String, image: String, type: String){
        val intent = Intent(this, RoomSettingsActivity::class.java)
        intent.putExtra("room_id", checkRoom)
        intent.putExtra("room_name", roomName)
        intent.putExtra("room_image", image)
        intent.putExtra("room_type", type)
        val animationBundle = ActivityOptions.makeCustomAnimation(
            this@CreateRoomActivity,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        ).toBundle()
        startActivity(intent, animationBundle)
    }


    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(checkRoom != null) {
                GlobalFun.deleteRoom(checkRoom!!) {
                    backActivity()
                    finish()
                }
            }
            else{
                backActivity()
                finish()
            }
        }
    }

    fun backActivity(){
        val intent = Intent(this@CreateRoomActivity,BaseActivity::class.java)
        val animationBundle = ActivityOptions.makeCustomAnimation(
            this@CreateRoomActivity,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        ).toBundle()
        startActivity(intent,animationBundle)
    }


    override fun onDestroy() {
        isFirstRun=true
        GlobalFun.stopTimer()
        super.onDestroy()
    }

    inner class RoomTypeAdapter(context: Context, arrType: MutableList<RoomType>) :
        ArrayAdapter<RoomType>(context, 0, arrType) {

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutInflater.from(context).inflate(R.layout.spinner_item_room_type, parent, false)
            val roomTypeLayout= view.findViewById<LinearLayout>(R.id.room_type_layout)
            val iconImageView = view.findViewById<ImageView>(R.id.room_type_icon)
            val typeTextView = view.findViewById<TextView>(R.id.room_type_name)



            val roomType = getItem(position) as RoomType
            iconImageView.setImageDrawable(ContextCompat.getDrawable(this@CreateRoomActivity,roomType.icon))
            typeTextView.text = roomType.typeName


            if (isFirstRun) {
                roomTypeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
            } else {
                if (position == 0) {
                    roomTypeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.select_green))
                } else {
                    roomTypeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                }
            }

            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = getView(position , convertView, parent)

            if(isFirstRun && position==0) {
                val roomTypeLayout= view.findViewById<LinearLayout>(R.id.room_type_layout)
                roomTypeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.select_green))
            }
            return view
        }
    }


}


