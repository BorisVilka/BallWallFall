package com.ballfallwall.app

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.ballfallwall.app.databinding.FragmentScoreBinding


class ScoreFragment : Fragment() {

    private lateinit var binding: FragmentScoreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentScoreBinding.inflate(inflater,container,false)
        var list = requireActivity().getSharedPreferences("prefs",Context.MODE_PRIVATE).getStringSet("score",HashSet<String>())!!.map { it.toInt() }.toMutableList()
            .sortedBy { -it }.map { it.toString() }.toMutableList()
        var adapter = ScoreAdapter(list)
        binding.list.adapter = adapter
        binding.back.setOnClickListener {
            val navController = Navigation.findNavController(requireActivity(),R.id.fragmentContainerView)
            navController.popBackStack()
        }
        return binding.root
    }

}