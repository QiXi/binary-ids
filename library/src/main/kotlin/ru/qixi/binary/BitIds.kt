package ru.qixi.binary

import java.io.RandomAccessFile
import java.nio.file.Path
import kotlin.io.path.exists

class BitIds(path: Path) : BinFile(path) {

    fun readCount(): Int {
        if (!path.exists()) return 0
        val file = RandomAccessFile(path.toFile(), "r")
        var byte = file.read()
        var count = 0
        while (byte != -1) {
            if (byte != 0) {
                count += getTrueBitCount(byte)
            }
            byte = file.read()
        }
        file.close()
        return count
    }

    private fun getTrueBitCount(flag: Int): Int {
        var count = 0
        for (pos in 0..7) {
            val bit = (flag shr pos and 1)
            if (bit == 1) {
                count++
            }
        }
        return count
    }

    fun updateBit(id: Int, flag: Boolean): Boolean {
        if (id < 0) return false
        val posByte = (id / 8).toLong()
        //println("updateBit id:$id pos:$posByte [$flag] $path")
        return if (path.exists()) {
            val updated = modifyByte(posByte) { updateByte(id, it, flag) }
            if (updated) println("modifyByte pos:$posByte $path")
            updated
        } else {
            writeByte(posByte, updateByte(id, 0, flag))
            flag
        }
    }

    private fun updateByte(id: Int, byte: Int, flag: Boolean): Int {
        return if (flag) setBit(byte, id) else unsetBit(byte, id)
    }

    fun readIds(): List<Int> {
        if (!path.exists()) {
            return listOf()
        }
        val file = RandomAccessFile(path.toFile(), "r")
        var byte = file.read()
        val list = mutableListOf<Int>()
        var index = 0
        while (byte != -1) {
            if (byte != 0) {
                for (posBit in 0..7) {
                    if ((0x80 shr posBit or byte) == byte) {
                        list.add(index)
                    }
                    index++
                }
            } else {
                index += 8
            }
            byte = file.read()
        }
        file.close()
        return list
    }

    fun findFirstZeroId(): Long {
        if (!path.exists()) return 0L
        return findFirstZeroBit()
    }

}