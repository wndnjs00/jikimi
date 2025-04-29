package com.example.jikimi.presentation.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jikimi.data.model.entity.LikeEntity
import com.example.jikimi.databinding.CustomDialogBinding
import com.example.jikimi.databinding.FragmentLikeBottomSheetBinding
import com.example.jikimi.presentation.VisibilityView
import com.example.jikimi.presentation.adapter.LikeAdapter
import com.example.jikimi.viewmodel.SharedViewModel
import com.example.jikimi.viewmodel.LikeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LikeBottomSheetFragment : BottomSheetDialogFragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentLikeBottomSheetBinding? = null

    private val likeViewModel : LikeViewModel by viewModels()
    private lateinit var likeEntity: LikeEntity

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val likeAdapter : LikeAdapter by lazy {
        LikeAdapter(
            onClick = { item, position ->
                likeEntity = item
                // 클릭한 위경도, 시도명 전달
                sharedViewModel.selectLikeEntity(likeEntity)
                dismiss()
            },

            onLongClick = {item, positon ->
                likeEntity = item
                CustomDialog(likeEntity)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLikeBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adjustBottomSheetHeight()
        setRecyclerView()
        setObserve()
    }

    // 바텀시트 사이즈 조절함수
    private fun adjustBottomSheetHeight() {
        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                // 화면 높이의 75%를 강제로 적용
                val targetHeight = (resources.displayMetrics.heightPixels * 0.75).toInt()
                it.layoutParams.height = targetHeight
                it.requestLayout()

                // BottomSheetBehavior를 적용
                val behavior = BottomSheetBehavior.from(it)
                behavior.peekHeight = targetHeight // peekHeight와 동일하게 설정
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true // 중간 상태를 건너뜀
            }
        }
    }


    private fun setRecyclerView(){
        with(binding.likeRecyclerView){
            adapter = likeAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }


    private fun setObserve(){
        viewLifecycleOwner.lifecycleScope.launch {
            likeViewModel.likeEntity.collect { likeShelter ->

                likeAdapter.submitList(likeShelter)
                likeViewModel.visibilityView()

                binding.likeCountTv.text = "총 ${likeShelter.size}개"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            likeViewModel.visibilityView.collect{ shelterView ->

                when(shelterView){
                    VisibilityView.EMPTYVIEW -> {
                        with(binding){
                            likeEmptyTv.visibility = View.VISIBLE
                            likeEmptyIv.visibility = View.VISIBLE
                            likeRecyclerView.visibility = View.INVISIBLE
                        }
                    }
                    VisibilityView.RECYCLERVIEW -> {
                        with(binding){
                            likeEmptyTv.visibility = View.INVISIBLE
                            likeEmptyIv.visibility = View.INVISIBLE
                            likeRecyclerView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun CustomDialog(likeEntity: LikeEntity){
        val dialog = Dialog(requireContext())
        val dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.dialogCancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.dialogDeleteBtn.setOnClickListener {
            likeViewModel.deleteData(likeEntity.vtAcmdfcltyNm)
            Snackbar.make(dialogBinding.dialogDeleteBtn, "좋아요가 삭제되었습니다.", Snackbar.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}