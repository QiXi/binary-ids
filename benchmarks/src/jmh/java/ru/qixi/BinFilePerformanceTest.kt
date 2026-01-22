package ru.qixi

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import ru.qixi.binary.BinFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteIfExists
import kotlin.random.Random

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
open class BinFilePerformanceTest {

    private lateinit var tempFile: Path
    private lateinit var binFile: BinFile
    private val fileSize = 1024 * 1024L // 1 MB

    @Setup
    fun setup() {
        tempFile = Files.createTempFile("bench_bin", ".dat")
        // Предварительно заполняем файл 1 МБ данных (все биты 1, кроме последнего)
        val data = ByteArray(fileSize.toInt()) { 0xFF.toByte() }
        data[data.size - 1] = 0xFE.toByte() // 11111110
        Files.write(tempFile, data)
        binFile = BinFile(tempFile)
    }

    @TearDown
    fun tearDown() {
        tempFile.deleteIfExists()
    }

    @Benchmark
    fun testReadByte(bh: Blackhole) {
        val randomPosition = Random.nextLong(fileSize)
        bh.consume(binFile.readByte(randomPosition))
    }

    @Benchmark
    fun testReadLastByte(bh: Blackhole) {
        bh.consume(binFile.readLastByte())
    }

    @Benchmark
    fun testWriteByte() {
        val randomPosition = Random.nextLong(fileSize)
        binFile.writeByte(randomPosition, 0xAB)
    }

    @Benchmark
    fun testUpdateByte() {
        val randomPosition = Random.nextLong(fileSize)
        binFile.updateByte(randomPosition) { it xor 0x01 }
    }

    @Benchmark
    fun testFindFirstZeroBit(bh: Blackhole) {
        bh.consume(binFile.findFirstZeroBit())
    }
}