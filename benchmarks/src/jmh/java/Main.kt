import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import ru.qixi.BinFilePerformanceTest

fun main() {
    val opt = OptionsBuilder()
        .include(BinFilePerformanceTest::class.java.simpleName)
        .forks(1)
        .build()

    Runner(opt).run()
}
