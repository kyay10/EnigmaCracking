private const val KEYBOARD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

class EnigmaMachine<R1 : Rotor<R1>, R2 : Rotor<R2>, R3 : Rotor<R3>, R4 : Rotor<R4>>(
    val rotor1: R1,
    val rotor2: R2,
    val rotor3: R3,
    val rotor4: R4?,
    val reflector: Reflector,
    val plugboard: Plugboard
) {
    val rotorCount get() = rotor4?.let { 4 } ?: 3
    val lastRotorState get() = rotor4State ?: rotor3State
    val secondToLastRotorState get() = rotor4State?.let { rotor3State } ?: rotor2State
    val thirdToLastRotorState get() = rotor4State?.let { rotor2State } ?: rotor1State
    val fourthToLastRotorState get() = rotor4State?.let { rotor1State }
    var rotor1State = rotor1.blankState()
    var rotor2State = rotor2.blankState()
    var rotor3State = rotor3.blankState()
    var rotor4State = rotor4?.blankState()
    var display: String
        get() = "${thirdToLastRotorState.displayVal}${secondToLastRotorState.displayVal}${lastRotorState.displayVal}"
        set(displayVal) {
            if (displayVal.length != rotorCount) {
                throw EnigmaError("Incorrect length for display value")
            }
            rotor1State = rotor1.setDisplay(rotor1State, displayVal[0])
            rotor2State = rotor2.setDisplay(rotor2State, displayVal[1])
            rotor3State = rotor3.setDisplay(rotor3State, displayVal[2])
            rotor4State = rotor4State?.let { rotor4?.setDisplay(it, displayVal[3]) }
        }

    fun keyPress(key: Char): Char {
        if (key !in KEYBOARD_CHARS) {
            throw EnigmaError("Illegal key press $key")
        }
        stepRotors()
        val signalNum = charToPosition(key)
        val lampNum = electricSignal(signalNum)
        return KEYBOARD_CHARS[lampNum]
    }

    fun processText(text: String, replaceChar: Char = 'X'): String =
        buildString(text.length) {
            for (key in text) {
                val c = key.uppercaseChar().let {
                    if (charToPosition(it) !in 0..25)
                        replaceChar
                    else it
                }
                append(keyPress(c))
            }
        }

    val rotorCounts: Array<UInt>
        get() = arrayOf(
            rotor1State.rotations,
            rotor2State.rotations,
            rotor3State.rotations,
            rotor4State?.rotations ?: 0U
        )

    fun setRingSettings(ring1: UInt, ring2: UInt, ring3: UInt, ring4: UInt = 0U) {
        rotor1State = rotor1State.copy(ringSetting = ring1 - 1U)
        rotor2State = rotor2State.copy(ringSetting = ring2 - 1U)
        rotor3State = rotor3State.copy(ringSetting = ring3 - 1U)
        rotor4State = rotor4State?.copy(ringSetting = ring4 - 1U)
        display = "AAA" + (rotor4State?.let { "A" } ?: "")
    }

    private fun electricSignal(signalNum: Int): Int {
        var position = plugboard.signal(signalNum)
        val rotor4State = rotor4State
        if (rotor4 != null && rotor4State != null) {
            position = rotor4.signalIn(rotor4State, position)
        }
        position = rotor3.signalIn(rotor3State, position)
        position = rotor2.signalIn(rotor2State, position)
        position = rotor1.signalIn(rotor1State, position)

        position = reflector.signalIn(RotorState(), position)

        position = rotor1.signalOut(rotor1State, position)
        position = rotor2.signalOut(rotor2State, position)
        position = rotor3.signalOut(rotor3State, position)
        if (rotor4 != null && rotor4State != null) {
            position = rotor4.signalOut(rotor4State, position)
        }

        return plugboard.signal(position)
    }

    private fun stepRotors() {
        val rotor4State = rotor4State
        if (rotor4 != null && rotor4State != null) {
            val secondToLastRotorNotchOverPawl = rotor3.notchOverPawl(rotor3State)
            val rotateSecondToLast = rotor4.notchOverPawl(rotor4State) || secondToLastRotorNotchOverPawl
            this.rotor4State = rotor4.rotate(rotor4State)
            if (rotateSecondToLast)
                rotor3State = rotor3.rotate(rotor3State)
            if (secondToLastRotorNotchOverPawl)
                rotor2State = rotor2.rotate(rotor2State)
        } else {
            val secondToLastRotorNotchOverPawl = rotor2.notchOverPawl(rotor2State)
            val rotateSecondToLast = rotor3.notchOverPawl(rotor3State) || secondToLastRotorNotchOverPawl
            rotor3State = rotor3.rotate(rotor3State)
            if (rotateSecondToLast)
                rotor2State = rotor2.rotate(rotor2State)
            if (secondToLastRotorNotchOverPawl)
                rotor1State = rotor1.rotate(rotor1State)
        }
    }

}

class EnigmaError(message: String) : Exception(message)
