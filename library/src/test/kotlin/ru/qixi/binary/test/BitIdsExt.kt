package ru.qixi.binary.test

import ru.qixi.binary.BitIds

fun BitIds.readIds(): List<Int> {
    val list = mutableListOf<Int>()
    readIds { list.add(it) }
    return list
}