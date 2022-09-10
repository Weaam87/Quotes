package com.example.quotes.ui


import android.app.*
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.example.quotes.R
import com.example.quotes.databinding.FragmentStartBinding
import com.example.quotes.model.QuotesViewModel
import com.example.quotes.service.AlarmService
import java.util.*


class StartFragment : Fragment() {

    private val viewModel: QuotesViewModel by activityViewModels()

    private var _binding: FragmentStartBinding? = null

    private val binding get() = _binding!!

    private lateinit var alarmService: AlarmService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        alarmService = AlarmService(requireContext())

        binding.setReminder.setOnClickListener {
            setAlarm { alarmService.setRepetitiveAlarm(it) }
        }
        /**
         * To see the callback in action, register the callback using the dispatcher,
         * "OnBackPressedDispatcher". P 586
         */
        val slidingPaneLayout = binding.slidingPaneLayout

        // Connect the SlidingPaneLayout to the system back cancel_reminder.
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            QuotesOnBackPressedCallback(slidingPaneLayout)
        )

        binding.getQuoteNumber.setOnClickListener {
            val stringInTextField = binding.chooseNumberEditText.text.toString()
            val userIndex = stringInTextField.toIntOrNull()
            if (userIndex != null) {
                binding.chooseNumberEditText.text?.clear()
                if (userIndex > 100 || userIndex == 0) {
                    Toast.makeText(context, "Choose Number Between 1 and 100", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    binding.slidingPaneLayout.openPane()
                    viewModel.getIndex(userIndex - 1)
                    // hide the keyboard after click
                    hideKeyboard()
                }
            } else {
                Toast.makeText(context, "Choose Number", Toast.LENGTH_SHORT).show()
            }
        }
        binding.getRandomQuote.setOnClickListener {
            val randomIndex = (0..99).shuffled().random()
            viewModel.getIndex(randomIndex)
            binding.slidingPaneLayout.openPane()
            hideKeyboard()
        }
        // change Mode
        binding.changeMode.setOnClickListener {
            if (isUsingNightMode()) {
                setDefaultNightMode(MODE_NIGHT_NO)
            } else {
                setDefaultNightMode(MODE_NIGHT_YES)
            }
        }
    }

    private fun setAlarm(callback: (Long) -> Unit) {
        Calendar.getInstance().apply {
            this.set(Calendar.SECOND, 0)
            this.set(Calendar.MILLISECOND, 0)
            TimePickerDialog(
                requireContext(),
                0,
                { _, hour, minute ->
                    this.set(Calendar.HOUR_OF_DAY, hour)
                    this.set(Calendar.MINUTE, minute)
                    callback(this.timeInMillis)
                    Toast.makeText(context, "Quotes reminder is set", Toast.LENGTH_SHORT).show()
                },
                this.get(Calendar.HOUR_OF_DAY),
                this.get(Calendar.MINUTE),
                false
            ).show()
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        // create a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.description = "Time for new quote"

            val notificationManager = requireActivity().getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    //check whether the user is using dark mode or not
    private fun isUsingNightMode(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
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

// Add custom back navigation.
// This class will ensure that the callback is only enabled on the smaller screen devices and
//  when the content pane is open.
class QuotesOnBackPressedCallback(private val slidingPaneLayout: SlidingPaneLayout) :
    OnBackPressedCallback(slidingPaneLayout.isSlideable && slidingPaneLayout.isOpen),
    SlidingPaneLayout.PanelSlideListener {

    // Add the ((QuotesOnBackPressedCallback listener class)) to the ((list of listeners)) that
    // will be notified of the(( Quotes pane slide events)).
    init {
        slidingPaneLayout.addPanelSlideListener(this)
    }

    // These methods have no effect if both panes are visible and do not overlap.
    override fun handleOnBackPressed() {
        // Close the quotes pane and return to the start pane.
        slidingPaneLayout.closePane()
    }

    /**
     * Monitor SlidingPaneLayout's events :
     * In addition to handling back press events, you must listen and monitor events related to the
     * sliding pane. When the content pane slides,the callback should be enabled or disabled accordingly.
     */
    override fun onPanelSlide(panel: View, slideOffset: Float) {}

    override fun onPanelOpened(panel: View) {
        // Enable the (OnBackPressedCallback) callback,when the details pane is opened (is visible).
        isEnabled = true
    }

    override fun onPanelClosed(panel: View) {
        // set isEnabled to false, when the details pane is closed.
        isEnabled = false
    }
}