package ru.qixi

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import ru.qixi.binary.BitIds
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class BitIdsPerformanceTest {

    private lateinit var tempPath: Path
    private lateinit var bitIds: BitIds
    private val fileSize = 1024 * 1024L // 1 MB

    @Setup
    fun setup() {
        tempPath = Files.createTempFile("perf_test", ".bin")
        bitIds = BitIds(tempPath)
        // Предварительно заполняем файл данными (1 МБ)
        val data = ByteArray(fileSize.toInt()) { 0xFF.toByte() }
        // Оставляем один нулевой бит в самом конце
        data[data.size - 1] = 0xFE.toByte()
        Files.write(tempPath, data)
    }

    @TearDown
    fun tearDown() {
        Files.deleteIfExists(tempPath)
    }

    /**
     * Тест скорости подсчета установленных битов во всем файле.
     */
    @Benchmark
    fun benchmarkReadCount() {
        bitIds.readCount()
    }

    /**
     * Тест поиска первого свободного ID (нулевого бита).
     */
    @Benchmark
    fun benchmarkFindFirstZeroId() {
        bitIds.findFirstZeroId()
    }

    /**
     * Тест последовательного чтения всех ID.
     */
    @Benchmark
    fun benchmarkGetId(): Int {
        return bitIds.getId()
    }

    @Benchmark
    fun benchmarkReadIds(bh: Blackhole) {
        bitIds.readIds { id ->
            bh.consume(id)
        }
    }

    /**
     * Тест записи бита в случайное место (Random Access Write).
     */
    @Benchmark
    fun benchmarkRandomUpdateBit() {
        val randomId = Random.nextLong(fileSize)
        bitIds.update(randomId.toInt(), true)
    }
}