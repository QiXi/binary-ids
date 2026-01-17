package ru.qixi

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import ru.qixi.binary.BinFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteIfExists

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS) // Измеряем в микросекундах, так как операции с диском медленные
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
        bh.consume(binFile.readByte(fileSize / 2))
    }

    @Benchmark
    fun testWriteByte() {
        binFile.writeByte(fileSize / 2, 0xAB)
    }

    @Benchmark
    fun testUpdateByte() {
        binFile.updateByte(fileSize / 4) { it xor 0x01 }
    }

    @Benchmark
    fun testReadLastByte(bh: Blackhole) {
        bh.consume(binFile.readLastByte())
    }

    @Benchmark
    fun testFindFirstZeroBit(bh: Blackhole) {
        bh.consume(binFile.findFirstZeroBit())
    }
}