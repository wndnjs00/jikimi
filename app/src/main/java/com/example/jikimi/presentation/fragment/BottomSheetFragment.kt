package com.example.jikimi.presentation.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.jikimi.R
import com.example.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.example.jikimi.data.model.entity.LikeEntity
import com.example.jikimi.databinding.FragmentBottomSheetBinding
import com.example.jikimi.viewmodel.LikeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BottomSheetFragment : BottomSheetDialogFragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentBottomSheetBinding? = null

    private val likeViewModel : LikeViewModel by viewModels()

    private var likeEntity : LikeEntity? = null
    private var likeClick = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getOutdoorShelterData()
        getIndoorShelterData()
        likeClickListener()
        liked()
    }


    companion object{
        private const val OUTDOOR_SHELTER_DATA = "outdoor_shelter_data"
        private const val OUTDOOR_DISTANCE_DATA = "outdoor_distance_data"
        private const val INDOOR_SHELTER_DATA = "indoor_shelter_data"
        private const val INDOOR_DISTANCE_DATA = "indoor_distance_data"

        fun outdoorNewInstance(outdoorShelterData : EarthquakeOutdoorsShelterResponse.EarthquakeOutdoorsShelter2.Row, distance: Double) : BottomSheetFragment{
            val fragment = BottomSheetFragment()
            val outdoorArgs = Bundle().apply {
                putParcelable(OUTDOOR_SHELTER_DATA, outdoorShelterData)
                putDouble(OUTDOOR_DISTANCE_DATA, distance)
            }
            fragment.arguments = outdoorArgs
            return fragment
        }

        fun indoorNewInstance(indoorShelterData : EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row, distance: Double) : BottomSheetFragment{
            val fragment = BottomSheetFragment()
            val indoorArgs = Bundle().apply {
                putParcelable(INDOOR_SHELTER_DATA, indoorShelterData)
                putDouble(INDOOR_DISTANCE_DATA, distance)
            }
            fragment.arguments = indoorArgs
            return fragment
        }
    }


    // 야외대피소 데이터 받아오기
    private fun getOutdoorShelterData(){

        val outdoorShelter = arguments?.getParcelable<EarthquakeOutdoorsShelterResponse.EarthquakeOutdoorsShelter2.Row>(OUTDOOR_SHELTER_DATA)
        Log.d("BottomSheetFragment", "Shelter Name: ${outdoorShelter}")

        val distance = arguments?.getDouble(OUTDOOR_DISTANCE_DATA)

        if (outdoorShelter != null) {
            with(binding){
                shelterTv.text = "야외대피장소"
                distanceTv.text = "${String.format("%.2f", distance ?: 0.0)} m"
                shelterNameTv.text = outdoorShelter.vtAcmdfcltyNm ?: "데이터 없음"
                shelterClassificationTv.text = outdoorShelter.acmdfcltySeNm ?: "데이터 없음"
                shelterPhoneTv.text = outdoorShelter.mngpsTelno ?: "데이터 없음"
                shelterPeopleConstraint.visibility = View.GONE

                // 주소 데이터 설정
                shelterAddressTv.text = when {
                    !outdoorShelter?.rnAdres.isNullOrEmpty() -> outdoorShelter?.rnAdres // rnAdres가 존재하면 사용
                    !outdoorShelter?.dtlAdres.isNullOrEmpty() -> outdoorShelter?.dtlAdres // rnAdres가 null이면 dtlAdres 사용
                    else -> "데이터가 없음" // 두 값이 모두 null이면 기본 텍스트
                }
            }

            likeEntity = LikeEntity(
                vtAcmdfcltyNm = outdoorShelter.vtAcmdfcltyNm ?: "",
                rnAdres = outdoorShelter.rnAdres ?: "",
                vtAcmdPsblNmpr = outdoorShelter.vtAcmdPsblNmpr ?: "",
                dtlAdres = outdoorShelter.dtlAdres ?: ""
            )
        }
    }


    //실내대피소 데이터 받아오기
    private fun getIndoorShelterData(){

        val indoorShelter = arguments?.getParcelable<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row>(INDOOR_SHELTER_DATA)
        Log.d("BottomSheetFragment", "Shelter Name: ${indoorShelter}")

        val distance = arguments?.getDouble(INDOOR_DISTANCE_DATA)

        with(binding){
            if (indoorShelter != null) {
                shelterTv.text = "임시주거시설"
                shelterConstraint.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue))
                distanceTv.text = "${String.format("%.2f", distance ?: 0.0)} m"
                shelterNameTv.text = indoorShelter.vtAcmdfcltyNm ?: "데이터 없음"
                shelterClassificationTv.text = indoorShelter.acmdfcltyDtlCn ?: "데이터 없음"
                shelterPhoneTv.text = indoorShelter.mngpsTelno ?: "데이터 없음"
                shelterPeopleConstraint.visibility = View.VISIBLE
                shelterPeopleTv.text = "수용인원: ${indoorShelter.vtAcmdPsblNmpr ?: "데이터 없음"}명"

                // 주소 데이터 설정
                shelterAddressTv.text = when {
                    !indoorShelter.rnAdres.isNullOrEmpty() -> indoorShelter.rnAdres
                    !indoorShelter.dtlAdres.isNullOrEmpty() -> indoorShelter.dtlAdres
                    else -> "데이터가 없음"
                }
            }

            if (indoorShelter != null) {
                likeEntity = LikeEntity(
                    vtAcmdfcltyNm = indoorShelter.vtAcmdfcltyNm ?: "",
                    rnAdres = indoorShelter.rnAdres ?: "",
                    vtAcmdPsblNmpr = indoorShelter.vtAcmdPsblNmpr ?: "",
                    dtlAdres = indoorShelter.dtlAdres ?: ""
                )
            }
        }
    }


    private fun likeClickListener(){
        // 좋아요 상태 검사에 따른 UI 업데이트
        binding.emptyHeartIv.setOnClickListener {

            likeClick = !likeClick

            if (likeEntity != null && likeClick){
                // Room 사용해서 데이터저장
                likeViewModel.saveData(likeEntity!!)
                binding.emptyHeartIv.setImageResource(R.drawable.full_heart_img)
            }else{
                // Room 사용해서 데이터 삭제
                likeViewModel.deleteData(likeEntity!!.vtAcmdfcltyNm)
                binding.emptyHeartIv.setImageResource(R.drawable.empty_heart_img)
                }
            }
        }


    private fun liked(){
        viewLifecycleOwner.lifecycleScope.launch {
            likeViewModel.likeEntity.collect{ like ->
                // vtAcmdfcltyNm가 일치하는지 확인 (vtAcmdfcltyNm로 좋아요 여부확인)
                val isLiked = like.any {it.vtAcmdfcltyNm == likeEntity?.vtAcmdfcltyNm}

                binding.emptyHeartIv.setImageResource(
                    if(isLiked){
                        R.drawable.full_heart_img
                    }else{
                        R.drawable.empty_heart_img
                    }
                )
                likeClick = isLiked
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}