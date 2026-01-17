package ru.qixi.binary

fun setBit(flag: Int, id: Int): Int {
    val posBit = id % 8
    return 0x80 shr posBit or flag
}

fun unsetBit(flag: Int, id: Int): Int {
    val posBit = id % 8
    return 0xFF7F shr posBit and flag
}

fun isSetBit(flag: Int, id: Int): Boolean {
    val posBit = id % 8
    return 0x80 shr posBit or flag == flag
}

fun getBit(flag: Int, id: Int): Int {
    val posBit = id % 8
    return 0x80 shr posBit and flag
}

fun convertByteToString(byte: Int): String {
    val binaryString = StringBuilder(8)
    for (pos in 0..7) {
        val bit = (byte shr pos and 1)
        binaryString.append(bit)
    }
    return binaryString.reverse().toString()
}

fun convertByteArrayToString(array: ByteArray): String {
    if (array.isEmpty()) {
        return "[]"
    }
    val binaryString = StringBuilder(128)
    array.forEach {
        val flag = it.toInt()
        binaryString.append('[')
        binaryString.append(convertByteToString(flag))
        binaryString.append(']')
    }
    return binaryString.toString()
}