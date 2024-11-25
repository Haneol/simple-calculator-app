package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Stack

class MainActivity : AppCompatActivity() {
    lateinit var text : TextView
    var isZero : Boolean = true                 // 현재 숫자가 0인지 체크하는 변수
    var isLastCharOperator : Boolean = false    // 마지막이 연산자인지 체크하는 변수
    var isCalculationFinish : Boolean = false   // 연산이 끝났는지 확인하는 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        text = findViewById(R.id.textView)

        // 숫자 버튼
        val button = arrayOf(
            findViewById<Button>(R.id.button0),
            findViewById<Button>(R.id.button1),
            findViewById<Button>(R.id.button2),
            findViewById<Button>(R.id.button3),
            findViewById<Button>(R.id.button4),
            findViewById<Button>(R.id.button5),
            findViewById<Button>(R.id.button6),
            findViewById<Button>(R.id.button7),
            findViewById<Button>(R.id.button8),
            findViewById<Button>(R.id.button9)
        )

        // 연산자 버튼
        val buttonOp = arrayOf(
            findViewById<Button>(R.id.buttonPlus),
            findViewById<Button>(R.id.buttonMinus),
            findViewById<Button>(R.id.buttonMult),
            findViewById<Button>(R.id.buttonDivide)
        )

        val buttonReset = findViewById<Button>(R.id.buttonReset)
        val buttonCalc = findViewById<Button>(R.id.buttonCalc)

        // 리셋(c) 누르면 0으로 초기화
        buttonReset.setOnClickListener {
            text.text = "0"
            isZero = true
        }

        // 숫자 버튼 누를 때 이벤트
        button.forEach {
            it.setOnClickListener { view ->
                setNumberKeypadClicked((view as Button).text.toString())
            }
        }

        // 연산자 버튼 누를 때 이벤트
        buttonOp.forEach {
            it.setOnClickListener { view ->
                setOperatorKeypadClicked((view as Button).text.toString())
            }
        }

        // 계산 이벤트
        buttonCalc.setOnClickListener {
            val expression = text.text.toString()

            text.text = calc(expression)
            isCalculationFinish = true
        }

    }

    /**
     * 숫자 입력 함수
     *
     * 0의 경우 중복 입력되지 않음
     */
    private fun setNumberKeypadClicked(number: String) {
        // 계산 직후 이벤트 받는 경우 초기화
        if(isCalculationFinish) {
            text.text = "0"
            isZero = true
            isCalculationFinish = false
        }

        val currentText = text.text.toString()

        // 현재 숫자 구하기
        val currentNumber = if (currentText.isEmpty()) ""
        else currentText.split(" ").last()

        when {
            // 1. 현재 숫자가 "0"이고 새로운 0을 입력한 경우 -> 무시
            currentNumber == "0" && number == "0" -> return

            // 2. 현재 숫자가 "0"이고 다른 숫자를 입력한 경우 -> 0을 새 숫자로 교체
            currentNumber == "0" && number != "0" -> {
                text.text = currentText.substring(0, currentText.length - 1) + number
            }

            // 3. 새로운 숫자 입력 시작 (연산자 다음)
            isLastCharOperator -> {
                text.text = currentText + number
            }

            // 4. 일반적인 숫자 이어붙이기
            else -> {
                text.text = currentText + number
            }
        }

        isLastCharOperator = false  // 숫자를 입력 받았으므로 마지막 char는 연산자가 아님
    }

    /**
     * 연산자 입력 함수
     *
     * 마지막이 연산자라면, 연산자 치환
     */
    private fun setOperatorKeypadClicked(op: String) {
        // 계산 직후 이벤트 받는 경우 초기화
        if(isCalculationFinish) {
            text.text = "0"
            isZero = true
            isCalculationFinish = false
        }

        val currentText = text.text.toString()

        if(!isLastCharOperator) {   // 마지막이 숫자라면 연산자 추가
            text.text = currentText + " $op "
        } else {    // 마지막이 연산자라면 연산자 덮어쓰기
            text.text = currentText.substring(0, currentText.length - 2) + "$op "
        }

        isLastCharOperator = true   // 연산자를 입력 받았으므로 true로 체크
    }

    /**
     * 수식 계산 함수
     */
    private fun calc(expression : String) : String {
        try {
            var number = "" // 숫자 단위를 입력받기 위한 임시 변수

            val numStack = Stack<Double>()  // 숫자 스택
            val opStack = Stack<Char>() // 연산자 스택

            /**
             *  연산자 우선순위 설정 함수
             *
             *  높을 수록 우선순위 높음
             */
            fun getPriority(op: Char): Int = when(op) {
                '+', '-' -> 1
                '*', '/' -> 2
                else -> 0
            }

            /**
             * 계산 함수
             *
             * 0으로 나뉘는 경우에는 ArithmeticException 에러
             */
            fun calculate(): Double {
                val b = numStack.pop()
                val a = numStack.pop()
                val op = opStack.pop()

                return when(op) {
                    '+' -> a + b
                    '-' -> a - b
                    '*' -> a * b
                    '/' -> if(b == 0.0) throw ArithmeticException("Division by zero") else a / b
                    else -> 0.0
                }
            }

            /**
             * 실제 계산 동작부 시작
             *
             * String을 받아서 전체 수식 연산 결과를 도출한다.
             */
            for(ch in expression) {
                if(ch in '0'..'9') {    // 숫자면 일단 계속 입력 받기
                    number += ch
                } else if(ch in "+-*/") {   // 연산자라면..
                    // 1. 지금까지 입력받던 숫자가 있다면 숫자 스택에 push
                    if (number.isNotEmpty()) {
                        numStack.push(number.toDouble())
                        number = ""
                    }

                    // 2. 연산자 우선순위에 따라 계산, 계산된 결과는 다시 숫자 스택에 push
                    while (opStack.isNotEmpty() &&
                        getPriority(opStack.last()) >= getPriority(ch)) {
                        numStack.push(calculate())
                    }

                    // 3. 해당 연산자 스택에 push
                    opStack.push(ch)
                }
            }

            // 계산이 끝나고도 숫자가 아직 남아있다면, 숫자 스택에 추가
            if (number.isNotEmpty()) {
                numStack.push(number.toDouble())
            }

            // 남은 연산자들 처리, 단 숫자 스택 사이즈가 2보다 작다면 계산이 불가능(계산 끝)하므로, 수행하지 않음
            while (opStack.isNotEmpty() && numStack.size > 1) {
                numStack.push(calculate())
            }

            return if(opStack.isNotEmpty()) {
                // 연산자 스택에 값이 남아있다면, 마지막 입력이 연산자라는 의미이므로,
                // 수식에서 마지막 연산자 부분을 제거 후 출력
                expression.substring(0, expression.length - 3) + "\n= " + numStack.last().toString()
            } else {
                // 아니라면 결과 값 출력
                expression + "\n= " + numStack.last().toString()
            }
        } catch (e : ArithmeticException) {
            // 0으로 나눈 경우 undefined
            return "undefined"
        }
    }
}