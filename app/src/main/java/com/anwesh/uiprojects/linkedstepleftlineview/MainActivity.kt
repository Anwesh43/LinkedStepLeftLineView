package com.anwesh.uiprojects.linkedstepleftlineview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.steplineview.StepLeftLineView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StepLeftLineView.create(this)
    }
}
