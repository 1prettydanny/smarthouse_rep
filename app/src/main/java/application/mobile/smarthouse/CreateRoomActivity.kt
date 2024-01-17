package application.mobile.smarthouse

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import application.mobile.smarthouse.databinding.ActivityCreateRoomBinding
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import kotlin.random.Random


class CreateRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRoomBinding
    val timer = Timer()
    data class RoomType(val typeName: String, val iconResId: Int)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        var myTask: TimerTask? = null



        val roomTypes = arrayListOf(
            RoomType("Choose your room type", R.drawable.arrow_down),
            RoomType("Living room", R.drawable.room_type_living_room_ico),
            RoomType("Kitchen", R.drawable.room_type_kitchen_ico),
            RoomType("Bathroom", R.drawable.room_type_bathroom_ico),
         //   RoomType("Other", R.drawable.other_ico)
        )

        val arrType: MutableList<RoomType> = arrayListOf()
        arrType.addAll(roomTypes)

        val roomTypeSpinner: Spinner = binding.spinner
        val adapter = RoomTypeAdapter(this, arrType)
        roomTypeSpinner.adapter = adapter

        roomTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?,
                position: Int, id: Long
            ) {
                if (isFirstRun == 3 && position == 0) {
                    arrType.removeAt(1)
                    isFirstRun++
                    binding.roomTypeErrorInput.visibility = View.GONE
                }

                (parentView?.adapter as RoomTypeAdapter).setSelectedItemPosition(position)

                if (position !=0) {
                    val selectedRoomType = arrType.removeAt(position)
                    arrType.add(0, selectedRoomType)
                    adapter.notifyDataSetChanged()
                    roomTypeSpinner.setSelection(0)
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                (parentView?.adapter as RoomTypeAdapter).setSelectedItemPosition(AdapterView.INVALID_POSITION)
            }
        }


        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    if ((s!!.length < 3) || (s!!.length >= 25)) {
                        myTask?.cancel()

                        myTask = object : TimerTask() {
                            override fun run() {
                                runOnUiThread {
                                    if (s!!.length <= 3)
                                        binding.roomErrorInput.text =
                                            getString(R.string.short_room_name_error)
                                    else
                                        binding.roomErrorInput.text =
                                            getString(R.string.long_room_name_error)

                                    binding.roomErrorInput.visibility = View.VISIBLE
                                    binding.roomNameInput.background = ContextCompat.getDrawable(
                                        this@CreateRoomActivity,
                                        R.drawable.rounded_edit_text
                                    )
                                }
                            }
                        }
                        timer.schedule(myTask, 1000)
                    } else {
                        myTask?.cancel()
                        binding.roomErrorInput.visibility = View.GONE
                        binding.roomNameInput.background = ContextCompat.getDrawable(
                            this@CreateRoomActivity,
                            R.drawable.rounded_selected_edit_text
                        )
                    }
                } catch (_: Exception) {
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }

        }

        binding.roomNameInput.addTextChangedListener(textWatcher)

        binding.createRoomBtn.setOnClickListener {

            val room_name: String = binding.roomNameInput.text.toString().trim()

            if (room_name.length < 3) {
                binding.roomErrorInput.visibility = View.VISIBLE
                binding.roomErrorInput.text = getString(R.string.short_room_name_error)
                return@setOnClickListener
            }

            if (room_name.length > 25) {
                binding.roomErrorInput.visibility = View.VISIBLE
                binding.roomErrorInput.text = getString(R.string.long_room_name_error)
                return@setOnClickListener
            }

            if (arrType.size == 4) {
                binding.roomTypeErrorInput.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val room_id = UUID.randomUUID().toString()
            val room_type = binding.spinner.selectedItem as RoomType
            val home_id = UserInfo.homes[UserInfo.selected_home]
            val number_of_room_type = getTypeID(room_type.typeName)

            val imagesArray = when (number_of_room_type) {
                0 -> arrayOf(R.drawable.img_living_room1, R.drawable.img_living_room2, R.drawable.img_living_room3, R.drawable.img_living_room4)
                1 -> arrayOf(R.drawable.img_kitchen1, R.drawable.img_kitchen2, R.drawable.img_kitchen3, R.drawable.img_kitchen4)
                2 -> arrayOf(R.drawable.img_bathroom1, R.drawable.img_bathroom2, R.drawable.img_bathroom3, R.drawable.img_bathroom4)
                else -> emptyArray()
            }

            val randomImageResource = if (imagesArray.isNotEmpty()) {
                imagesArray[Random.nextInt(imagesArray.size)]
            } else { -1 }

//            val room = hashMapOf(
//                "room_id" to room_id,
//                "home_id" to home_id,
//                "room_name" to room_name,
//                "room_type" to number_of_room_type
//            )
//
//              GlobalObj.db.collection("rooms").add(room)

            val intent = Intent(this, RoomSettingsActivity::class.java)
            intent.putExtra("room_name", room_name)
            intent.putExtra("room_image", randomImageResource)
            intent.putExtra("room_type", number_of_room_type)
            startActivity(intent)

//                UserInfo.addhome(this,home_id) {
//
//                    val intent = Intent(this, BaseActivity::class.java)
//                    intent.putExtra("home_name", home_name)
//                    startActivity(intent)
//                }


        }
    }

    private fun getTypeID(typeName: String): Int{
        return when(typeName){
            "Living room" -> { 0 }
            "Kitchen" -> { 1 }
            "Bathroom" -> { 2 }
            else -> { -1 }
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
    override fun onResume() {
        super.onResume()
    }
    override fun onDestroy() {
        isFirstRun=0
        timer.cancel()
        super.onDestroy()
    }

    class RoomTypeAdapter(context: Context, arrType: MutableList<RoomType>) :
        ArrayAdapter<RoomType>(context, 0, arrType) {

        private var selectedItemPosition = AdapterView.INVALID_POSITION
        fun setSelectedItemPosition(position: Int) {
            selectedItemPosition = position
            notifyDataSetChanged()
        }
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutInflater.from(context).inflate(R.layout.room_type_view, parent, false)
            val roomTypeLayout= view.findViewById<LinearLayout>(R.id.room_type_layout)
            val iconImageView = view.findViewById<ImageView>(R.id.room_type_icon)
            val typeTextView = view.findViewById<TextView>(R.id.room_type_name)



            val roomType = getItem(position)
            iconImageView.setImageResource(roomType?.iconResId ?: R.drawable.menu_home_ico)
            typeTextView.text = roomType?.typeName


            if (isFirstRun < 3) {
                roomTypeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                isFirstRun ++
            } else {
                if (position == selectedItemPosition) {
                    roomTypeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.select_green))
                } else {
                    roomTypeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                }
            }

            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {

            return getView(position , convertView, parent)

        }
    }


    companion object{
        var isFirstRun = 0
    }
}


