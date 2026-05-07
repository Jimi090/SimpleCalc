package com.example.calculator4android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calculator4android.databinding.HistoryWindowBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.getValue
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import android.content.res.Resources
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


class HistoryWindow : BottomSheetDialogFragment() {
    val REQUEST_KEY = "REQUEST_KEY"
    val DATA_KEY = "DATA_KEY"
    private lateinit var binding: HistoryWindowBinding
    private val mainVM by activityViewModels<MainViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val historyElements = mainVM.history.reversed()
        binding = HistoryWindowBinding.inflate(inflater,container,false)
        val adapter = HistoryAdapter(historyElements){ item ->
            historyElementClicked(item)
        }

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = adapter

        binding.closeButton.setOnClickListener {
                dismiss()
        }
        binding.clearHistory.setOnClickListener {
            val activity = requireActivity() as MainActivity
            activity.clearHistory()
            adapter.updateHistory(arrayListOf<HistoryElement>())
        }

        return binding.root
    }
    fun historyElementClicked(item: HistoryElement){
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY, Bundle().apply {
                putString(DATA_KEY, Gson().toJson(item))
            }
        )
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return
        
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.peekHeight = 500.dpToPx()
    }
    fun Int.dpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()
}