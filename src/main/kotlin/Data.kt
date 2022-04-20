sealed interface Wiring {
    val wiring: CharArray
}

const val DEFAULT_WIRING_LENGTH = 26

sealed class Rotor<Self : Rotor<Self>>(
    override val wiring: CharArray,
    val stepping: Char = '\u0000',
    vararg val extraStepping: Char
) :
    Wiring {
    constructor(wiring: String, stepping: Char = '\u0000', vararg extraStepping: Char) : this(
        wiring.toCharArray(),
        stepping,
        *extraStepping
    )

    companion object {
        val ROTORS by lazy { arrayOf(I, II, III, IV, V, VI, VII, VIII, Beta, Gamma) }
        val NAME_TO_ROTOR by lazy { ROTORS.associateBy { it::class.simpleName ?: "" } }
        val ROTOR_TO_NAME by lazy { NAME_TO_ROTOR.entries.associate { (k, v) -> v to k } }

        fun byName(name: String): Rotor<*> = NAME_TO_ROTOR[name] ?: throw RotorError("Unknown rotor type: $name")

    }

    val entryMap = IntArray(wiring.size) {
        wiring[it] - 'A'
    }

    val exitMap = IntArray(wiring.size).apply {
        entryMap.forEachIndexed { index, out ->
            this[out] = index
        }
    }

    fun blankState() = with(RotorState<Self>()) { setDisplay('A') }

    context(RotorState<Self>)
    fun setDisplay(value: Char): RotorState<Self> {
        return copy(rotations = 0U, displayVal = value)
    }

    context(RotorState<Self>)
    fun setPosition(value: UInt): RotorState<Self> {
        return copy(rotations = 0U, position = value)
    }

    context(RotorState<Self>)
    fun signalIn(signal: Int): Int {
        val pin = Math.floorMod(signal + position.toInt(), wiring.size)
        val contact = entryMap[pin]
        return Math.floorMod(contact - position.toInt(), wiring.size)
    }

    context(RotorState<Self>)
    fun signalOut(signal: Int): Int {
        val contact = Math.floorMod(signal + position.toInt(), wiring.size)
        val pin = exitMap[contact]
        return Math.floorMod(pin - position.toInt(), wiring.size)
    }

    context(RotorState<Self>)
    val isNotchOverPawl
        get() = displayVal == stepping || displayVal in extraStepping

    context(RotorState<Self>)
    fun rotate(): RotorState<Self> {
        return copy(position = Math.floorMod(position.toInt() + 1, wiring.size).toUInt(), rotations = rotations + 1U)
    }

    override fun toString(): String {
        return ROTOR_TO_NAME[this] ?: super.toString()
    }
}

// Display map
fun charToPosition(displayVal: Char, ringSetting: Int = 0, wiringLength: Int = DEFAULT_WIRING_LENGTH): Int {
    val n = displayVal - 'A'
    return Math.floorMod(n - ringSetting, wiringLength)
}

// Pos map
fun positionToChar(position: Int, ringSetting: Int = 0, wiringLength: Int = DEFAULT_WIRING_LENGTH): Char {
    val n = Math.floorMod(position + ringSetting, wiringLength)
    return 'A' + n
}

class RotorError(message: String) : Exception(message)

object I : Rotor<I>("EKMFLGDQVZNTOWYHXUSPAIBRCJ", 'Q')
object II : Rotor<II>("AJDKSIRUXBLHWTMCQGZNPYFVOE", 'E')
object III : Rotor<III>("BDFHJLCPRTXVZNYEIWGAKMUSQO", 'V')
object IV : Rotor<IV>("ESOVPZJAYQUIRHXLNFTGKDCMWB", 'J')
object V : Rotor<V>("VZBRGITYUPSDNHLXAWMJQOFECK", 'Z')
object VI : Rotor<VI>("JPGVOUMFYQBENHZRDKASXLICTW", 'Z', 'M')
object VII : Rotor<VII>("NZJHGRCXMYSWBOUFAIVLPEKQDT", 'Z', 'M')
object VIII : Rotor<VIII>("FKQHTLXOCBJSPDZRAMEWNIUYGV", 'Z', 'M')
object Beta : Rotor<Beta>("LEYJVCNIXWPBQMDRTAKZGFUHOS")
object Gamma : Rotor<Gamma>("FSOKANUERHMBTIYCWLQPZXVGJD")

