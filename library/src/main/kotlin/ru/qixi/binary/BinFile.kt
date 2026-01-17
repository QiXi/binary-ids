package ru.qixi.binary

import java.io.RandomAccessFile
import java.nio.file.Path

open class BinFile(val path: Path) {

    fun readByte(pos: Long): Int {
        val file = RandomAccessFile(path.toFile(), "r")
        file.seek(pos)
        val byte = file.read()
        file.close()
        return byte
    }

    fun writeByte(pos: Long, value: Int) {
        val file = RandomAccessFile(path.toFile(), "rw")
        file.seek(pos)
        file.writeByte(value)
        file.close()
    }

    fun modifyByte(pos: Long, modify: (byte: Int) -> Int): Boolean {
        val file = RandomAccessFile(path.toFile(), "rw")
        file.seek(pos)
        val byte = file.read()
        return if (byte == -1) {
            file.writeByte(128) //1000|0000
            file.close()
            true
        } else {
            val newByte = modify(byte)
            val updated = newByte != byte
            if (updated) {
                file.seek(pos)
                file.writeByte(newByte)
            }
            file.close()
            updated
        }
    }

    fun readLastByte(): Int {
        val file = RandomAccessFile(path.toFile(), "r")
        file.seek(file.length() - 1)
        val byte = file.read()
        file.close()
        return byte
    }

    fun findFirstZeroBit(): Long {
        val file = RandomAccessFile(path.toFile(), "r")
        var byte = file.read()
        var index = 0L
        while (byte != -1) {
            if (byte != 255) {
                val posBit = findZeroBit(byte)
                if (posBit >= 0) {
                    index += posBit
                    break
                }
            }
            index += 8
            byte = file.read()
        }
        file.close()
        return index
    }

    private fun findZeroBit(byte: Int): Int {
        for (posBit in 0..7) {
            if ((0x80 shr posBit or byte) != byte) {
                return posBit
            }
        }
        return -1
    }

}