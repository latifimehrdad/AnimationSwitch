package ir.agaring.switchanimation

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import ir.agaring.animationswitch.AnimationSwitch
import ir.agaring.animationswitch.EnumSwitchPosition

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val animationSwitch = findViewById<AnimationSwitch>(R.id.dayNightSwitch)

        val theme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == 32
        if (theme)
            animationSwitch.setPosition(position = EnumSwitchPosition.RIGHT, isAnimated = false)
        else
            animationSwitch.setPosition(position = EnumSwitchPosition.LEFT, isAnimated = false)

        animationSwitch.setOnSwitchListener {
            when(animationSwitch.currentPosition()) {
                EnumSwitchPosition.RIGHT ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                EnumSwitchPosition.LEFT ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

    }

}