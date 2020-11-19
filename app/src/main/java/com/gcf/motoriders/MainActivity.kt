package com.gcf.motoriders

import android.os.Bundle
import android.view.View
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    var simpleListCar: GridView? = null
    var simpleListBike: GridView? = null
    var simpleListElec: GridView? = null
    var carList: ArrayList<Item> =  ArrayList()
    var bikeList: ArrayList<Item> =  ArrayList()
    var elecList: ArrayList<Item> =  ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        simpleListCar = findViewById<View>(R.id.simpleGridView) as GridView
        simpleListBike = findViewById<View>(R.id.simpleGridViewbike) as GridView
        simpleListElec = findViewById<View>(R.id.simpleGridViewElec) as GridView
        carList.add(Item("bumper repainting", R.drawable.bumper_repainting))
        carList.add(Item("car dent repairing", R.drawable.car_dent_repair_and_painting_144))
        carList.add(Item("car detailing", R.drawable.car_detailing_144))
        carList.add(Item("complete car spa", R.drawable.complete_car_spa_144))
        carList.add(Item("Oil Change", R.drawable.oil))
        bikeList.add(Item("Premium Service",R.drawable.premium_bike))
        bikeList.add(Item("General Service",R.drawable.general_service))
        bikeList.add(Item("Repair Job",R.drawable.repair_job))
        elecList.add(Item("AC",R.drawable.ac))
        elecList.add(Item("refrigerator",R.drawable.refrigerator))
        elecList.add(Item("washing machine",R.drawable.washing_machine))
        elecList.add(Item("purifier",R.drawable.purifier))
        val myAdapter = MyAdapter(this, R.layout.item_view, carList)
        simpleListCar!!.adapter = myAdapter
        val myAdapter1 = MyAdapter(this, R.layout.item_view, bikeList)
        simpleListBike!!.adapter = myAdapter1
        val myAdapter2 = MyAdapter(this, R.layout.item_view, elecList)
        simpleListElec!!.adapter = myAdapter2
    }
}