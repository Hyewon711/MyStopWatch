package com.seo.stopwatch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.seo.stopwatch.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var timeRun = 0L
    private var timeStarted = 0L // start 누를 때의 시간
    private var isWorking = false
    private var lapTime = 0L // start 누르면 항상 0부터 시작, pause 누르면 0으로 초기화
    private var timeRunInMillis = 0L
    private var formattedTime = ""
    private var currentText = "" // 기록할 현재 시간
    val TIMER_UPDATE_INTERVAL = 50L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        postInitialValues()
        /* 시작 버튼 */
        binding.btnStart.setOnClickListener {
            startTimer()
            binding.btnPause.visibility = View.VISIBLE
            binding.btnStart.visibility = View.INVISIBLE
        }
        /* 중지 버튼 */
        binding.btnPause.setOnClickListener {
            pauseService()
            binding.btnPause.visibility = View.INVISIBLE
            binding.btnStart.visibility = View.VISIBLE

            currentText = binding.tvRecord.text.toString()
            /** EditText setText, clear를 해주어야 이전 텍스트를 지운 후
             * 이전 텍스트 + 다음 텍스트를 같이 추가할 수 있다.
             * append가 아닌 setText로 하면 계속 변경되므로 append를 이용하여 텍스트를 추가해야 한다
             */
            binding.tvRecord.text.clear()
            binding.tvRecord.append("${currentText}\n $formattedTime")
        }
        /* 초기화 버튼 버튼 */
        binding.btnClear.setOnClickListener {
            postInitialValues()
        }
    }

    /* 초기화 메서드 */
    private fun postInitialValues() {
        timeRunInMillis = 0L
        isWorking = false
        lapTime = 0L
        timeRun = 0L
        timeStarted = 0L
        formattedTime = getFormattedStopWatchTime(timeRunInMillis)
        binding.tvTime.text = formattedTime
        binding.tvRecord.text.clear()
    }

    /* 중지 메서드 */
    private fun pauseService() {
        isWorking = false
    }

    /* 시작 메서드 */
    private fun startTimer() {
        isWorking = true
        timeStarted = System.currentTimeMillis()
        /* coroutine Dispatchers 이용 */
        CoroutineScope(Dispatchers.Main).launch {
            while (isWorking) {
                // 현재 시간과 start 버튼을 눌렀을 때의 시간의 차이 (start버튼을 누르고 나서 얼마만큼 지났는지)
                lapTime = System.currentTimeMillis() - timeStarted
                // 총 시간 = 지금까지 총 시간 + 'start'버튼 누르고 지난 시간
                timeRunInMillis = (timeRun + lapTime)
                delay(TIMER_UPDATE_INTERVAL)

                formattedTime = getFormattedStopWatchTime(timeRunInMillis)
                binding.tvTime.text = formattedTime
            }
            // 총 시간 갱신
            timeRun += lapTime
        }
    }

    private fun getFormattedStopWatchTime(ms: Long): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds) // '시' 단위
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) // '분' 단위
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) // '초' 단위

        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        // 분 단위
        milliseconds /= 10
        return "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds:" +
                "${if (milliseconds < 10) "0" else ""}$milliseconds"
    }
}
