package dk.bracketz.roomregistration.helperclasses

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.widget.EditText
import android.icu.text.SimpleDateFormat

fun calendarSetter(c: Calendar, textField: EditText, context: Context, timeFormat: SimpleDateFormat): DatePickerDialog.OnDateSetListener {
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