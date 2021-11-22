private const val SIZE_IN_BITS = Int.SIZE_BITS / 2

@JvmInline
value class RotorState<R : Rotor<R>> private constructor(/*private*/ val underlying: ULong) {
    constructor() : this(0U)

    val rotations: UInt
        get() = unpackRotations(underlying)

    val ringSetting: UInt
        get() = unpackRingSetting(unpackUInt(underlying))

    //set(value) = (value.toLong() shl 32) or (displayVal.toInt() and 0xffffffffL)
    val position: UInt
        get() = unpackPosition(unpackUInt(underlying))
    val displayVal: Char
        get() = positionToChar(position.toInt(), ringSetting.toInt())


    //set(value) = (ringSetting.toLong() shl 32) or (value.toInt() and 0xffffffffL)
    fun copy(
        rotations: UInt = this.rotations,
        ringSetting: UInt = this.ringSetting,
        position: UInt = this.position
    ): RotorState<R> =
        RotorState(packUIntWithRotations(packRingSettingAndPosition(ringSetting, position), rotations))

    fun copy(
        rotations: UInt = this.rotations,
        ringSetting: UInt = this.ringSetting,
        displayVal: Char = this.displayVal,
        unit: Unit = Unit
    ): RotorState<R> =
        RotorState(
            packUIntWithRotations(
                packRingSettingAndPosition(
                    ringSetting,
                    charToPosition(displayVal, ringSetting.toInt()).toUInt()
                ), rotations
            )
        )

}

private fun packRingSettingAndPosition(ringSetting: UInt, position: UInt): UInt =
    (ringSetting shl SIZE_IN_BITS) or (position and 0xffffU)

private fun unpackRingSetting(packed: UInt): UInt =
    packed shr SIZE_IN_BITS

private fun unpackPosition(packed: UInt): UInt =
    packed.toShort().toUInt()

private fun packUIntWithRotations(packed: UInt, rotations: UInt): ULong =
    (rotations.toULong() shl UInt.SIZE_BITS) or (packed.toULong() and 0xffffffffUL)

private fun unpackRotations(packed: ULong): UInt =
    (packed shr UInt.SIZE_BITS).toUInt()

private fun unpackUInt(packed: ULong): UInt =
    packed.toUInt()

/*
private const val SIZE_IN_BITS = Int.SIZE_BITS / 2

@JvmInline
value class RotorState<R : Rotor<R>> private constructor(private val underlying: Int) {
    constructor() : this(0)

    val ringSetting: Int
        get() = unpackRingSetting(underlying)

    //set(value) = (value.toLong() shl 32) or (displayVal.toInt() and 0xffffffffL)
    val displayVal: Char
        get() = unpackDisplayVal(underlying)

    val position: Int
        get() = charToPosition(displayVal, ringSetting)

    //set(value) = (ringSetting.toLong() shl 32) or (value.toInt() and 0xffffffffL)
    fun copy(ringSetting: Int = this.ringSetting, displayVal: Char = this.displayVal): RotorState<R> =
        RotorState(packRingSettingAndDisplayVal(ringSetting, displayVal))

    fun copy(ringSetting: Int = this.ringSetting, position: Int = this.position): RotorState<R> =
        RotorState(packRingSettingAndDisplayVal(ringSetting, positionToChar(position, ringSetting)))
}
private fun packRingSettingAndDisplayVal(ringSetting: Int, displayVal: Char): Int =
    (ringSetting shl SIZE_IN_BITS) or (displayVal.code and 0xffff)
private fun unpackRingSetting(packed: Int): Int =
    packed shr SIZE_IN_BITS
private fun unpackDisplayVal(packed: Int): Char =
    packed.toShort().toInt().toChar()
private fun unpackPosition(packed: Int): Int =
    packed.toShort().toInt()

private fun packIntWithRotations(packed: Int, rotations: Int): Long =
    (packed.toLong() shl Int.SIZE_BITS) or (rotations.toLong() and 0xffffffffL)
 */