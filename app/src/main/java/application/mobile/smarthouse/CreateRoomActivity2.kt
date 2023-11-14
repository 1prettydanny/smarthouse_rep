package application.mobile.smarthouse

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class CreateRoomActivity2 {


    data class RoomType(val typeName: String, val iconResId: Int)


    class RoomTypeAdapter(private val context: Context, private val roomTypes: List<RoomType>) :
        ArrayAdapter<RoomType>(context, R.layout.room_type_view, roomTypes) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val iconImageView: ImageView = view.findViewById(R.id.room_type_icon)
            val typeTextView: TextView = view.findViewById(R.id.room_type_icon)

            val roomType = getItem(position)
            iconImageView.setImageResource(roomType?.iconResId ?: R.drawable.home)
            typeTextView.text = roomType?.typeName

            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getView(position, convertView, parent)
        }
    }
}



