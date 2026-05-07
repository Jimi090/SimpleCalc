package com.example.calculator4android

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator4android.databinding.ActivityMainBinding
import net.objecthunter.exp4j.ExpressionBuilder
import androidx.activity.viewModels
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainVM by viewModels<MainViewModel>()
    val current = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    val HISTORY_KEY = "HISTORY_KEY" //saved as string
    val REQUEST_KEY = "REQUEST_KEY"
    val DATA_KEY = "DATA_KEY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.main)

        val sharedPreferences = getSharedPreferences("Calculator",MODE_PRIVATE)
        val savedHistoryJson = sharedPreferences.getString(HISTORY_KEY,null)

        if(savedHistoryJson!=null){
            val type = object : TypeToken<List<HistoryElement>>() {}.type
            val list: List<HistoryElement> = Gson().fromJson(savedHistoryJson, type)
            mainVM.history = list.toMutableList()
        }

        updateText(mainVM.equation)
        val idList = mapOf(
            binding.one to 1,
            binding.two to 2,
            binding.three to 3,
            binding.four to 4,
            binding.five to 5,
            binding.six to 6,
            binding.seven to 7,
            binding.eight to 8,
            binding.nine to 9,
            binding.zero to 0,
            binding.plus to "+",
            binding.minus to "-",
            binding.multiply to "*",
            binding.division to "/",
            binding.precent to "%",
            binding.point to "."
        )

        idList.forEach { (button, value) ->
            button.setOnClickListener {
                if (mainVM.equation == "0" && isNumber(value.toString()) && value!=".") {
                    mainVM.equation=value.toString()
                    updateText(mainVM.equation)
                } else {
                    val operants = arrayOf("+","-","*","/","%")
                    if(mainVM.equation[mainVM.equation.lastIndex].toString() in operants && value in operants){
                        if(mainVM.equation[mainVM.equation.lastIndex].toString() in arrayOf("*","/") && value=="-"){
                            mainVM.equation+=value.toString()
                            updateText(mainVM.equation)
                        }else{
                            mainVM.equation = mainVM.equation.dropLast(1) + value
                            updateText(mainVM.equation)
                        }
                    }else{
                        mainVM.equation+=value.toString()
                        updateText(mainVM.equation)
                    }
                }
                updateEquationBox("")
            }

        }
        binding.equal.setOnClickListener {
            try {
                if(!isEquation(mainVM.equation)){
                    Log.d("XD",mainVM.equation)
                    return@setOnClickListener
                }
                Log.d("XD",mainVM.equation)
                if(mainVM.equation=="2+2"){
                    Toast.makeText(this, "Do you really need to use a calculator for this?", Toast.LENGTH_SHORT).show()
                }
                val list = mainVM.equation.split(Regex("(?=[+\\-*/])|(?<=[+\\-*/])")).toMutableList()
                updateEquationBox(mainVM.equation)
                for (i in list.indices){
                    if ("%" in list[i]){
                        list[i] = list[i].removeSuffix("%")
                        list[i] = (list[i].toDouble()*0.01).toString()
                    }
                }
                mainVM.equation = list.joinToString("","","")
                var result =
                    ExpressionBuilder(mainVM.equation).build().evaluate().toString()
                if (result.takeLast(2) == ".0") {
                    result = result.dropLast(2)
                }
                mainVM.history.add(HistoryElement(beautifyOutput(mainVM.equation), beautifyOutput(result), current))
                mainVM.equation=result
                updateText(result)
            } catch (e: Exception) {
                Log.d("XD",e.toString())
            }
        }
        binding.delete.setOnClickListener {
            if (mainVM.equation.isNotEmpty()) {
                if(mainVM.equation.length==1){
                    mainVM.equation="0"
                }else {
                    mainVM.equation = mainVM.equation.substring(0, mainVM.equation.length - 1)
                }
                updateEquationBox("")
                updateText(mainVM.equation)
            }
        }
        binding.clear.setOnClickListener {
            mainVM.equation = "0"
            updateEquationBox("")
            updateText(mainVM.equation)
        }
        binding.signChange.setOnClickListener {
            val list = mainVM.equation.split(Regex("(?=[+\\-*/])|(?<=[+\\-*/])")).toMutableList()
            var lastElement = list[list.lastIndex]

            if (isNumber(lastElement)) {
                if(list.size>1){
                    if(list[list.lastIndex-1]=="-" && list[1]=="-"){
                        list[list.lastIndex-1] = ""
                    }else if(list[list.lastIndex-1]=="-"){
                        list[list.lastIndex-1] = "+"
                    }else if(list[list.lastIndex-1]=="+"){
                        list[list.lastIndex-1] = "-"
                    }else {
                        list[list.lastIndex] = "(-${list.last()})"
                    }
                }else{list[list.lastIndex] = "(-${list.last()})"}
            }
            if (list.size > 2) {
                if (list[list.size - 3] == "(" && list[list.size - 2] == "-" && lastElement.takeLast(
                        1
                    ) == ")"
                ) {
                    list.removeAt(list.size - 2)
                    list.removeAt(list.size - 2)
                    lastElement = lastElement.substring(0, lastElement.length - 1)
                    list[list.lastIndex] = lastElement
                }
            }
            mainVM.equation = list.joinToString("", "", "")
            updateText(mainVM.equation)
            updateEquationBox("")
        }
        binding.history.setOnClickListener {
            openHistory()
        }
        supportFragmentManager.setFragmentResultListener(
            REQUEST_KEY,this
        ){
            _,bundle->
            val type = object : TypeToken<HistoryElement>() {}.type
            val item: HistoryElement = Gson().fromJson(bundle.getString(DATA_KEY),type)

            mainVM.equation = item.result
            updateText(mainVM.equation)
            updateEquationBox(item.equation)
        }
    }

    override fun onPause() {
        super.onPause()
        val jsonHistory = Gson().toJson(mainVM.history)
        val sharedPreferences = getSharedPreferences("Calculator",MODE_PRIVATE)
        sharedPreferences.edit { putString(HISTORY_KEY, jsonHistory) }
    }

    fun updateText(text: String) {
        val textField = binding.textField
        val textContainer = binding.textContainer
        val newText = beautifyOutput(text)

        textField.setText(newText)
        textContainer.post {
            textContainer.fullScroll(View.FOCUS_RIGHT)
        }
    }
    fun updateEquationBox(text: String){
        val newText = beautifyOutput(text)
        binding.equationBox.setText(newText)
    }
    fun openHistory() {
        val window = HistoryWindow()
        window.show(supportFragmentManager, "History")
    }
    fun clearHistory(){
        mainVM.history.clear()
    }
    fun beautifyOutput(text: String): String{
        var newText = text
        newText = newText.replace("*","\u00d7")
        newText = newText.replace("/","\u00f7")

        return newText
    }
    fun isEquation(equation: String): Boolean{
        if("+" in equation || "-" in equation || "*" in equation || "/" in equation || "%" in equation){
            var numbers = 0
            val list = equation.split(Regex("(?=[+\\-*/%])|(?<=[+\\-*/%])")).toMutableList()
            for (i in list){
                if(i.isNotEmpty()) {
                    var a: String
                    if (i[i.lastIndex].toString() == ")") {
                        a = i.dropLast(1)
                    } else {
                        a = i
                    }
                    if (isNumber(a)) {
                        numbers++
                    }
                }
            }
            if(numbers>1){
                return true
            }
            else if(numbers==1 && "%" in equation){
                return true
            }
            else{
                return false
            }
        }else{ return false }
    }
}

fun isNumber(input: String): Boolean {
    if(input.isEmpty() || input=="%"){
        return false
    }
    val integerChars = '0'..'9'
    var dotOccurred = 0
    return input.all { it in integerChars || it == '.' && dotOccurred++ < 1 }
}