open class CustomRotor(wiring: CharArray, stepping: Char = '\u0000', vararg extraStepping: Char) :
    Rotor<CustomRotor>(
        CharArray(wiring.size) { wiring[it].uppercaseChar() },
        stepping.uppercaseChar(),
        *extraStepping
    ) {
    constructor(wiring: String, stepping: Char = '\u0000', vararg extraStepping: Char) : this(
        wiring.toCharArray(),
        stepping,
        *extraStepping
    )

    init {
        when {
            wiring.size != DEFAULT_WIRING_LENGTH -> {
                throw RotorError("invalid wiring length")
            }
            wiring.toSet().size != DEFAULT_WIRING_LENGTH -> {
                throw RotorError("invalid wiring frequency")
            }
            wiring.any { !it.isLetter() } -> {
                throw RotorError("invalid wiring: ${wiring.joinToString(separator = "")}")
            }
        }
    }

}


sealed class Reflector(wiring: CharArray) : CustomRotor(wiring) {
    constructor(wiring: String) : this(wiring.toCharArray())

    companion object {
        val REFLECTORS by lazy { arrayOf(B, C, B_Thin, C_Thin) }
        val NAME_TO_REFLECTOR by lazy { REFLECTORS.associateBy { it::class.simpleName?.replace('_', '-') ?: "" } }

        fun byName(name: String): Reflector = NAME_TO_REFLECTOR[name]
            ?: throw RotorError("Unknown reflector type: $name")
    }

    fun signalIn(signal: Int, unit: Unit = Unit): Int {
        val pin = Math.floorMod(signal, wiring.size)
        val contact = entryMap[pin]
        return Math.floorMod(contact, wiring.size)
    }

    fun signalOut(signal: Int, unit: Unit = Unit): Int {
        val contact = Math.floorMod(signal, wiring.size)
        val pin = exitMap[contact]
        return Math.floorMod(pin, wiring.size)
    }
}

object B : Reflector("YRUHQSLDPXNGOKMIEBFZCWVJAT")
object C : Reflector("FVPJIAOYEDRZXWGCTKUQSBNMHL")
object B_Thin : Reflector("ENKQAUYWJICOPBLMDXZVFTHRGS")
object C_Thin : Reflector("RDOBJNTKVEHMLFCWZAXGYIPSUQ")

open class CustomReflector(wiring: CharArray) : Reflector(wiring) {
    constructor(wiring: String) : this(wiring.toCharArray())
}


class PlugboardError(message: String) : Exception(message)

class Plugboard(override var wiring: CharArray) : Wiring {
    constructor(wiring: String) : this(wiring.toCharArray())

    init {
        /*if(wiring.size > 20) {
            throw PlugboardError("Please specify 10 or less maximum pairs")
        }*/
    }

    companion object {
        fun fromKeySheet(sheetStr: String): Plugboard {
            val charArray = CharArray(DEFAULT_WIRING_LENGTH) { positionToChar(it) }
            for (pairing in sheetStr.split(" ")) {
                if (pairing.isEmpty())
                    continue
                if (pairing.length != 2)
                    throw PlugboardError("Invalid pair: $pairing")
                charArray[charToPosition(pairing[0])] = pairing[1]
                charArray[charToPosition(pairing[1])] = pairing[0]
            }
            return Plugboard(charArray)
        }
    }

    fun signal(position: Int) = charToPosition(wiring[position])
}

