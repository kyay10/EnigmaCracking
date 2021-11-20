sealed interface Wiring {
    val wiring: CharArray
}

sealed class Rotor(override val wiring: CharArray, val stepping: Char = '\u0000', vararg val extraStepping: Char) :
    Wiring {
    constructor(wiring: String, stepping: Char = '\u0000', vararg extraStepping: Char) : this(
        wiring.toCharArray(),
        stepping,
        *extraStepping
    )

    companion object {
        val ROTORS = arrayOf(I, II, III, IV, V, VI, VII, VIII, Beta, Gamma)
        val NAME_TO_ROTOR = ROTORS.associateBy { it::class.simpleName ?: "" }

        fun byName(name: String): Rotor = NAME_TO_ROTOR[name] ?: throw RotorError("Unknown rotor type: $name")
    }

    val entryMap = IntArray(wiring.size) {
        wiring[it] - 'A'
    }

    val exitMap = IntArray(wiring.size).apply {
        entryMap.forEachIndexed { index, out ->
            this[out] = index

        }
    }
    fun charToPosition(char: Char, ringSetting: Int): Int {
        val n = char - 'A'
        return Math.floorMod(n - ringSetting, 26)
    }

    fun positionToChar(position: Int, ringSetting: Int): Char {
        val n = Math.floorMod(n + ringSetting, 26)
        return 'A' + n
    }
}

class RotorError(message: String) : Exception(message)

object I : Rotor("EKMFLGDQVZNTOWYHXUSPAIBRCJ", 'Q')
object II : Rotor("AJDKSIRUXBLHWTMCQGZNPYFVOE", 'E')
object III : Rotor("BDFHJLCPRTXVZNYEIWGAKMUSQO", 'V')
object IV : Rotor("ESOVPZJAYQUIRHXLNFTGKDCMWB", 'J')
object V : Rotor("VZBRGITYUPSDNHLXAWMJQOFECK", 'Z')
object VI : Rotor("JPGVOUMFYQBENHZRDKASXLICTW", 'Z', 'M')
object VII : Rotor("NZJHGRCXMYSWBOUFAIVLPEKQDT", 'Z', 'M')
object VIII : Rotor("FKQHTLXOCBJSPDZRAMEWNIUYGV", 'Z', 'M')
object Beta : Rotor("LEYJVCNIXWPBQMDRTAKZGFUHOS")
object Gamma : Rotor("FSOKANUERHMBTIYCWLQPZXVGJD")

class CustomRotor(wiring: CharArray, stepping: Char = '\u0000', vararg extraStepping: Char) :
    Rotor(CharArray(wiring.size) { wiring[it].uppercaseChar() }, stepping.uppercaseChar(), *extraStepping) {
    constructor(wiring: String, stepping: Char = '\u0000', vararg extraStepping: Char) : this(
        wiring.toCharArray(),
        stepping,
        *extraStepping
    )

    init {
        when {
            wiring.size != 26 -> {
                throw RotorError("invalid wiring length")
            }
            wiring.toSet().size != 26 -> {
                throw RotorError("invalid wiring frequency")
            }
            wiring.any { !it.isLetter() } -> {
                throw RotorError("invalid wiring: ${wiring.joinToString(separator = "")}")
            }
        }
    }

}


sealed class Reflector(override val wiring: CharArray) : Wiring {
    constructor(wiring: String) : this(wiring.toCharArray())

    companion object {
        val REFLECTORS = arrayOf(B, C, B_Thin, C_Thin)
        val NAME_TO_REFLECTOR = REFLECTORS.associateBy { it::class.simpleName?.replace('_', '-') ?: "" }

        fun byName(name: String): Reflector = NAME_TO_REFLECTOR[name]
            ?: throw RotorError("Unknown reflector type: $name")
    }
}

object B : Reflector("YRUHQSLDPXNGOKMIEBFZCWVJAT")
object C : Reflector("FVPJIAOYEDRZXWGCTKUQSBNMHL")
object B_Thin : Reflector("ENKQAUYWJICOPBLMDXZVFTHRGS")
object C_Thin : Reflector("RDOBJNTKVEHMLFCWZAXGYIPSUQ")

class CustomReflector(wiring: CharArray) : Reflector(wiring) {
    constructor(wiring: String) : this(wiring.toCharArray())
}
