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
                    "Help page 1",
                    "This text is written on a gradient background",
                    imageDrawable = R.drawable.man1,
                    backgroundDrawable = R.drawable.back_slide2
                )
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    "Gradients!",
                    "This text is written on a gradient background",
                    imageDrawable = R.drawable.man2,
                    backgroundDrawable = R.drawable.back_slide2
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