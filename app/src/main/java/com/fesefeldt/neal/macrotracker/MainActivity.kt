package com.fesefeldt.neal.macrotracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_calc_activiity.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase



class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager : SensorManager? = null
    private var stepTracker : Sensor? = null
    private var mDatabase: DatabaseReference? = null


    private var file = File("/data/user/0/com.fesefeldt.neal.macrotracker/files", "calData")
   // private var file1 = File(this.filesDir.path, "calData2")

    private var requestCode = 99
    private var calsIntent = "myDesign.intent.GET_CALS"
    private var calories = 0
    private var protein = 0
    private var carb = 0
    private var fat = 0
    private var running = false
    private var stepCals = 0
    private var steps = 0

    class CalorieObj (var cals: Int, var proteins: Int, var fats: Int,  var carbs: Int, var step: Int )

    class NewUser(_cals: Int, _proteins: Int, _fats: Int,  _carbs: Int){

        var cals: Int = _cals
        var proteins: Int = _proteins
        var fats: Int = _fats
        var carbs: Int = _carbs
    }

    private var saveData: ArrayList<CalorieObj> = ArrayList()
    private var userData = CalorieObj(calories, protein, carb, fat, steps)


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        requestButton.setOnClickListener {
            sendMessage(it, requestCode)
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        resetButton.setOnClickListener {
            steps = 0
            stepCount.text = "$steps steps"
        }

        dBbutton.setOnClickListener {

           val userTemp = NewUser(calories, protein, fat, carb)
            mDatabase = FirebaseDatabase.getInstance().getReference()
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("message")
            myRef.setValue("Can you see this??")

            val userRef = database.getReference("New User")
            userRef.setValue(userTemp)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        if(saveData.size > 0) {
            saveData[0].step = steps
            saveArrayList(saveData)
        }

       // val database = FirebaseDatabase.getInstance()
       // val myRef = database.getReference("message")

       // myRef.setValue("Can you see this??")


        // if you unregister, the hardware will stop detecting steps when app is in background
        //  sensorManager!!.unregisterListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        running = true
        stepTracker = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if(stepTracker != null){
            sensorManager!!.registerListener(this, stepTracker, SensorManager.SENSOR_DELAY_UI)
        } else {
            Toast.makeText(this, "Sensor Not Found", Toast.LENGTH_SHORT).show()
        }

            var tempList: ArrayList<String> = getSavedArrayList()

            if (tempList.size > 4) {
                Log.i("MacroTracker", "List not empty. size of tempList is: " + tempList.size)
                Log.i("MacroTracker", "tempList[0] value contains: " + tempList[0])
                Log.i("MacroTracker", "tempList[1] value contains: " + tempList[1])
                Log.i("MacroTracker", "tempList[2] value contains: " + tempList[2])
                Log.i("MacroTracker", "tempList[3] value contains: " + tempList[3])
                Log.i("MacroTracker", "tempList[4] value contains: " + tempList[4])
                for (i in 0 until tempList.size step 5) {
                    val oldData = CalorieObj(tempList[i].toInt(), tempList[i + 1].toInt(),
                            tempList[i + 2].toInt(), tempList[i + 3].toInt(), tempList[i + 4].toInt())
                    saveData.add(oldData)
                    Log.i("MacroTracker", "for loop executed. value of i is: " + i)
                }

                calories = saveData[0].cals
                protein = saveData[0].proteins
                fat = saveData[0].fats
                carb = saveData[0].carbs
                steps = saveData[0].step

                tempText.text = "Calories needed: $calories" +
                        "\nProtein needed: $protein grams" +
                        "\nFats needed: $fat grams" +
                        "\nCarbs needed: $carb grams"
                stepCount.text = saveData[0].step.toString() + " steps"
            } else {
                Log.i("ColorPicker", "List is empty")
            }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {

        if(running){
            steps++
            stepCount.text = "$steps steps"
            // steps = event!!.values[0].toInt()
            // stepCount.text = "$steps steps"
            if((event!!.values[0].toInt()) / 20 == 0)
                stepCals ++
        }
    }

    private fun sendMessage(view: View, i: Int){

        val intent = Intent(calsIntent)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        startActivityForResult(intent, i)
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // super.onActivityResult(requestCode, resultCode, data)

        when (requestCode)  {
            requestCode -> {
                when (resultCode) {
                    68 -> {
                        calories = data!!.getIntExtra("Calories", 0)
                        protein = data.getIntExtra("Protein", 0)
                        carb = data.getIntExtra("Carbs", 0)
                        fat = data.getIntExtra("Fats", 0)

                        tempText.text = "Calories needed: $calories" +
                                        "\nProtein needed: $protein grams" +
                                        "\nFats needed: $fat grams" +
                                        "\nCarbs needed: $carb grams"
                        userData = CalorieObj(calories, protein, carb, fat, steps)
                        saveData.add(userData)
                        saveData[0].cals = calories
                        saveData[0].proteins = protein
                        saveData[0].fats = fat
                        saveData[0].carbs = carb
                    }
                }
            }
        }
    }

    private fun saveArrayList(list: ArrayList<CalorieObj>) {

        try {
            File(file.toString()).printWriter().use { out ->
                list.forEach {
                    //out.println(it.name + " " + it.redVal + " " + it.blueVal + " " + it.greenVal)
                    out.println(it.cals)
                    out.println(it.proteins)
                    out.println(it.fats)
                    out.println(it.carbs)
                    out.println(it.step)
                }
                Log.i("MacroTracker","List Written")
            }
        } catch (e: Exception) {
            Log.e("InternalStorage", "Write fail.")
        }
    }

    private fun getSavedArrayList(): ArrayList<String> {


            var toReturn: ArrayList<String> = ArrayList()


                try {
                    File(file.toString()).inputStream().use {

                        Log.i("MacroTracker", "File Exists ")
                    val file = InputStreamReader(openFileInput("calData"))
                    val br = BufferedReader(file)
                    Log.i("MacroTracker", "File Located")
                    var line = br.readLine()

                    if (line == null)
                        Log.i("MacroTracker", "File is Empty")

                    while (line != null) {

                        toReturn.add(line)
                        Log.i("MacroTracker", "line read and added $line to list")
                        line = br.readLine()
                    }

                    file.close()
                    br.close()
                }

                } catch (e: FileNotFoundException) {
                    Log.e("InternalStorage", "File not found Exception")
                } catch (e: IOException) {
                    Log.e("InternalStorage", "IO Exception")
                }


            return toReturn

    }

    private fun addObjToDb(){

    }

}
