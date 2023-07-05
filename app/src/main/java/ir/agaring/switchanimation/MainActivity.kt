package ir.agaring.switchanimation

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import ir.agaring.animationswitch.DayNightSwitch

class MainActivity : AppCompatActivity() {

    var day = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dayNightSwitch = findViewById<DayNightSwitch>(R.id.dayNightSwitch)

        val theme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == 32

        dayNightSwitch.setDayChecked(isDayChecked = theme, isAnimated = false)

        dayNightSwitch.setOnSwitchListener {
            if (!dayNightSwitch.isDayChecked())
                changeApplicationTheme(Configuration.UI_MODE_NIGHT_YES)
            else
                changeApplicationTheme(Configuration.UI_MODE_NIGHT_NO)
        }

        val switchIO = findViewById<SwitchCompat>(R.id.switchIO)
        switchIO.isChecked = theme

/*        switchIO.setOnClickListener {
            if (switchIO.isChecked)
                changeApplicationTheme(Configuration.UI_MODE_NIGHT_YES)
            else
                changeApplicationTheme(Configuration.UI_MODE_NIGHT_NO)
        }*/

/*        val layout1 = findViewById<ConstraintLayout>(R.id.layout1)
        layout1.setOnClickListener {
            if (dayNightSwitch.isDayChecked())
                changeApplicationTheme(Configuration.UI_MODE_NIGHT_YES)
            else
                changeApplicationTheme(Configuration.UI_MODE_NIGHT_NO)
        }*/


    }


    //______________________________________________________________________________________________ changeApplicationTheme
    fun changeApplicationTheme(theme: Int) {
        when (theme) {
            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
    //______________________________________________________________________________________________ changeApplicationTheme

}