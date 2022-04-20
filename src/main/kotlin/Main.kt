import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun main() = coroutineScope {
    val cribText = "IDDSQRFXKJ"
    val cipherTexts = arrayOf(
        "ALFREDXAHO",
        "DAVIDXBLEI",
        "TRACYXCAMP",
        "PETERXCHEN",
        "TRACYXCHOU",
        "KITXCOSPER",
        "VINODXDHAM",
        "KENXFORBUS",
        "BILLXGATES",
        "KURTXGODEL",
        "ASHOKXGOEL",
        "GENEXGOLUB",
        "BILLXGROPP",
        "WENDYXHALL",
        "JIAWEIXHAN",
        "LESXHATTON",
        "DAVIDXKORN",
        "YANNXLECUN",
        "JOHNXLIONS",
        "JOELXMOSES",
        "MAXXNEWMAN",
        "LARRYXPAGE",
        "JUANXPAVON",
        "KATHYXPHAM",
        "JONXPOSTEL",
        "RONXRIVEST",
        "DANAXSCOTT",
        "RAVIXSETHI",
        "CLIFFXSHAW",
        "EVAXTARDOS",
        "JOHNXTUKEY",
        "EIITIXWADA",
        "STEVEXWARD",
        "ARIFXZAMAN"
    )
    val rotors = arrayOf(
        arrayOf(I, II, III), arrayOf(I, II, IV), arrayOf(I, II, V), arrayOf(I, III, II),
        arrayOf(I, III, IV), arrayOf(I, III, V), arrayOf(I, IV, II), arrayOf(I, IV, III),
        arrayOf(I, IV, V), arrayOf(I, V, II), arrayOf(I, V, III), arrayOf(I, V, IV),
        arrayOf(II, I, III), arrayOf(II, I, IV), arrayOf(II, I, V), arrayOf(II, III, I),
        arrayOf(II, III, IV), arrayOf(II, III, V), arrayOf(II, IV, I), arrayOf(II, IV, III),
        arrayOf(II, IV, V), arrayOf(II, V, I), arrayOf(II, V, III), arrayOf(II, V, IV),
        arrayOf(III, I, II), arrayOf(III, I, IV), arrayOf(III, I, V), arrayOf(III, II, I),
        arrayOf(III, II, IV), arrayOf(III, II, V), arrayOf(III, IV, I), arrayOf(III, IV, II),
        arrayOf(III, IV, V), arrayOf(IV, I, II), arrayOf(IV, I, III), arrayOf(IV, I, V),
        arrayOf(IV, II, I), arrayOf(IV, II, III), arrayOf(IV, I, V), arrayOf(IV, II, I),
        arrayOf(IV, II, III), arrayOf(IV, II, V), arrayOf(IV, III, I), arrayOf(IV, III, II),
        arrayOf(IV, III, V), arrayOf(IV, V, I), arrayOf(IV, V, II), arrayOf(IV, V, III),
        arrayOf(V, I, II), arrayOf(V, I, III), arrayOf(V, I, IV), arrayOf(V, II, I),
        arrayOf(V, II, III), arrayOf(V, II, IV), arrayOf(V, III, I), arrayOf(V, III, II),
        arrayOf(V, III, IV), arrayOf(V, IV, I), arrayOf(V, IV, II), arrayOf(V, IV, III)
    )
    val notKey = "OQB"
    val reflector = B
    val allDisplays1 = allDisplaysExcept(notKey[0])
    val allDisplays2 = allDisplaysExcept(notKey[1])
    val allDisplays3 = allDisplaysExcept(notKey[2])
    val capitalAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
/*    val capitalAlphabet1stRotor = capitalAlphabet.replace(notKey[0].toString(), "")
    val capitalAlphabet2ndRotor = capitalAlphabet.replace(notKey[1].toString(), "")
    val capitalAlphabet3rdRotor = capitalAlphabet.replace(notKey[2].toString(), "")*/
    withContext(Dispatchers.Default) {
        for (rotorList in rotors) {
            launch {
                val machine = EnigmaMachine(
                    rotorList,
                    reflector,
                    plugboard = Plugboard.fromKeySheet("")
                )
                for (ring1 in 1U..26U) {
                    for (ring2 in 1U..26U) {
                        // The last one always rotates, the second sometimes rotates, the first barely does
                        machine.setRingSettings(1U, ring2, ring1)
                        for (display1 in allDisplays1) {
                            for (display2 in allDisplays2) {
                                for (display3 in allDisplays3) {
                                    for (cipherText in cipherTexts) {
                                        machine.setDisplay(display1, display2, display3)
                                        val rotor1StateBefore = machine.rotor1State
                                        val rotor2StateBefore = machine.rotor2State
                                        val rotor3StateBefore = machine.rotor3State
                                        val text = machine.processText(cipherText)
                                        val fit = fit(text, cribText)
                                        if (fit >= 7) {
                                            println(rotorList.joinToString(", "))
                                            println("$cipherText matches $text with $fit/10 matches")
                                            println("[$ring1, $ring2, $display1, $display2, $display3]")
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}

fun allDisplaysExcept(character: Char) = CharArray(25) {
    val current = positionToChar(it)
    current + if (current >= character) 1 else 0
}

fun fit(actual: String, expected: String): Int {
    var numCorrect = 0
    actual.forEachIndexed { index, letter ->
        if (letter == expected[index])
            numCorrect++
    }
    return numCorrect
}