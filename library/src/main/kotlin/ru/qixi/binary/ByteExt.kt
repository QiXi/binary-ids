package ru.qixi.binary

fun Int.toByteString(): String {
    return (this and 0xFF).toString(2).padStart(8, '0')
}

fun ByteArray.toByteArrayString(): String {
    if (isEmpty()) return "[]"
    val sb = StringBuilder(size * 10)
    for (byte in this) {
        val b = byte.toInt() and 0xFF
        sb.append('[')
        for (i in 7 downTo 0) {
            sb.append(if ((b shr i and 1) == 1) '1' else '0')
        }
        sb.append(']')
    }
    return sb.toString()
}