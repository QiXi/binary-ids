package ru.qixi.binary

fun setBit(value: Int, id: Int): Int {
    val posBit = id % 8
    return 0x80 shr posBit or value
}

fun unsetBit(value: Int, id: Int): Int {
    val posBit = id % 8
    return 0xFF7F shr posBit and value
}

fun isSetBit(value: Int, id: Int): Boolean {
    val posBit = id % 8
    return 0x80 shr posBit or value == value
}

fun getBit(value: Int, id: Int): Int {
    val posBit = id % 8
    return 0x80 shr posBit and value
}
