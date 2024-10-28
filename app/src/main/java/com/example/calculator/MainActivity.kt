package com.example.calculator

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
//TODO
//+-

class MainActivity : AppCompatActivity() {
    private lateinit var digitButtons: List<TextView>
    private lateinit var operationButtons: List<TextView>

    private var currentOperation: TextView? = null
    private var firstNumber: Double = 0.0
    private lateinit var expressionTextField: TextView
    private var firstClick = true
    private var pressedEquals = false
    private var pressedOperation = false
    private var isFloatEntered = false
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        expressionTextField = findViewById<TextView>(R.id.expression_text_field)
        digitButtons = listOf(
            findViewById(R.id.eins_button),
            findViewById(R.id.zwie_button),
            findViewById(R.id.drei_button),
            findViewById(R.id.vier_button),
            findViewById(R.id.fünf_button),
            findViewById(R.id.sechs_button),
            findViewById(R.id.seben_button),
            findViewById(R.id.acht_button),
            findViewById(R.id.neun_button),
            findViewById(R.id.zero_button),
        )
        operationButtons = listOf(
            findViewById(R.id.div_button),
            //findViewById(R.id.plus_minus_button),
            findViewById(R.id.inc_button),
            //findViewById(R.id.float_button),
            findViewById(R.id.mlp_button),
            findViewById(R.id.mns_button),
        )
        val equalButton = findViewById<TextView>(R.id.equals_button)
        val acButton = findViewById<TextView>(R.id.ac_button)
        val floatButton = findViewById<TextView>(R.id.float_button)


        val commonClickListener = View.OnClickListener { view ->
            if (firstClick) {
                expressionTextField.text = ""
                firstClick = false
            }

            if (pressedEquals) {
                expressionTextField.text = ""
                pressedEquals = false
            }

            when (view) {
                in digitButtons -> addDigitListener(view as TextView)
                in operationButtons -> addOperationListener(view as TextView)
                equalButton -> onEqualsPressed()
                acButton -> onACPressed()
                floatButton -> onFloatButton()
            }
        }

        digitButtons.forEach { it.setOnClickListener(commonClickListener) }
        operationButtons.forEach { it.setOnClickListener(commonClickListener) }
        equalButton.setOnClickListener(commonClickListener)
        acButton.setOnClickListener(commonClickListener)
        floatButton.setOnClickListener(commonClickListener)

        gestureDetector = GestureDetector(this, SwipeGestureListener())

        expressionTextField.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun onFloatButton() {
        if (!isFloatEntered && !pressedOperation) {
            isFloatEntered = false
            if (expressionTextField.text.isEmpty() || expressionTextField.text == "0") {
                expressionTextField.text = "0."
            } else {
                expressionTextField.append(".")
            }
        }
    }


    private fun addDigitListener(button: TextView) {
        if (pressedOperation) {
            expressionTextField.text = ""
            pressedOperation = false
            isFloatEntered = false
        }
        val currentText = expressionTextField.text.toString()

        if (currentText == "0" && button.text == "0") return

        expressionTextField.text = if (currentText == "0") {
            button.text
        } else {
            currentText + button.text
        }
        currentOperation?.apply {
            setBackgroundResource(R.drawable.orange_buttons)
            setTextColor(resources.getColor(R.color.white))
        }

    }

    private fun addOperationListener(button: TextView) {
        pressedOperation = true
        isFloatEntered = false
        firstNumber = expressionTextField.text.toString().toDoubleOrNull() ?: firstNumber
        expressionTextField.text = dropZeroFractPart(firstNumber)

        currentOperation?.apply {
            setBackgroundResource(R.drawable.orange_buttons)
            setTextColor(ContextCompat.getColor(context, R.color.button_text_selector))
        }
            currentOperation = button
            button.setBackgroundResource(R.drawable.active_orange_button)
            button.setTextColor(resources.getColor(R.color.orange))
        }




    private fun onEqualsPressed() {
        val secondNumber = expressionTextField.text.toString().toDoubleOrNull() ?: 0.0
        var result = 0.0
        isFloatEntered = false
        pressedEquals = true

        when (currentOperation?.text) {
            "+" -> result = firstNumber + secondNumber
            "−" -> result = firstNumber - secondNumber
            "÷" -> if (secondNumber != 0.0) result = firstNumber / secondNumber
            else {
                expressionTextField.text = "Error"
                return
            }

            "×" -> result = firstNumber * secondNumber
            "=" -> result = expressionTextField.text.toString().toDouble()
        }

        currentOperation?.apply {
            setBackgroundResource(R.drawable.orange_buttons)
            setTextColor(resources.getColor(R.color.white))
        }

        currentOperation = null

        expressionTextField.text = dropZeroFractPart(result)
        firstNumber = result
    }

    private fun dropZeroFractPart(number : Double) : String {
        if (number % 1.0 == 0.0) {
            return number.toInt().toString()
        }
        else {
            return number.toString()
        }
    }

    private fun onACPressed() {
        isFloatEntered = false
        expressionTextField.text = ""
        firstNumber = 0.0
        currentOperation?.apply {
            setBackgroundResource(R.drawable.orange_buttons)
            setTextColor(resources.getColor(R.color.white))
        }
        firstClick = true
    }

    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null || e2 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (Math.abs(diffX) > Math.abs(diffY) &&
                Math.abs(diffX) > SWIPE_THRESHOLD &&
                Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
            ) {
                if (diffX > 0) {
                    val currentText = expressionTextField.text.toString()
                    if (currentText.isNotEmpty()) {
                        expressionTextField.text = currentText.dropLast(1)
                    }
                    return true
                }
            }
            return false
        }
    }
}


