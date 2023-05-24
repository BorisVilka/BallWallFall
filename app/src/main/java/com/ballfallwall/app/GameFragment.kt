package com.ballfallwall.app

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.ballfallwall.app.databinding.FragmentGameBinding


class GameFragment : Fragment() {

    private lateinit var binding: FragmentGameBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGameBinding.inflate(inflater,container,false)
        val ctx = requireContext()
        binding.game.setEndListener(object : GameView.Companion.EndListener  {
            override fun end() {
                requireActivity().runOnUiThread {
                    binding.game.togglePause()
                    binding.lose.visibility = View.VISIBLE
                    val set = ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE).getStringSet("score",HashSet<String>())
                    val set1 = HashSet<String>()
                    set1.addAll(set!!.toList())
                    if(!set1.contains(binding.game.score.toString())) set1.add(binding.game.score.toString())
                    ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE).edit().putStringSet("score",set1).apply()
                }
            }

            override fun score(score: Int) {

            }

        })
        binding.lose.setOnClickListener {
            val navController = Navigation.findNavController(requireActivity(),R.id.fragmentContainerView)
            navController.popBackStack()
        }
        binding.button.setOnClickListener {
            val navController = Navigation.findNavController(requireActivity(),R.id.fragmentContainerView)
            navController.popBackStack()
        }
        binding.imageView10.setOnClickListener {
            binding.game.togglePause()
            binding.pause.visibility = View.VISIBLE
            binding.imageView10.setImageResource(R.drawable.play)
        }
        binding.imageView12.setOnClickListener {
            binding.game.togglePause()
            binding.pause.visibility = View.GONE
            binding.imageView10.setImageResource(R.drawable.pause)
        }
        return binding.root
    }


}