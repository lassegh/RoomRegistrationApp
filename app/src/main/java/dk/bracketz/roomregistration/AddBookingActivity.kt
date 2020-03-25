package dk.bracketz.roomregistration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dk.bracketz.roomregistration.model.Room
import dk.bracketz.roomregistration.model.User

class AddBookingActivity : AppCompatActivity() {

    var chosenRoom = Room()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_booking)

        val roomBundle = intent.getBundleExtra("room")
        setChosenRoom(roomBundle)

        //actionbar
        val actionbar = supportActionBar
        actionbar!!.title = "Book "+chosenRoom.name
        actionbar.setDisplayHomeAsUpEnabled(true)
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

}
