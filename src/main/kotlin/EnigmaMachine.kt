@file:Suppress("UNCHECKED_CAST")

private const val KEYBOARD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

class EnigmaMachine<R1 : Rotor<R1>, R2 : Rotor<R2>, R3 : Rotor<R3>, R4 : Rotor<R4>, L1 : Rotor<L1>, L2 : Rotor<L2>, L3 : Rotor<L3>>(
    val rotor1: R1, val rotor2: R2, val rotor3: R3, val rotor4: R4?, val reflector: Reflector, val plugboard: Plugboard
) {

    constructor(rotorList: Array<Rotor<*>>, reflector: Reflector, plugboard: Plugboard) : this(
        rotorList[0] as R1, rotorList[1] as R2, rotorList[2] as R3, rotorList.getOrNull(3) as R4?, reflector, plugboard
    )

    val rotorCount get() = rotor4?.let { 4 } ?: 3
    var lastRotorState
        get() = (rotor4State ?: rotor3State) as RotorState<L1>
        set(value) {
            if (rotor4State != null) {
                rotor4State = value as RotorState<R4>
            } else {
                rotor3State = value as RotorState<R3>
            }
        }
    var secondToLastRotorState
        get() = (rotor4State?.let { rotor3State } ?: rotor2State) as RotorState<L2>
        set(value) {
            if (rotor4State != null) {
                rotor3State = value as RotorState<R3>
            } else {
                rotor2State = value as RotorState<R2>
            }
        }
    var thirdToLastRotorState
        get() = (rotor4State?.let { rotor2State } ?: rotor1State) as RotorState<L3>
        set(value) {
            if (rotor4State != null) {
                rotor2State = value as RotorState<R2>
            } else {
                rotor1State = value as RotorState<R1>
            }
        }
    val lastRotor get() = (rotor4 ?: rotor3) as L1
    val secondToLastRotor get() = (rotor4?.let { rotor3 } ?: rotor2) as L2
    val thirdToLastRotor get() = (rotor4?.let { rotor2 } ?: rotor1) as L3
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
            setDisplay(displayVal[0], displayVal[1], displayVal[2], displayVal.getOrElse(3) { '\u0000' })
        }

    fun setDisplay(display1: Char, display2: Char, display3: Char, display4: Char = '\u0000') =
        withContexts(rotor1State, rotor2State, rotor3State) {
            rotor1State = rotor1.setDisplay(display1)
            rotor2State = rotor2.setDisplay(display2)
            rotor3State = rotor3.setDisplay(display3)
            rotor4State = rotor4State?.run { rotor4?.setDisplay(display4) }
        }

    fun resetDisplay() {
        setDisplay('A', 'A', 'A', 'A')
    }

    fun setPositions(position1: UInt, position2: UInt, position3: UInt, position4: UInt = 0U) =
        withContexts(rotor1State, rotor2State, rotor3State) {
            rotor1State = rotor1.setPosition(position1)
            rotor2State = rotor2.setPosition(position2)
            rotor3State = rotor3.setPosition(position3)
            rotor4State = rotor4State?.run { rotor4?.setPosition(position4) }
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

    fun setRingSettings(ring1: UInt, ring2: UInt, ring3: UInt, ring4: UInt = 1U) {
        rotor1State = rotor1State.copy(ringSetting = ring1 - 1U)
        rotor2State = rotor2State.copy(ringSetting = ring2 - 1U)
        rotor3State = rotor3State.copy(ringSetting = ring3 - 1U)
        rotor4State = rotor4State?.copy(ringSetting = ring4 - 1U)
        resetDisplay()
    }

    private fun electricSignal(signalNum: Int): Int = withContexts(rotor1State, rotor2State, rotor3State) {
        var position = plugboard.signal(signalNum)
        val rotor4State = rotor4State
        if (rotor4 != null && rotor4State != null) with(rotor4State) {
            position = rotor4.signalIn(position)
        }
        position = rotor3.signalIn(position)
        position = rotor2.signalIn(position)
        position = rotor1.signalIn(position)

        position = reflector.signalIn(position)

        position = rotor1.signalOut(position)
        position = rotor2.signalOut(position)
        position = rotor3.signalOut(position)
        if (rotor4 != null && rotor4State != null) with(rotor4State) {
            position = rotor4.signalOut(position)
        }

        return plugboard.signal(position)
    }

    private fun stepRotors() = withContexts(lastRotorState, secondToLastRotorState, thirdToLastRotorState) {
        val secondToLastRotorNotchOverPawl = secondToLastRotor.isNotchOverPawl
        val rotateSecondToLast = lastRotor.isNotchOverPawl || secondToLastRotorNotchOverPawl
        lastRotorState = lastRotor.rotate()
        if (rotateSecondToLast) secondToLastRotorState = secondToLastRotor.rotate()
        if (secondToLastRotorNotchOverPawl) thirdToLastRotorState = thirdToLastRotor.rotate()
    }
/*if (rotor4 != null && rotor4State != null) withContexts(
        rotor1State, rotor2State, rotor3State, rotor4State!!
    ) {
        val secondToLastRotorNotchOverPawl = rotor3.isNotchOverPawl
        val rotateSecondToLast = rotor4.isNotchOverPawl || secondToLastRotorNotchOverPawl
        rotor4State = rotor4.rotate()
        if (rotateSecondToLast)
            rotor3State = rotor3.rotate()
        if (secondToLastRotorNotchOverPawl)
            rotor2State = rotor2.rotate()
    }
    else withContexts(rotor1State, rotor2State, rotor3State) {
        val secondToLastRotorNotchOverPawl = rotor2.isNotchOverPawl
        val rotateSecondToLast = rotor3.isNotchOverPawl || secondToLastRotorNotchOverPawl
        rotor3State = rotor3.rotate()
        if (rotateSecondToLast)
            rotor2State = rotor2.rotate()
        if (secondToLastRotorNotchOverPawl)
            rotor1State = rotor1.rotate()
    }*/
}

class EnigmaError(message: String) : Exception(message)

