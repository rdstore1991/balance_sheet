package com.mfstracker.app.util

import java.util.Locale

object MathExpressionEvaluator {

    /**
     * Checks if the given input string contains any basic arithmetic operator (+, -, *, /).
     */
    fun containsMathOperators(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return false
        // Exclude a leading unary minus or plus if it's the only operator
        val withoutLeadingSign = if (trimmed.startsWith("+") || trimmed.startsWith("-")) {
            trimmed.substring(1)
        } else {
            trimmed
        }
        return withoutLeadingSign.any { it == '+' || it == '-' || it == '*' || it == '/' }
    }

    /**
     * Evaluates a math expression string containing numbers and +, -, *, / operators.
     * Returns null if the expression is invalid, incomplete, or divides by zero.
     */
    fun evaluateMathExpression(input: String): Double? {
        val cleaned = input.trim().replace(",", "").replace(" ", "")
        if (cleaned.isEmpty()) return null
        if (cleaned == "." || cleaned == "-" || cleaned == "+") return null

        return runCatching {
            val parser = SimpleParser(cleaned)
            val result = parser.parse()
            if (result.isFinite() && !result.isNaN()) result else null
        }.getOrNull()
    }

    private class SimpleParser(private val text: String) {
        private var pos = 0

        fun parse(): Double {
            val value = parseExpression()
            if (pos < text.length) {
                // If there are unconsumed trailing characters like an unfinished operator (e.g. "100+"),
                // it is considered incomplete
                throw IllegalArgumentException("Unexpected character at $pos")
            }
            return value
        }

        // Expression = Term { ('+' | '-') Term }
        private fun parseExpression(): Double {
            var value = parseTerm()
            while (pos < text.length) {
                val op = text[pos]
                if (op != '+' && op != '-') break
                pos++
                val nextTerm = parseTerm()
                if (op == '+') value += nextTerm else value -= nextTerm
            }
            return value
        }

        // Term = Factor { ('*' | '/') Factor }
        private fun parseTerm(): Double {
            var value = parseFactor()
            while (pos < text.length) {
                val op = text[pos]
                if (op != '*' && op != '/') break
                pos++
                val nextFactor = parseFactor()
                if (op == '*') {
                    value *= nextFactor
                } else {
                    if (nextFactor == 0.0) throw ArithmeticException("Division by zero")
                    value /= nextFactor
                }
            }
            return value
        }

        // Factor = ('+' | '-')? (Number | '(' Expression ')')
        private fun parseFactor(): Double {
            if (pos >= text.length) throw IllegalArgumentException("Unexpected end of expression")

            var sign = 1.0
            if (text[pos] == '+') {
                pos++
            } else if (text[pos] == '-') {
                sign = -1.0
                pos++
            }

            if (pos >= text.length) throw IllegalArgumentException("Trailing operator without operand")

            val value = if (text[pos] == '(') {
                pos++
                val expr = parseExpression()
                if (pos >= text.length || text[pos] != ')') {
                    throw IllegalArgumentException("Missing closing parenthesis")
                }
                pos++
                expr
            } else {
                parseNumber()
            }

            return sign * value
        }

        private fun parseNumber(): Double {
            val start = pos
            var hasDot = false
            while (pos < text.length) {
                val c = text[pos]
                if (c.isDigit()) {
                    pos++
                } else if (c == '.' && !hasDot) {
                    hasDot = true
                    pos++
                } else {
                    break
                }
            }
            if (start == pos) throw IllegalArgumentException("Expected number at $pos")
            val numStr = text.substring(start, pos)
            return numStr.toDouble()
        }
    }
}

