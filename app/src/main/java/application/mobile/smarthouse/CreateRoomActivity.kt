package application.mobile.smarthouse

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
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat

class CreateRoomActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        val homeId = intent.getStringExtra("home_id")

        val roomTypes = arrayListOf(
            RoomType("Classic", R.drawable.home),
            RoomType("Kitchen", R.drawable.home),
            RoomType("Other", R.drawable.home)
        )
        val arrType : MutableList<RoomType> = arrayListOf()
        arrType.addAll(roomTypes)

        val roomTypeSpinner: Spinner = findViewById(R.id.spinner)
        val adapter = RoomTypeAdapter(this,arrType)
        roomTypeSpinner.adapter = adapter



        roomTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                (parentView?.adapter as RoomTypeAdapter).setSelectedItemPosition(position)
                roomTypeSpinner.setTag(R.id.spinner, position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                (parentView?.adapter as RoomTypeAdapter).setSelectedItemPosition(AdapterView.INVALID_POSITION)
                roomTypeSpinner.setTag(R.id.spinner, null)
            }
        }

    }


    data class RoomType(val typeName: String, val iconResId: Int)


    class RoomTypeAdapter(context: Context, arrType: MutableList<RoomType>) :
        ArrayAdapter<RoomType>(context, 0, arrType) {
        private var isFirstRun = 0


        private var selectedItemPosition = AdapterView.INVALID_POSITION
        fun setSelectedItemPosition(position: Int) {
            selectedItemPosition = position
            notifyDataSetChanged()
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutInflater.from(context).inflate(R.layout.room_type_view, parent, false)
            val roomTypeLayout= view.findViewById<LinearLayout>(R.id.room_type_layout)
            val iconImageView = view.findViewById<ImageView>(R.id.room_type_icon)
            val typeTextView = view.findViewById<TextView>(R.id.room_type_name)

            val roomType = getItem(position)
            iconImageView.setImageResource(roomType?.iconResId ?: R.drawable.home)
            typeTextView.text = roomType?.typeName


            if (isFirstRun < 3) {
                roomTypeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                isFirstRun ++
            } else {
                if (position == selectedItemPosition) {
                    roomTypeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                } else {
                    roomTypeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                }
            }

            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getView(position, convertView, parent)
        }
    }
}