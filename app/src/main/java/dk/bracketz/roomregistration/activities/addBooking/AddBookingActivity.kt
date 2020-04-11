package dk.bracketz.roomregistration.activities.addBooking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import dk.bracketz.roomregistration.R
import dk.bracketz.roomregistration.activities.room.RoomActivity
import dk.bracketz.roomregistration.model.Reservation
import dk.bracketz.roomregistration.model.Room
import dk.bracketz.roomregistration.helpers.ApiUtils
import dk.bracketz.roomregistration.helpers.FirebaseLogin
import kotlinx.android.synthetic.main.activity_add_booking.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class AddBookingActivity : AppCompatActivity() {

    private var chosenRoom = Room()
    private var timeFormat = SimpleDateFormat("dd/MM/yy - HH:mm", Locale.GERMAN)
    private val fromTime = Calendar.getInstance()!!
    private val toTime = Calendar.getInstance()!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_booking)

        val roomBundle = intent.getBundleExtra("room")
        setChosenRoom(roomBundle)

        // Set default dateTimes
        editFromTime.setText(timeFormat.format(fromTime.time))
        toTime.add(Calendar.HOUR,1)
        editToTime.setText(timeFormat.format(toTime.time))

        // From time picker
        editFromTime.setOnClickListener({
            DatePickerDialog(this,calendarSetter(fromTime, editFromTime, this, timeFormat), fromTime
                    .get(Calendar.YEAR), fromTime.get(Calendar.MONTH), fromTime.get(Calendar.DAY_OF_MONTH)).show()
        })

        // To time picker
        editToTime.setOnClickListener({
            DatePickerDialog(this,calendarSetter(toTime, editToTime, this, timeFormat), toTime
                    .get(Calendar.YEAR), toTime.get(Calendar.MONTH), toTime.get(Calendar.DAY_OF_MONTH)).show()
        })
    }



    override fun onStart() {
        super.onStart()
        val actionbar = supportActionBar
        actionbar!!.title = "Book "+chosenRoom.name
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun setChosenRoom(roomBundle:Bundle) {
        chosenRoom.name = roomBundle.getString("name")
        chosenRoom.id = roomBundle.getInt("id")
    }

    fun gotoSelectRoom() {
        // Create intent - go to select room activity
        val intent = Intent(this, RoomActivity::class.java)
        intent.putExtra("fromTime",toUnixTime(editFromTime.text.toString()))
        startActivityForResult(intent, 42)
    }

    // navigation bar buttons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_select_room) {
            gotoSelectRoom()
            return true
        }
        if (id == R.id.action_log_in) {
            // Spørg bruger om han ønsker at logge ud
            val alertDialog = AlertDialog.Builder(this@AddBookingActivity).create()
            alertDialog.setTitle("Logged in as " + FirebaseLogin.getInstance().firebaseUser.email)
            alertDialog.setMessage("Do you want to log out?")
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Yes"
            ) { dialog, which ->
                FirebaseLogin.getInstance().logout()
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
        if (requestCode == 42 && data != null) {
            super.onActivityResult(requestCode, resultCode, data)
            val roomBundle = data.getBundleExtra("roomBundle")
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

    fun preSave(view: View) {
        if (fromTime.time.time>toTime.time.time){
            Toast.makeText(applicationContext, "'To Date' cannot be before 'From Date'.", Toast.LENGTH_LONG).show()
        }
        else if (editPurposeText.text.toString().isEmpty() || editPurposeText.text.all { c -> c == ' ' }){
            Toast.makeText(applicationContext, "Please enter a purpose", Toast.LENGTH_LONG).show()
        }
        else {
            saveRegistration(view)
        }

    }

    private fun saveRegistration(view: View){
        val reservation = Reservation(1,toUnixTime(editFromTime.text.toString()),toUnixTime(editToTime.text.toString()), FirebaseLogin.getInstance().firebaseUser.email,editPurposeText.text.toString(),chosenRoom.id)
        val modelStoreService = ApiUtils.getReservationService()
        val response = modelStoreService.postReservation(reservation)
        response.enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    Log.d("MyTag", response.body().toString())
                    Toast.makeText(applicationContext, "Room successfully booked.", Toast.LENGTH_LONG).show()
                    FirebaseLogin.getInstance().checkUserChoice()
                    finish()
                }
                else{
                    Log.e("MyTag",response.code().toString())

                    // Dismiss keyboard
                    val keyboard = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    keyboard.hideSoftInputFromWindow(view.windowToken, 0)

                    // Snackbar
                    val snack = Snackbar.make(view,"Room is occupied.",Snackbar.LENGTH_LONG)
                    snack.setAction("Find available room", {
                        gotoSelectRoom()
                    })
                    snack.show()
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.e("MyTag",t.message)
            }
        })
    }

    private fun calendarSetter(c: Calendar, textField: EditText, context: Context, timeFormat: SimpleDateFormat): DatePickerDialog.OnDateSetListener {
        return DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            c[Calendar.YEAR] = year
            c[Calendar.MONTH] = monthOfYear
            c[Calendar.DAY_OF_MONTH] = dayOfMonth

            // Nested time picker
            TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                c[Calendar.HOUR_OF_DAY] = hourOfDay
                c[Calendar.MINUTE] = minute
                textField.setText(timeFormat.format(c.time))
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }
    }
}
