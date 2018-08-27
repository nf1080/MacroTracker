package com.fesefeldt.neal.macrotracker

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_calc_activiity.*
import android.content.DialogInterface
import android.view.View


class CalcActivity : AppCompatActivity() {

    private var BMR = 0.0
    private var LBM = 0.0
    private var totalWeight = 0.0
    private var bodyFat = 0.0
    private var TEE = 0.0
    private var activityVar = 0.0
    private var totalCals = 0.0
    private var protein = 0
    private var fat = 0
    private var carb = 0
    private var gain = false
    private var lose = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calc_activiity)

        weightButtonClick()
        bodyFatButtonClick()
        activityLevelButtonClick()
        setUpRadioButtons()
        calculateButtonClick()

        val info = intent
        if (info != null) {

            if(info.action == "myDesign.intent.GET_CALS"){
                acceptButton.setOnClickListener {
                    sendResult()
                }
            }
        }
    }

    private fun weightButtonClick() {

        weightButton.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Enter Weight")

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("Submit") { dialog, which ->
                totalWeight = input.text.toString().toDouble()
                Log.i("input", "totalWeight stored as $totalWeight")
                showInputText()
            }

            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
    }

    private fun bodyFatButtonClick() {

        bodFatButton.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Enter Approximate Bodyfat %")

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("Submit") { dialog, which ->
                bodyFat = input.text.toString().toDouble()
                Log.i("input", "totalWeight stored as $bodyFat")
                showInputText()
            }

            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun activityLevelButtonClick() {

        aLvlButton.setOnClickListener {

            val b = android.app.AlertDialog.Builder(this)
            b.setTitle("Select Daily Activity Level")
            val types = arrayOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Endurance Athlete")
            b.setItems(types) { dialog, which ->
                dialog.dismiss()
                when (which) {
                    0 -> activityVar = 1.2
                    1 -> activityVar = 1.3
                    2 -> activityVar = 1.5
                    3 -> activityVar = 1.7
                    4 -> activityVar = 2.0
                }
                Log.i("input", "Activity variable stored as $activityVar")
                showInputText()
            }

            b.show()
        }
    }

    private fun setUpRadioButtons() {

        radioMuscButton.setOnClickListener {
            gain = true
            lose = false
        }

        radioFatButton.setOnClickListener {
            lose = true
            gain = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showInputText() {

        showInputTextField.text = "BodyWeight is $totalWeight \n" +
                                  "BodyFat is $bodyFat \n" +
                                  "Activity Level is $activityVar"
    }

    @SuppressLint("SetTextI18n")
    private fun calculateButtonClick() {

        calculateButton.setOnClickListener {
            if ((totalWeight == 0.0 || bodyFat == 0.0 || activityVar == 0.0)
                    || (!gain && !lose)) {

                Toast.makeText(this, "Please input all required data above",
                        Toast.LENGTH_LONG).show()

            } else {

                val kgWeight = (totalWeight * 0.453592)
                var gainLose = ""

                LBM = ((kgWeight * (100 - bodyFat)) / 100)
                BMR = 370 + (21.6 * LBM)
                TEE = (BMR * activityVar)

                if (gain) {
                    totalCals = TEE + (TEE * 0.15)
                    gainLose = "gain"
                } else if (lose) {
                    totalCals =  TEE - (TEE * 0.15)
                    gainLose = "lose"
                }

                protein = (2.25 * kgWeight).toInt()
                fat = (.95 * kgWeight).toInt()
                var proteinCals = protein * 4
                var fatCals = fat * 9

                carb = ((totalCals - (fatCals + proteinCals))/ 4).toInt()

                showCaloriesTextField.text = "-Your daily TEE(Total Energy Expenditure) is " +
                                             Math.floor(TEE).toInt() + " cals" +
                                             "\n-You need to eat " + Math.floor(totalCals).toInt() +
                                             " calories a day to $gainLose 1 lb/week" +
                                             "\n-You should eat $protein grams of protein, $fat " +
                                             "grams of fat, and $carb grams of carbs per day"

                acceptButton.visibility = View.VISIBLE
            }
        }
    }

    private fun sendResult(){

        intent!!.putExtra("Calories", totalCals.toInt())
        intent!!.putExtra("Protein", protein )
        intent!!.putExtra("Carbs", carb)
        intent!!.putExtra("Fats", fat)

        setResult(68, intent)
        super.finish()
    }
}
