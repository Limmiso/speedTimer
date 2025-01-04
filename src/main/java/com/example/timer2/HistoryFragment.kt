package com.example.timer2
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment(R.layout.fragment_history) {
    private lateinit var viewModel: TimerViewModel
    private lateinit var historyLayout: LinearLayout
    private var lastDate: String? = null
    private lateinit var repository: HistoryRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewModel을 Activity 범위로 가져오기
        viewModel = ViewModelProvider(requireActivity()).get(TimerViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        repository = HistoryRepository(requireContext())
        historyLayout = view.findViewById(R.id.tv_history)

        // ViewModel의 timerData를 이용하여 기록 추가
        viewModel.timerData.observe(viewLifecycleOwner) { timerData ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            val historyData = HistoryData(
                date = currentDate,
                selectedTime = timerData.selectedTime,
                elapsedTime = timerData.elapsedTime,
                speed = timerData.speed,
                memo = timerData.memo// 기본 메모
            )
            // 새로운 기록 저장 및 UI 업데이트
            repository.insertHistory(
                date = historyData.date,
                selectedTime = historyData.selectedTime,
                elapsedTime = historyData.elapsedTime,
                speed = historyData.speed,
                memo = historyData.memo
            ).also {
                Log.d("HistoryFragment", "Record inserted: $it")
            }
            updateHistory(timerData) // TimerData로 전달
        }
        loadHistory()
        return view
    }

    private fun loadHistory() {
        val historyList = repository.getAllHistories()
        Log.d("HistoryFragment", "Loaded histories: $historyList")
        historyLayout.removeAllViews()  // 기존 UI 초기화

        historyList.forEach { history ->
            // HistoryData를 TimerData로 변환 후 updateHistory 호출
            val timerData = TimerData(
                date = history.date,
                elapsedTime = history.elapsedTime,
                selectedTime = history.selectedTime,
                speed = history.speed,
                memo = history.memo
            )
            updateHistory(timerData)
        }
    }

    private fun updateHistory(timerData: TimerData) {
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.HOUR_OF_DAY) <0) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        // 같은 날짜가 계속 나오지 않도록 처리
        if (currentDate != lastDate) { //오늘 날짜
            lastDate = currentDate //날짜 업데이트
            // 날짜 텍스트 추가
            val dateText = TextView(requireContext()).apply {
                text = currentDate
                textSize = 20f
                setPadding(16, 16, 16, 3)
                setTextColor(resources.getColor(android.R.color.holo_blue_dark))
            }
            historyLayout.addView(dateText)
        }

        // 설정된 시간, 실제 경과 시간, 빠르기 출력
        val historyText = TextView(requireContext()).apply {
            text = "설정한 시간: ${timerData.selectedTime}, 실제 경과 시간: ${timerData.elapsedTime}, 빠르기: ${timerData.speed}"
            textSize = 16f
            setPadding(16, 16, 16, 0)
        }
        val dateTextView = TextView(requireContext()).apply {
            text = "날짜: ${timerData.date}" // 저장된 날짜 사용
            textSize = 16f
            setPadding(0, 0, 0, 8)
        }
        val memoEditText = EditText(requireContext()).apply {
            hint = "메모를 입력하세요"
            setPadding(16, 16, 16, 16)
            setText(timerData.memo)  // 기존 메모 값을 표시하도록 설정
        }

        val deleteButton = Button(requireContext()).apply {
            text = "기록 삭제"
        }
        // 삭제 버튼 클릭 리스너에서 historyLayoutWithDelete를 참조
        val historyLayoutWithDelete = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            addView(dateTextView)
            addView(historyText)
            addView(memoEditText)
            addView(deleteButton)

            memoEditText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    memoEditText.clearFocus() // 포커스를 제거하여 커서가 깜빡이지 않도록 함
                    val updatedMemo = memoEditText.text.toString().trim()
                    // 기존 기록을 업데이트
                    repository.updateHistory(
                        date = timerData.date,
                        selectedTime = timerData.selectedTime,
                        elapsedTime = timerData.elapsedTime,
                        speed = timerData.speed,
                        memo = updatedMemo
                    )

                }
            }
            // 삭제 버튼 클릭 시 해당 레이아웃을 삭제
            deleteButton.setOnClickListener {
                // timerData를 HistoryData로 변환
                val historyData = HistoryData(
                    date = currentDate,
                    selectedTime = timerData.selectedTime,
                    elapsedTime = timerData.elapsedTime,
                    speed = timerData.speed,
                    memo = memoEditText.text.toString()
                )
                Toast.makeText(
                    requireContext(),
                    "기록이 삭제되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("HistoryFragment", "Deleting historyData: $historyData")
                repository.deleteHistory(
                    date = historyData.date,
                    selectedTime = historyData.selectedTime,
                    elapsedTime = historyData.elapsedTime,
                    speed = historyData.speed,
                    memo = historyData.memo
                )
                historyLayout.removeView(this) // historyLayoutWithDelete 자체를 삭제
            }
        }
        historyLayout.addView(historyLayoutWithDelete)
    }
}