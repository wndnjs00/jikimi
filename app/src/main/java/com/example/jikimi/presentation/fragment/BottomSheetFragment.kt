package com.example.jikimi.presentation.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.example.jikimi.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetFragment : BottomSheetDialogFragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentBottomSheetBinding? = null


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
    }


    companion object{
        private const val OUTDOOR_SHELTER_DATA = "outdoor_shelter_data"
        fun outdoorNewInstance(outdoorShelterData : EarthquakeOutdoorsShelterResponse.EarthquakeOutdoorsShelter2.Row) : BottomSheetFragment{
            val fragment = BottomSheetFragment()
            val args = Bundle().apply {
                putParcelable(OUTDOOR_SHELTER_DATA, outdoorShelterData)
            }
            fragment.arguments = args
            return fragment
        }
    }


    // 야외대피소 데이터 받아오기
    private fun getOutdoorShelterData(){

        val outdoorShelter = arguments?.getParcelable<EarthquakeOutdoorsShelterResponse.EarthquakeOutdoorsShelter2.Row>(OUTDOOR_SHELTER_DATA)
        Log.d("BottomSheetFragment", "Shelter Name: ${outdoorShelter}")

        binding.shelterTv.text = "야외대피장소"
        binding.shelterNameTv.text = outdoorShelter?.vtAcmdfcltyNm ?: "데이터없음"
        binding.shelterClassificationTv.text = outdoorShelter?.acmdfcltySeNm ?: "데이터없음"
        binding.shelterPhoneTv.text = outdoorShelter?.mngpsTelno ?: "데이터없음"
        binding.shelterPeopleConstraint.visibility = View.GONE

        // 주소 데이터 설정
        binding.shelterAddressTv.text = when {
            !outdoorShelter?.rnAdres.isNullOrEmpty() -> outdoorShelter?.rnAdres // rnAdres가 존재하면 사용
            !outdoorShelter?.dtlAdres.isNullOrEmpty() -> outdoorShelter?.dtlAdres // rnAdres가 null이면 dtlAdres 사용
            else -> "데이터가 없음" // 두 값이 모두 null이면 기본 텍스트
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}