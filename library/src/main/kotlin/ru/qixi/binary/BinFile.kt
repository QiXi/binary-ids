package ru.qixi.binary

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*
import kotlin.io.path.exists
import kotlin.io.path.fileSize

internal const val DEFAULT_BUFFER_SIZE: Int = 16 * 1024

/**
 * Класс для низкоуровневой работы с бинарными файлами.
 * Использует FileChannel для обеспечения высокой производительности и потокобезопасности.
 *
 * @param path Полный путь к бинарному файлу.
 */
open class BinFile(val path: Path) {

    /**
     * Считывает один байт из файла по указанному смещению [position].
     *
     * @param position Абсолютное смещение в байтах от начала файла.
     * @return Целое число от 0 до 255 или -1, если позиция выходит за пределы файла.
     */
    fun readByte(position: Long): Int {
        if (position >= path.fileSize()) return -1
        FileChannel.open(path, READ).use { channel ->
            val buffer = ByteBuffer.allocate(1)
            return if (channel.read(buffer, position) > 0) {
                buffer.flip()
                buffer.get().toInt() and 0xFF
            } else -1
        }
    }

    /**
     * Извлекает значение последнего байта в файле.
     *
     * @return Значение байта (0-255) или -1, если файл пуст или не существует.
     */
    fun readLastByte(): Int {
        if (!path.exists()) return -1
        val size = path.fileSize()
        return if (size > 0) readByte(size - 1) else -1
    }

    /**
     * Записывает один байт [byte] в файл по указанному смещению [position].
     *
     * @param position Смещение, в которое будет произведена запись.
     * @param byte Значение для записи.
     */
    fun writeByte(position: Long, byte: Int) {
        FileChannel.open(path, WRITE, CREATE).use { channel ->
            val buffer = ByteBuffer.allocate(1)
            buffer.put(byte.toByte())
            buffer.flip()
            channel.write(buffer, position)
        }
    }

    /**
     * Выполняет атомарную модификацию байта по указанному смещению [position].
     *
     * @param position Позиция байта в файле.
     * @param modify Лямбда-функция, принимающая текущее значение байта и возвращающая новое.
     * @return
     *  - `true`, если данные в файле были изменены или создан новый байт.
     *  - `false`, если новое значение совпало со старым.
     */
    fun updateByte(position: Long, modify: (byte: Int) -> Int): Boolean {
        FileChannel.open(path, READ, WRITE, CREATE).use { channel ->
            val buffer = ByteBuffer.allocate(1)
            val readResult = channel.read(buffer, position)
            if (readResult == -1) {
                buffer.clear()
                buffer.put(128.toByte())
                buffer.flip()
                channel.write(buffer, position)
                return true
            }
            buffer.flip()
            val currentByte = buffer.get().toInt() and 0xFF
            val newByte = modify(currentByte)
            return if (newByte != currentByte) {
                buffer.clear()
                buffer.put(newByte.toByte())
                buffer.flip()
                channel.write(buffer, position)
                true
            } else {
                false
            }
        }
    }

    /**
     * Ищет первый бит со значением 0 (свободный ID) во всем файле.
     *
     * @return Абсолютный индекс (позиция) первого нулевого бита.
     */
    fun findFirstZeroBit(): Int {
        if (path.fileSize() == 0L) return 0
        FileChannel.open(path, READ).use { channel ->
            val buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE)
            var byteOffset = 0
            while (channel.read(buffer) != -1) {
                buffer.flip()
                while (buffer.remaining() >= 8) {
                    val value = buffer.long
                    if (value != -1L) { // Если в 64 битах есть хотя бы один ноль
                        val bitPos = value.inv().countLeadingZeroBits()
                        return (byteOffset * 8) + bitPos
                    }
                    byteOffset += 8
                }
                while (buffer.hasRemaining()) {
                    val byte = buffer.get().toInt() and 0xFF
                    if (byte != 0xFF) {
                        val bitPos = Integer.numberOfLeadingZeros(byte.inv() and 0xFF) - 24
                        return (byteOffset * 8) + bitPos
                    }
                    byteOffset++
                }
                buffer.clear()
            }
            return byteOffset * 8
        }
    }

}