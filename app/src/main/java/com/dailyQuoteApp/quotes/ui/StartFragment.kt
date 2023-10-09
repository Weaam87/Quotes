package com.dailyQuoteApp.quotes.ui


import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.dailyQuoteApp.quotes.model.QuotesViewModel
import com.dailyQuoteApp.quotes.notifications.AlarmReceiver
import com.dailyQuoteApp.quotes.notifications.AlarmService
import com.dailyQuoteApp.quotes.notifications.BootReceiver
import com.example.quotes.R
import com.example.quotes.databinding.FragmentStartBinding
import java.util.Calendar


class StartFragment : Fragment() {

    private val viewModel: QuotesViewModel by activityViewModels()

    private var _binding: FragmentStartBinding? = null

    private val binding get() = _binding!!


    private lateinit var alarmService: AlarmService

    // Create an instance of BootReceiver
    private val bootReceiver = BootReceiver()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)

        // Create the notification channel using the createChannel function
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_title)
        )
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        /**
         * To see the callback in action, register the callback using the dispatcher,
         * "OnBackPressedDispatcher". P 586
         */
        val slidingPaneLayout = binding.slidingPaneLayout

        // Connect the SlidingPaneLayout to the system back button.
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            QuotesOnBackPressedCallback(slidingPaneLayout)
        )

        // Set an OnEditorActionListener for the EditText to handle the "Done" action (Enter key)
        binding.chooseNumberEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
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
                        // hide the keyboard after clicking Enter
                        hideKeyboard()
                    }
                } else {
                    Toast.makeText(context, "Choose Number", Toast.LENGTH_SHORT).show()
                }
                true // Return true to indicate that the event has been handled
            } else {
                false // Return false for other action events
            }
        }

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
        // Create a function to handle SharedPreferences operations for a given name
        fun getSharedPreferences(name: String) =
            requireContext().getSharedPreferences(name, MODE_PRIVATE)

        // Retrieve the user's preference for checkbox state
        val sharedPreferences = getSharedPreferences("ModePrefs")
        // Default to false if not found
        val isCheckboxChecked = sharedPreferences.getBoolean("checkbox_state", false)
        binding.changeMode.isChecked = isCheckboxChecked

        // Set the click listener for changing the theme mode and saving checkbox state
        binding.changeMode.setOnClickListener {
            val newMode = if (isUsingNightMode()) {
                setDefaultNightMode(MODE_NIGHT_NO)
                MODE_NIGHT_NO
            } else {
                setDefaultNightMode(MODE_NIGHT_YES)
                MODE_NIGHT_YES
            }

            // Save the user's mode preference
            sharedPreferences.edit().putInt("theme_mode", newMode).apply()

            // Save the checkbox state
            sharedPreferences.edit().putBoolean("checkbox_state", binding.changeMode.isChecked)
                .apply()
        }

        alarmService = AlarmService(requireContext())
        // Retrieve the user's preference for the Switch state
        val switchSharedPreferences = getSharedPreferences("SwitchPrefs")
        // Default to false if not found
        val isSwitchChecked = switchSharedPreferences.getBoolean("switch_state", false)

        // Set the Switch state based on the saved state
        binding.setReminder.isChecked = isSwitchChecked

        binding.setReminder.setOnClickListener {
            // Check for notification permission
            if (isNotificationPermissionGranted(requireContext())) {
                if (!alarmIsSet()) {
                    binding.setReminder.isChecked = alarmIsSet()
                    showTimePickerDialog {
                        binding.setReminder.isChecked = alarmIsSet()
                        alarmService.setRepetitiveAlarm(it)
                        // Save the Switch state since the alarm is set
                        switchSharedPreferences.edit().putBoolean("switch_state", true).apply()
                    }
                } else {
                    alarmService.cancelAlarm()
                    bootReceiver.deleteBootReceiverSharedPreferences(requireContext())
                    // Save the Switch state since the alarm is canceled
                    switchSharedPreferences.edit().putBoolean("switch_state", false).apply()
                    binding.setReminder.isChecked = false
                    Toast.makeText(
                        requireContext(),
                        "Alarms and Notifications Canceled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Request notification permission
                requestNotificationPermission()
                // Uncheck the switch
                binding.setReminder.isChecked = false
            }
        }
    }

    private fun alarmIsSet(): Boolean {
        val sharedPreferences =
            requireContext().getSharedPreferences(requireContext().packageName, MODE_PRIVATE)
        val dailyReminderTime = sharedPreferences.getLong("dailyReminderTime", -1)

        return dailyReminderTime != -1L // Return true if dailyReminderTime is not equal to -1
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

    // Create a notification channel
    private fun createChannel(channelId: String, channelName: String) {
        // Check if the Android version is Oreo (API level 26) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a NotificationChannel with the specified channelId and channelName
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false) // Hide the badge on the app icon
            }

            // Configure additional notification channel settings
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Time for Daily Quote"

            // Get the NotificationManager from the activity
            val notificationManager = requireActivity().getSystemService(
                NotificationManager::class.java
            )

            // Create the notification channel using the NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePickerDialog(callback: (Long) -> Unit) {
        val context = requireContext()

        // Check if notification permission is granted
        if (!isNotificationPermissionGranted(context)) {
            // If permission is not granted, request it
            requestNotificationPermission()
        } else {
            // Permission is granted, proceed with showing the TimePickerDialog
            val currentTime = Calendar.getInstance()
            val initialHourOfDay = currentTime.get(Calendar.HOUR_OF_DAY)
            val initialMinute = currentTime.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                context,
                { _, hour, minute ->
                    val selectedTime = Calendar.getInstance()
                    selectedTime.set(Calendar.HOUR_OF_DAY, hour)
                    selectedTime.set(Calendar.MINUTE, minute)
                    selectedTime.set(Calendar.SECOND, 0)
                    selectedTime.set(Calendar.MILLISECOND, 0)

                    val selectedTimeInMillis = selectedTime.timeInMillis

                    // Store the selected time in SharedPreferences
                    val sharedPreferences =
                        context.getSharedPreferences(requireContext().packageName, MODE_PRIVATE)
                    sharedPreferences.edit()
                        .putLong("dailyReminderTime", selectedTimeInMillis)
                        .apply()

                    callback(selectedTimeInMillis)

                    // Schedule the daily alarm using the selected time
                    setAlarm(context, hour, minute)

                    Toast.makeText(
                        context,
                        "Daily reminder set for $hour:$minute",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                initialHourOfDay,
                initialMinute,
                false
            )

            timePickerDialog.show()
        }
    }


    private fun setAlarm(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val alarmPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate the time for the first alarm (tomorrow at the selected hour and minute)
        val currentTimeMillis = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0) // Reset seconds to zero

        // If the chosen time is earlier than the current time, set the alarm for tomorrow
        if (calendar.timeInMillis <= currentTimeMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Set the alarm
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmPendingIntent
        )
    }


    private fun isNotificationPermissionGranted(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestNotificationPermission() {
        val context = requireContext()

        val title = context.getString(R.string.notification_permission_title)
        val message = context.getString(R.string.notification_permission_message)
        val grantPermissionLabel = context.getString(R.string.grant_permission)
        val cancelLabel = context.getString(R.string.cancel)

        // Create a dialog to request notification permission
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(grantPermissionLabel) { _, _ ->
                // Open the app's notification settings
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                startActivity(intent)
            }
            .setNegativeButton(cancelLabel) { _, _ ->
                // Handle the case when the user cancels the permission request
                val canceledMessage = context.getString(R.string.permission_request_canceled)
                Toast.makeText(context, canceledMessage, Toast.LENGTH_SHORT).show()
            }
            .create()

        alertDialog.show()
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