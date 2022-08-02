package com.example.quotes.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.quotes.R
import com.example.quotes.databinding.FragmentStartBinding
import com.example.quotes.model.QuotesViewModel


class StartFragment : Fragment() {

    private val viewModel: QuotesViewModel by activityViewModels()

    private var _binding: FragmentStartBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.getQuoteNumber.setOnClickListener {
            val stringInTextField = binding.chooseNumberEditText.text.toString()
            val userIndex = stringInTextField.toIntOrNull()
            if (userIndex != null) {
                binding.chooseNumberEditText.text?.clear()
                if (userIndex > 100 || userIndex == 0) {
                    Toast.makeText(context, "Choose Number Between 1 and 100", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    findNavController().navigate(R.id.action_startFragment_to_quotes)
                    viewModel.getIndex(userIndex-1)
                }
            } else {
                Toast.makeText(context, "Choose Number", Toast.LENGTH_SHORT).show()
            }
        }
        binding.getRandomQuote.setOnClickListener {
            val randomIndex = (0..99).random()
            viewModel.getIndex(randomIndex)
            findNavController().navigate(R.id.action_startFragment_to_quotes)

        }
    }

    /**
    Clear out the binding object when the view hierarchy associated with the fragment
    is being removed
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}