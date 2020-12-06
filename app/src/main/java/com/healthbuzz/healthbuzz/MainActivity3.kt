package com.healthbuzz.healthbuzz

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.model.SliderPage

class MainActivity3 : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main3)
        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    "page one",
                    "Welcome! We are here to guard your health. \n" +
                            "In the main page, you can find useful dashboards.",
                    imageDrawable = R.drawable.helppage1,
                    backgroundDrawable = R.drawable.back_slide1
                )
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    "Page two",
                    "You can enable Stretch and Water drinking functionalities by flipping the switch. \n" +
                            "Make sure you have a paired watch that has our app installed. Otherwise, you can add manually by the + button! Click on any of these dashboards to see the detail view!\n",
                    imageDrawable = R.drawable.helppage4,
                    backgroundDrawable = R.drawable.back_slide2
                )
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    "Page three",
                    "When we detect a watch, you will see this watch logo added in the header.",
                    imageDrawable = R.drawable.helppage21,
                    backgroundDrawable = R.drawable.back_slide2
                )
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    "Page four",
                    "Make sure to login first to join the community! You can share your accomplishments with other users! (Settings/Login)\n",
                    imageDrawable = R.drawable.helppage5,
                    backgroundDrawable = R.drawable.back_slide3
                )
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    "Page five",
                    "You will save this thirsty tree and the tired man when you drink and stretch! \n" +
                            "Make sure to check out how they look throughout the day! \n",
                    imageDrawable = R.drawable.helppage3,
                    backgroundDrawable = R.drawable.back_slide4
                )
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    "Page six" +
                            "",
                    "In the setting page, you will be able to customize the time interval to receive a reminder.",
                    imageDrawable = R.drawable.helppage2,
                    backgroundDrawable = R.drawable.back_slide5
                )
            )
        )


    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        startActivity(Intent(this@MainActivity3, MainActivity::class.java))
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        startActivity(Intent(this@MainActivity3, MainActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        startActivity(Intent(this@MainActivity3, MainActivity::class.java))
        finish()
    }
}