package dk.bracketz.roomregistration

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dk.bracketz.roomregistration.model.Reservation
import dk.bracketz.roomregistration.model.Room
import dk.bracketz.roomregistration.model.User
import dk.bracketz.roomregistration.restconsuming.ApiUtils
import kotlinx.android.synthetic.main.activity_add_booking.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.util.*
import kotlin.math.log


class AddBookingActivity : AppCompatActivity() {

    var chosenRoom = Room()
    var timeFormat = SimpleDateFormat("dd/MM/yy - HH:mm", Locale.GERMAN)
    val fromTime = Calendar.getInstance()
    val toTime = Calendar.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_booking)

        val roomBundle = intent.getBundleExtra("room")
        setChosenRoom(roomBundle)

        //actionbar
        val actionbar = supportActionBar
        actionbar!!.title = "Book "+chosenRoom.name
        actionbar.setDisplayHomeAsUpEnabled(true)

        // Set default dateTimes
        editFromTime.setText(timeFormat.format(fromTime.time))
        toTime.add(Calendar.HOUR,1)
        editToTime.setText(timeFormat.format(toTime.time))

        // From time picker
        editFromTime.setOnClickListener(View.OnClickListener {
            DatePickerDialog(this,calendatSetter(fromTime, editFromTime), fromTime
                    .get(Calendar.YEAR), fromTime.get(Calendar.MONTH), fromTime.get(Calendar.DAY_OF_MONTH)).show()
            editFromTime.setText(timeFormat.format(fromTime.time))
        })

        // To time picker
        editToTime.setOnClickListener(View.OnClickListener {
            DatePickerDialog(this,calendatSetter(toTime, editToTime), toTime
                    .get(Calendar.YEAR), toTime.get(Calendar.MONTH), toTime.get(Calendar.DAY_OF_MONTH)).show()
        })
    }

    // From date / time picker dialog
    private fun calendatSetter(c:Calendar, textField:EditText):OnDateSetListener{
        return OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            c[Calendar.YEAR] = year
            c[Calendar.MONTH] = monthOfYear
            c[Calendar.DAY_OF_MONTH] = dayOfMonth

            // Nested time picker
            TimePickerDialog(this,OnTimeSetListener { view, hourOfDay, minute ->
                c[Calendar.HOUR_OF_DAY] = hourOfDay
                c[Calendar.MINUTE] = minute
                textField.setText(timeFormat.format(c.time))
            },c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val actionbar = supportActionBar
        actionbar!!.title = "Book "+chosenRoom.name
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    fun setChosenRoom(roomBundle:Bundle) {
        chosenRoom.name = roomBundle.getString("name")
        chosenRoom.id = roomBundle.getInt("id")
    }

    // navigation bar buttons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_select_room) {
            // Create intent - go to select room activity
            val intent = Intent(this, RoomActivity::class.java)
            startActivityForResult(intent, 42)
            return true
        }
        if (id == R.id.action_log_in) {
            // Spørg bruger om han ønsker at logge ud
            val alertDialog = AlertDialog.Builder(this@AddBookingActivity).create()
            alertDialog.setTitle("Logged in as " + User.getInstance().firebaseUser.email)
            alertDialog.setMessage("Do you want to log out?")
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Yes"
            ) { dialog, which ->
                User.getInstance().logout()
                dialog.dismiss()
                val toast = Toast.makeText(applicationContext, "You have been logged out.", Toast.LENGTH_LONG)
                toast.show()
                finish()
            }
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "No") { dialog, which -> // No press
                    dialog.dismiss() }
            alertDialog.show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Recieves data from go back intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 42) {
            super.onActivityResult(requestCode, resultCode, data)
            val roomBundle = data?.getBundleExtra("roomBundle")
            chosenRoom.setName(roomBundle?.getString("name"))
            chosenRoom.setId(roomBundle?.getInt("id"))
            Log.d("MyTag", chosenRoom.getName())
        }
    }

    private fun toUnixTime(timestamp: String?): Int {
            val dt: Date = timeFormat.parse(timestamp)
            val epoch = dt.time
            return (epoch / 1000).toInt()
    }

    fun SaveRegistration(view: View) {
        if (fromTime.time.time>toTime.time.time){
            Toast.makeText(getApplicationContext(), "'To Date' cannot be before 'From Date'.", Toast.LENGTH_LONG).show()
        }
        else if (editPurposeText.text.toString().isNullOrEmpty() || editPurposeText.text.all { c -> c == ' ' }){
            Toast.makeText(getApplicationContext(), "Please enter a purpose", Toast.LENGTH_LONG).show()
        }
        else {
            val reservation = Reservation(1,toUnixTime(editFromTime.text.toString()),toUnixTime(editToTime.text.toString()),User.getInstance().firebaseUser.email,editPurposeText.text.toString(),chosenRoom.id)
            val modelStoreService = ApiUtils.getReservationService()
            var response = modelStoreService.postReservation(reservation)
            response.enqueue(object : Callback<Reservation?> {
                override fun onResponse(call: Call<Reservation?>, response: Response<Reservation?>) {
                    if (response.isSuccessful) {
                        Log.d("response", response.body().toString())
                        Toast.makeText(applicationContext, "Room successfully reserved.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else{
                        Log.e("Eroor",response.code().toString())
                        Log.e("Error", response.toString())
                        Toast.makeText(applicationContext, "Room is occupied. Please try another room.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Reservation?>, t: Throwable) {
                }
            })
        }

    }

}
