package ru.qixi.binary.test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.qixi.binary.BitIds
import ru.qixi.binary.toByteString
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.fileSize

internal class BitIdsTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var bitIdsFile: Path
    private lateinit var bitIds: BitIds

    @BeforeEach
    fun setup() {
        bitIdsFile = tempDir.resolve("test_bits.bin")
        println(bitIdsFile)
        bitIds = BitIds(bitIdsFile)
    }

    @Test
    fun `readCount should return 0 for non-existing file`() {
        assertFalse(bitIdsFile.exists())
        assertEquals(0, bitIds.readCount())
    }

    @Test
    fun `updateBit should create file and set first bit`() {
        val updated = bitIds.update(0, true)
        println(bitIds.readByte(0).toByteString())
        assertTrue(updated)
        assertTrue(bitIdsFile.exists())

        assertEquals(true, bitIds.contains(0))
        assertEquals(1, bitIds.readCount())
        assertEquals(listOf(0), bitIds.readIds())
    }

    @Test
    fun `updateBit should set bits in different bytes`() {
        bitIds.update(0, true)
        bitIds.update(8, true)
        bitIds.update(15, true)
        bitIds.update(1000000, true)

        assertEquals(4, bitIds.readCount())
        assertEquals(listOf(0, 8, 15, 1000000), bitIds.readIds())
    }

    @Test
    fun `updateBit should unset existing bit`() {
        bitIds.update(5, true)
        assertEquals(1, bitIds.readCount())

        val updated = bitIds.update(5, false)

        assertTrue(updated)
        assertEquals(0, bitIds.readCount())
        assertTrue(bitIds.readIds().isEmpty())
    }

    @Test
    fun `readIds should return correct sequence for complex byte`() {
        bitIds.update(0, true)
        bitIds.update(2, true)
        bitIds.update(4, true)
        bitIds.update(6, true)
        bitIds.update(16, true)
        bitIds.update(32, true)

        val ids = bitIds.readIds()
        assertEquals(true, bitIds.contains(0))
        assertEquals(true, bitIds.contains(2))
        assertEquals(true, bitIds.contains(4))
        assertEquals(true, bitIds.contains(6))
        assertEquals(true, bitIds.contains(16))
        assertEquals(true, bitIds.contains(32))
        assertEquals(listOf(0, 2, 4, 6, 16, 32), ids)
        assertEquals(5, bitIds.path.fileSize())
    }

    @Test
    fun `findFirstZeroId should find gap in bits`() {
        bitIds.update(0, true)
        bitIds.update(1, true)
        // Пропускаем ID 2
        bitIds.update(3, true)

        assertEquals(2, bitIds.findFirstZeroId())
    }

    @Test
    fun `findFirstZeroId should return next bit after end of file`() {
        bitIds.update(0, true)
        assertEquals(1, bitIds.findFirstZeroId())

        for (i in 0..7) bitIds.update(i, true)

        assertEquals(8, bitIds.findFirstZeroId())
    }

    @Test
    fun `updateBit with negative id should return false`() {
        assertFalse(bitIds.update(-1, true))
    }
}