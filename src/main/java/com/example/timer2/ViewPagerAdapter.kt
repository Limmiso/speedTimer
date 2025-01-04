package com.example.timer2.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.timer2.HistoryFragment
import com.example.timer2.TimerFragment


class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 2 // 타이머 화면과 기록 화면 두 개
    }
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TimerFragment()
            1 -> HistoryFragment() // 두 번째 페이지는 기록 화면
            else -> TimerFragment()
        }
    }
}


