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
    var isFirstRun = true
    var check_room: String? = null

    data class RoomType(val typeName: String, val icon: Int)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        var myTask: TimerTask? = null


        val roomTypes = arrayListOf(
            RoomType("Choose your room type", R.drawable.arrow_down),
            RoomType("Classic", R.drawable.room_type_classic_ico),
            RoomType("Kitchen", R.drawable.room_type_kitchen_ico),
            RoomType("Bathroom", R.drawable.room_type_bathroom_ico),
        )


        check_room = intent.getStringExtra("room_id")
        val arrType: MutableList<RoomType> = arrayListOf()
        arrType.addAll(roomTypes)
        val adapter = RoomTypeAdapter(this, arrType)
        val roomTypeSpinner= binding.spinner
        roomTypeSpinner.adapter = adapter



        if(check_room!= null) {
            isFirstRun=false
            val name_of_room = intent.getStringExtra("room_name")
            binding.roomNameInput.setText(name_of_room)

            val type_of_room = intent.getIntExtra("room_type", -1)

            arrType.removeAt(0)
            adapter.notifyDataSetChanged()


            val index = when (type_of_room) {
                R.drawable.room_type_classic_ico -> 0
                R.drawable.room_type_kitchen_ico -> 1
                R.drawable.room_type_bathroom_ico -> 2
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

        binding.backBtn.setOnClickListener{
            onBackPressedCallback.handleOnBackPressed()
        }

        binding.createRoomBtn.setOnClickListener {



            val room_name: String = binding.roomNameInput.text.toString().trim()

            val room_type = binding.spinner.selectedItem as RoomType

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

            val randomImageResource = randomImage(room_type.icon)


            if(check_room==null) {

                val room_id = UUID.randomUUID().toString()

                val home_id = UserInfo.selected_home


                val room = hashMapOf(
                    "room_id" to room_id,
                    "home_id" to home_id,
                    "room_name" to room_name,
                    "room_type" to room_type.icon,
                    "room_image" to randomImageResource
                )

                GlobalObj.db.collection("rooms").add(room)
                val intent = Intent(this, RoomSettingsActivity::class.java)
                intent.putExtra("room_id", room_id)
                intent.putExtra("room_name", room_name)
                intent.putExtra("room_image", randomImageResource)
                intent.putExtra("room_type", room_type.icon)
                startActivity(intent)
            }
            else{

                val reference = GlobalObj.db.collection("rooms")
                var docId: String? = null
                reference.whereEqualTo("room_id",check_room)
                    .get()
                    .addOnSuccessListener {
                        for(document in it) {
                             docId = document.id
                        }
                        val updates = hashMapOf<String, Any>(
                            "room_name" to room_name,
                            "room_type" to room_type.icon
                        )
                        if(docId!=null) {
                            reference.document(docId!!)
                                .update(updates)
                                .addOnCompleteListener {
                                    val intent = Intent(this, RoomSettingsActivity::class.java)
                                    intent.putExtra("room_id", check_room)
                                    intent.putExtra("room_name", room_name)
                                    intent.putExtra("room_image", randomImageResource)
                                    intent.putExtra("room_type", room_type.icon)
                                    startActivity(intent)
                                }
                        }

                    }

            }



        }
    }

    private fun randomImage(icon: Int): Int{

        val imagesArray = when (icon) {
            R.drawable.room_type_classic_ico -> arrayOf(
                R.drawable.img_classic1,
                R.drawable.img_classic2,
                R.drawable.img_classic3,
                R.drawable.img_classic4
            )

            R.drawable.room_type_kitchen_ico -> arrayOf(
                R.drawable.img_kitchen1,
                R.drawable.img_kitchen2,
                R.drawable.img_kitchen3,
                R.drawable.img_kitchen4
            )

            R.drawable.room_type_bathroom_ico -> arrayOf(
                R.drawable.img_bathroom1,
                R.drawable.img_bathroom2,
                R.drawable.img_bathroom3,
                R.drawable.img_bathroom4
            )

            else -> emptyArray()
        }

        return if (imagesArray.isNotEmpty()) {
            imagesArray[Random.nextInt(imagesArray.size)]
        } else {
            -1
        }
    }


    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(check_room != null) {
                val reference = GlobalObj.db.collection("rooms")
                var docId: String? = null
                reference.whereEqualTo("room_id", check_room)
                    .get()
                    .addOnSuccessListener {
                        for (document in it) {
                            docId = document.id
                        }

                        if (docId != null) {
                            reference.document(docId!!).delete()
                        }
                    }
            }
            finish()
            val intent = Intent(this@CreateRoomActivity,BaseActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onDestroy() {
        isFirstRun=true
        timer.cancel()
        super.onDestroy()
    }

    inner class RoomTypeAdapter(context: Context, arrType: MutableList<RoomType>) :
        ArrayAdapter<RoomType>(context, 0, arrType) {

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutInflater.from(context).inflate(R.layout.room_type_view, parent, false)
            val roomTypeLayout= view.findViewById<LinearLayout>(R.id.room_type_layout)
            val iconImageView = view.findViewById<ImageView>(R.id.room_type_icon)
            val typeTextView = view.findViewById<TextView>(R.id.room_type_name)



            val roomType = getItem(position)
            iconImageView.setImageResource(roomType?.icon ?: R.drawable.menu_home_ico)
            typeTextView.text = roomType?.typeName


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


