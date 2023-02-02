package com.grandtech.mapframe.orm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu

import kotlinx.android.synthetic.main.activity_main.*

class SpatiaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true
    }


}
