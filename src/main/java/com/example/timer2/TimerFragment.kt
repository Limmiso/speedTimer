package com.example.timer2

import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.media.MediaPlayer
import android.widget.ImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class TimerFragment : Fragment(R.layout.fragment_timer) {
    //소리
    private var mediaPlayer: MediaPlayer? = null //알
    private lateinit var music: ImageView //음악

    private lateinit var viewModel: TimerViewModel
    private lateinit var btnClear: Button //초기화
    private lateinit var btnStart: Button //시작
    private lateinit var btnStopTimer: Button //중지
    private lateinit var btnResumeTimer: Button //재시작
    private lateinit var btnrecord: Button //초기화
    private lateinit var etHour: EditText
    private lateinit var etMinute: EditText
    private lateinit var etSecond: EditText
    private lateinit var tvCountdown: TextView //가는 시간
    private lateinit var ckTime: TextView //설정한 시간
    private lateinit var dot: TextView
    private lateinit var dotBu: TextView
    private lateinit var numberPicker: NumberPicker //빠르기 설정 id
    private var elapsedRealTime: Long = 0L // 실제 경과 시간
    //초기화
    private var countDownTimer: CountDownTimer? = null
    private var remainingMillis: Long = 0L
    private var isTimerRunning = false
    private var selectedValue=1.toDouble()
    //실제시간
    private var click: Long =0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(TimerViewModel::class.java)
//함수지정
        numberPicker = view.findViewById(R.id.numberPicker)
        btnClear = view.findViewById(R.id.btn_clear)
        btnStart = view.findViewById(R.id.btn_start)
        btnStopTimer = view.findViewById(R.id.btn_stop_timer)
        btnResumeTimer = view.findViewById(R.id.btn_resume_timer)
        btnrecord = view.findViewById(R.id.btn_record)
        etHour = view.findViewById(R.id.et_hour)
        etMinute = view.findViewById(R.id.et_minute)
        etSecond = view.findViewById(R.id.et_second)
        tvCountdown = view.findViewById(R.id.tv_countdown) //
        ckTime = view.findViewById(R.id.ck_time)  // ck_time 초기화
        dot= view.findViewById(R.id.dot)
        dotBu= view.findViewById(R.id.d_ot)
        //분, 초는 59까지 입력가능
        etMinute.filters = arrayOf(InputFilterMinMax(0, 59))
        etSecond.filters = arrayOf(InputFilterMinMax(0, 59))
//numberpicker최대 최소
        numberPicker.value=0 //초기 값
        val minValue = 0.01 //최소
        val maxValue = 10.00  //최대
        val step = 0.01 //차이
        val values = generateDecimalValues(minValue, maxValue, step) //최소, 최대, 차이에 따라 모든 값을 배열에 저장
        numberPicker.minValue = 0 //배열의 첫번째 값
        numberPicker.maxValue = values.size - 1 //선택할 수 있는 마지막 값
        numberPicker.displayedValues = values //표시될 실제 값

// 1.0에 해당하는 인덱스 계산
        val defaultIndex = ((1.0 - minValue) / step).toInt()

// NumberPicker의 초기값을 1.0으로 설정
        numberPicker.value = defaultIndex
        // 타이머 시작, 취소, 중지, 재시작 버튼 클릭 리스너 설정
        btnStart.setOnClickListener { startTimer() }
        btnClear.setOnClickListener { cancelTimer() }
        btnStopTimer.setOnClickListener { stopTimer() }
        btnResumeTimer.setOnClickListener { resumeTimer() }
        btnrecord.setOnClickListener { record_clear() }
        numberPicker.setOnValueChangedListener { _, _, newVal ->  //numberpicker로 선택한 수
            selectedValue = values[newVal].toDouble() // NumberPicker 선택 값 저장
        }
        tvCountdown.visibility = View.GONE
        btnClear.visibility = Button.GONE
        btnStopTimer.visibility = Button.GONE
        btnrecord.visibility = Button.GONE

        music = view.findViewById(R.id.music)  // 음악을 재생할 버튼

        // 음악 버튼 클릭 리스너 설정
        music.setOnClickListener {
            toggleMusic()  // 이미지 클릭 시 음악을 시작/멈춤
        }

        return view
    }
    //numberPicker
    private fun generateDecimalValues(min: Double, max: Double, step: Double): Array<String> {
        val values = mutableListOf<String>()
        var currentValue = min
        while (currentValue <= max) {
            values.add(String.format("%.2f", currentValue))
            currentValue += step
        }
        return values.toTypedArray()
    }
    //입력된 숫자가 설정해둔 범위 내의 값인지 확인
    class InputFilterMinMax(private val min: Int, private val max: Int) : InputFilter {
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            try {
                val input = (dest.subSequence(0, dstart).toString() + source + dest.subSequence(
                    dend,
                    dest.length
                )).toInt()
                if (isInRange(min, max, input)) return null
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }

        private fun isInRange(min: Int, max: Int, value: Int): Boolean {
            return value in min..max
        }
    }
    //시작버튼
    private fun startTimer() {
        elapsedRealTime=0L//0초로 초기화
        click=0L
        // 시, 분, 초 입력을 가져와 밀리초로 변환
        val hours = etHour.text.toString().toLongOrNull() ?: 0L
        val minutes = etMinute.text.toString().toLongOrNull() ?: 0L
        val seconds = etSecond.text.toString().toLongOrNull() ?: 0L
        if(selectedValue>1) { //시간 느리게 가기
            remainingMillis = ((hours * 3600 + minutes * 60 + seconds) * 1000)* selectedValue.toLong() +1
        }
        if (selectedValue<=1) {
            remainingMillis = ((hours * 3600 + minutes * 60 + seconds) * 1000)+1
        }

        //설정 시간 설정
        ckTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        //입력필드 안 보이게 하기
        etHour.visibility = View.GONE
        etMinute.visibility = View.GONE
        etSecond.visibility = View.GONE
        dot.visibility = View.GONE
        dotBu.visibility = View.GONE
        btnStart.visibility = View.GONE
        numberPicker.visibility = View.GONE

        // 기존 타이머 중지
        countDownTimer?.cancel()

        // 새 타이머 시작 , countdown됨
        createCountDownTimer(remainingMillis).start()
        isTimerRunning = true

        // 버튼 상태 변경
        tvCountdown.visibility = View.VISIBLE
        btnStopTimer.visibility = Button.VISIBLE
        btnClear.visibility = Button.VISIBLE
        btnResumeTimer.visibility = Button.GONE
        btnrecord.visibility = Button.VISIBLE
    }
    private fun record_clear() {
        countDownTimer?.onFinish()
        cancelTimer()
    }

    //초기화버튼
    private fun cancelTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        elapsedRealTime=0L//0초로 초기화
        click=0L
        //타이머 취소
        //타이머 0으로 리셋
        etHour.text.clear()
        etMinute.text.clear()
        etSecond.text.clear()

        //흐르는 시간 초기화
        tvCountdown.text = "00:00:00"
        tvCountdown.visibility = View.GONE

        // 입력 필드 다시 보이게 하기
        numberPicker.visibility = View.VISIBLE
        etHour.visibility = View.VISIBLE
        etMinute.visibility = View.VISIBLE
        etSecond.visibility = View.VISIBLE
        dot.visibility = View.VISIBLE
        dotBu.visibility = View.VISIBLE
        btnStart.visibility = View.VISIBLE

        // 버튼 상태 변경
        btnClear.visibility = Button.GONE
        btnStopTimer.visibility = Button.GONE
        btnResumeTimer.visibility = Button.GONE
        btnrecord.visibility = Button.GONE
        tvCountdown.text = "00:00:00"
    }

    //타이머 중지
    private fun stopTimer() {
        countDownTimer?.cancel() //타이머 멈춤
        isTimerRunning = false //현재 실행 중이 아님
//버튼 상태 변경
        btnStopTimer.visibility = Button.GONE
        btnResumeTimer.visibility = Button.VISIBLE

        // 실제 경과 시간 계산
        elapsedRealTime=click
        // 기록을 시,분,초로 저장
        val elapsedSeconds = elapsedRealTime / 1000
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60

        // 실제 경과 시간 표시
        Toast.makeText(
            requireContext(),
            "선택된 속도: $selectedValue\n실제 경과 시간: $hours:$minutes:$seconds",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun resumeTimer() {
        createCountDownTimer(remainingMillis).start()
        isTimerRunning = true
        btnStopTimer.visibility = Button.VISIBLE
        btnResumeTimer.visibility = Button.GONE
    }
    private fun toggleMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.music_piano)
            mediaPlayer?.isLooping = true // 음악 반복 재생
            mediaPlayer?.start()  // 음악 시작
            Toast.makeText(requireContext(), "음악이 재생 중입니다.", Toast.LENGTH_SHORT).show()
        } else if (mediaPlayer?.isPlaying == true) {
            // 이미 음악이 재생 중일 때, 음악 멈추기
            stopMusic()
            Toast.makeText(requireContext(), "음악이 중지되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 음악이 멈춘 상태라면 재생하기
            mediaPlayer?.start()
            Toast.makeText(requireContext(), "음악이 재생 중입니다.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun stopMusic() {
        mediaPlayer?.stop()  // 음악 멈추기
        mediaPlayer?.release()  // 자원 해제
        mediaPlayer = null  // 객체 참조 초기화
    }
    private fun playAlarmSound() {
        // MediaPlayer 초기화
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_sound)
        // res/raw/alarm_sound.mp3 파일
        mediaPlayer?.setOnCompletionListener {
            stopAlarmSound()
        }
        mediaPlayer?.start() // 알림 소리 재생
    }

    // MediaPlayer 리소스를 해제하는 함수
    private fun stopAlarmSound() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Activity가 종료될 때 MediaPlayer도 해제
    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
    }
    private fun createCountDownTimer(millis: Long): CountDownTimer {
        val speed=(1000 * selectedValue).toLong()
        return object : CountDownTimer(millis, speed) { //1초가 1000
            override fun onTick(millisUntilFinished: Long) {
                if (speed>1000) {
                    remainingMillis -= 1000*selectedValue.toLong() //느린대로 1초 감소, 매틱마다 감소되는 시간 (느리게 가니까 한 틱에 많은 시간을 빼줘야 한다)
                }
                if (speed<=1000){
                    remainingMillis -= 1000L //빠른대로 1초 감소
                }

                click+= speed //실제 실행시간을 구할 땐 speed를 더한다. 0.5초 빠르게 일 경우 500이 된다.
                if (remainingMillis <= 0) {
                    remainingMillis = 0L //남은 시간은 0으로 만들고
                    onFinish()  // onFinish()를 즉시 호출하여 타이머 종료 처리
                    return
                }
                if (speed>1000) {
                    val remainigtime=((remainingMillis)/selectedValue.toLong()) //물리적으로 늘린 시간 표시할 땐 원래대로
                    val remainingHours = (remainigtime / 3600000)
                    val remainingMinutes = ((remainigtime % 3600000) / 60000)
                    val remainingSeconds = ((remainigtime % 60000) / 1000+1)
                    tvCountdown.text = String.format(
                        "%02d:%02d:%02d",
                        remainingHours,
                        remainingMinutes,
                        remainingSeconds
                    )
                }
                else if (speed<=1000) {
                    val remainingHours = remainingMillis / 3600000
                    val remainingMinutes = (remainingMillis % 3600000) / 60000
                    val remainingSeconds = (remainingMillis % 60000) / 1000 +1
                    tvCountdown.text = String.format(
                        "%02d:%02d:%02d",
                        remainingHours,
                        remainingMinutes,
                        remainingSeconds
                    )
                }

                Log.d("Timer", "Remaining millis: $remainingMillis")  // 남은 시간 로그 추가
            }

            override fun onFinish() {
                if (!isTimerRunning) return // 타이머가 이미 멈췄2다면 다시 실행되지 않도록 방지
                stopMusic()
                tvCountdown.text = "타이머 종료"
                isTimerRunning = false
                playAlarmSound()
                btnStopTimer.visibility = Button.GONE
                btnrecord.visibility = Button.GONE

                // 실제 경과 시간 계산
                elapsedRealTime=click

                // 기록을 시,분,초로 저장
                val elapsedSeconds = elapsedRealTime / 1000
                val hours = elapsedSeconds / 3600
                val minutes = (elapsedSeconds % 3600) / 60
                val seconds = elapsedSeconds % 60

                // 실제 경과 시간 표시
                Toast.makeText(
                    requireContext(),
                    "선택된 속도: $selectedValue\n실제 경과 시간: $hours:$minutes:$seconds",
                    Toast.LENGTH_SHORT
                ).show()
///왜 스트링으로 보내요?
                val elapsedTimeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                // 설정한 시간 문자열 (타이머 시작 시 설정한 시간)
                val selectedTime = ckTime.text.toString()  // 설정된 시간을 가져오기
                val setspeed = selectedValue.toString()
                val setmemo = ""
                Log.d("Tir", "Elapsed Time: $elapsedTimeString")
                Log.d("Tir", "Selected Time: $selectedTime")
                // HistoryFragment로 전달할 데이터 준비
                val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date()
                )
                viewModel.setTimerData(currentDate,elapsedTimeString, selectedTime, setspeed, setmemo)
            }
        }.also { countDownTimer = it } ///필수!!!!!!!!!!
    }
}

