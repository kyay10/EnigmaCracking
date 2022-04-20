private const val SIZE_IN_BITS = Int.SIZE_BITS / 2

//TOOD: This could be sooooooo much neater when value classes are fully implemented in Kotlin
//Since we can change the state vars in the machine, and so instead of copy we can have copying functions here
@JvmInline
value class RotorState<R : Rotor<R>> private constructor(private val underlying: ULong) {
    constructor() : this(0U)

    val rotations: UInt
        get() = unpackRotations(underlying)

    val ringSetting: UInt
        get() = unpackRingSetting(unpackUInt(underlying))

    val position: UInt
        get() = unpackPosition(unpackUInt(underlying))
    val displayVal: Char
        get() = positionToChar(position.toInt(), ringSetting.toInt())

    fun copy(
        rotations: UInt = this.rotations,
        ringSetting: UInt = this.ringSetting,
        position: UInt = this.position
    ): RotorState<R> =
        RotorState(packRotationsWithUInt(packRingSettingAndPosition(ringSetting, position), rotations))

    fun copy(
        rotations: UInt = this.rotations,
        ringSetting: UInt = this.ringSetting,
        displayVal: Char = this.displayVal,
        unit: Unit = Unit
    ): RotorState<R> =
        RotorState(
            packRotationsWithUInt(
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

private fun packRotationsWithUInt(packed: UInt, rotations: UInt): ULong =
    (rotations.toULong() shl UInt.SIZE_BITS) or (packed.toULong() and 0xffffffffUL)

private fun unpackRotations(packed: ULong): UInt =
    (packed shr UInt.SIZE_BITS).toUInt()

private fun unpackUInt(packed: ULong): UInt =
    packed.toUInt()
