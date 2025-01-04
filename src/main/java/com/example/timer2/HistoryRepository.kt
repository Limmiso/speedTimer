package com.example.timer2

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import java.util.Objects

data class HistoryData(
    val date: String,
    val selectedTime: String,
    val elapsedTime: String,
    val speed: String,
    val memo: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as HistoryData

        val thisMemo = if (this.memo.trim().isEmpty()) "메모 없음" else this.memo.trim()
        val otherMemo = if (other.memo.trim().isEmpty()) "메모 없음" else other.memo.trim()

        return date.trim() == other.date.trim() &&
                selectedTime.trim() == other.selectedTime.trim() &&
                elapsedTime.trim() == other.elapsedTime.trim() &&
                speed.trim() == other.speed.trim() &&
                thisMemo == otherMemo
    }

    override fun hashCode(): Int {
        return Objects.hash(date, selectedTime, elapsedTime, speed, memo)
    }
}
class HistoryRepository(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // 저장된 모든 기록을 가져오기
    fun getAllHistories(): List<HistoryData> {
        val json = sharedPreferences.getString("history_list", "[]")
        val historyList = gson.fromJson(json, Array<HistoryData>::class.java).toList()
        Log.d("HistoryRepository", "Fetched histories: $historyList")
        return historyList
    }
    // 새로운 기록 저장
    fun insertHistory(date: String, selectedTime: String, elapsedTime: String, speed: String, memo: String) {
        val currentHistories = getAllHistories().toMutableList()
        currentHistories.add(HistoryData(date, selectedTime, elapsedTime, speed, memo))
        saveHistories(currentHistories)
    }
    fun  updateHistory(date: String, selectedTime: String, elapsedTime: String, speed: String, memo: String) {
        //Log.d("tunotuno", "updateHistory called with date: $date, selectedTime: $selectedTime, elapsedTime: $elapsedTime, speed: $speed, memo: $memo")
        val currentHistories = getAllHistories().toMutableList()

        val index = currentHistories.indexOfFirst { it.date == date && it.selectedTime == selectedTime && it.elapsedTime == elapsedTime && it.speed == speed }
       // Log.d("HR", "Updated history: $index")
            // 기존 기록이 있으면, 메모만 업데이트
        val updatedHistory = currentHistories[index].copy(memo = memo)
        currentHistories[index] = updatedHistory
        saveHistories(currentHistories)
        //Log.d("HistoryRepository", "Updated history: $updatedHistory")
    }
    //삭제 데이터 삭제
    fun deleteHistory(date: String, selectedTime: String, elapsedTime: String, speed: String, memo: String) {
        val currentHistories = getAllHistories().toMutableList()
        //Log.d("HFt", "Attempting to delete: date=$date, time=$selectedTime, elapsed=$elapsedTime, speed=$speed")
        val isRemoved = currentHistories.removeIf {
                    it.selectedTime == selectedTime &&
                    it.elapsedTime == elapsedTime &&
                    it.speed == speed
            // 메모 비교 제외
        }

        Log.d("HFti", "Is removed: $isRemoved")
        Log.d("HFtl", "Updated history list after removal: $currentHistories")

        if (isRemoved) {
            saveHistories(currentHistories) // 삭제 후 다시 저장
            Log.d("HFtt", "Successfully removed history")
        } else {
            Log.d("HFtr", "History data not found")
        }
    }

    // 기록을 SharedPreferences에 저장
    private fun saveHistories(historyList: List<HistoryData>) {
        val json = gson.toJson(historyList)
        Log.d("HFtp", "Deleting historyData: $json")
        sharedPreferences.edit().putString("history_list", json).commit()
    }
}
