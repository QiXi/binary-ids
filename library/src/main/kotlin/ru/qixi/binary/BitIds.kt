package ru.qixi.binary

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.WRITE
import kotlin.io.path.exists

/**
 * Класс [BitIds] предоставляет высокоуровневый интерфейс для работы с битовым массивом,
 * хранящимся в файле. Каждый бит в файле представляет наличие или отсутствие ID.
 * Наследуется от [BinFile] для доступа к базовым операциям ввода-вывода.
 *
 * @param path Путь к файлу, где хранится битовый массив.
 */
class BitIds(path: Path) : BinFile(path) {

    /**
     * Ищет первый свободный идентификатор (первый нулевой бит в файле).
     *
     * @return Индекс первого нулевого бита. Если файл пуст, возвращает 0.
     */
    fun findFirstZeroId(): Int {
        if (!path.exists()) return 0
        return findFirstZeroBit()
    }

    /**
     * Резервирует первый доступный идентификатор (ID).
     *
     * @return Индекс зарезервированного ID (позиция бита).
     */
    fun getId(): Int {
        FileChannel.open(path, READ, WRITE, CREATE).use { channel ->
            val bitPosition = findFirstZeroBit(channel)
            val bytePosition = (bitPosition / BITS_PER_BYTE).toLong()
            updateByte(channel, bytePosition) { updateBit(bitPosition, it, true) }
            return bitPosition
        }
    }

    /**
     * Подсчитывает общее количество занятых ID (установленных битов со значением 1).
     * Используется для получения общего числа существующих ID.
     * Оптимизировано через аппаратный подсчет бит (POPCNT).
     *
     * @return Количество установленных битов (занятых ID).
     * Возвращает 0, если файл не существует или пуст.
     */
    fun readCount(): Int {
        if (!path.exists()) return 0
        return FileChannel.open(path, READ).use { channel ->
            val buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE)
            var totalCount = 0
            while (channel.read(buffer) != -1) {
                buffer.flip()
                while (buffer.remaining() >= 8) {
                    totalCount += buffer.long.countOneBits()
                }
                while (buffer.hasRemaining()) {
                    totalCount += (buffer.get().toInt() and 0xFF).countOneBits()
                }
                buffer.clear()
            }
            totalCount
        }
    }

    /**
     * Проверяет, занят ли указанный ID (установлен ли соответствующий бит).
     *
     * @param [id] Изменяемый идентификатор.
     * @return `true`, если бит установлен (ID занят); `false` в иных случаях.
     */
    fun contains(id: Int): Boolean {
        if (!path.exists() || id < 0) return false
        val position = (id / BITS_PER_BYTE).toLong()
        val byte = readByte(position)
        if (byte == -1) return false
        return isSetBit(byte, id)
    }

    /**
     * Обновляет состояние бита для указанного [id].
     * @param state true для установки в 1 (set), false для сброса в 0 (unset).
     *
     * @return true, если операция привела к изменению данных в файле.
     */
    fun update(id: Int, state: Boolean): Boolean {
        if (id < 0) return false
        val position = (id / BITS_PER_BYTE).toLong()
        return if (path.exists()) {
            updateByte(position) { updateBit(id, it, state) }
        } else {
            writeByte(position, updateBit(id, 0, state))
            true
        }
    }

    /**
     * Внутренняя функция для вычисления нового значения байта после изменения бита.
     */
    private inline fun updateBit(id: Int, byte: Int, state: Boolean): Int {
        val mask = 0x80 shr (id % 8)
        return if (state) byte or mask else byte and mask.inv()
    }

    /**
     * Итерируется по всем занятым ID в файле и вызывает [action] для каждого.
     *
     * @param action Функция обработки найденного ID.
     */
    inline fun readIds(action: (Int) -> Unit) {
        if (!path.exists()) return
        FileChannel.open(path, READ).use { channel ->
            val buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE)
            var byteOffset = 0
            while (channel.read(buffer) != -1) {
                buffer.flip()
                while (buffer.remaining() >= 8) {
                    val value = buffer.long
                    if (value != 0L) { // Пропускаем пустые 64 бита мгновенно
                        processBits(value, byteOffset, action)
                    }
                    byteOffset += 8
                }
                while (buffer.hasRemaining()) {
                    val byte = buffer.get().toInt() and 0xFF
                    if (byte != 0) {
                        processBits(byte.toLong() shl 56, byteOffset, action)
                    }
                    byteOffset++
                }
                buffer.clear()
            }
        }
    }

    /**
     * Извлекает индексы установленных бит из 64-битного числа.
     *
     * @param bits 64-битное значение для анализа.
     * @param offset Текущее смещение байта в файле.
     * @param action Функция обработки найденного ID.
     */
    inline fun processBits(bits: Long, offset: Int, action: (Int) -> Unit) {
        var temp = bits
        while (temp != 0L) {
            // Находим количество нулей перед первой единицей слева (интринсик процессора)
            val leadingZeros = java.lang.Long.numberOfLeadingZeros(temp)
            action(offset * 8 + leadingZeros)
            // Сбрасываем найденный бит и продолжаем
            temp = temp and (Long.MIN_VALUE ushr leadingZeros).inv()
        }
    }
}