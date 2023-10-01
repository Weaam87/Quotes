package com.dailyQuoteApp.quotes.ui


import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.quotes.R
import com.example.quotes.databinding.FragmentStartBinding
import java.util.Calendar


class StartFragment : Fragment() {

    private val viewModel: QuotesViewModel by activityViewModels()

    private var _binding: FragmentStartBinding? = null

    private val binding get() = _binding!!

    // Set the alarm interval to 24 hours
    private val ALARM_INTERVAL_MILLIS = 24 * 60 * 60 * 1000L

    private lateinit var alarmService: AlarmService


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
        // Retrieve the user's preference for checkbox state
        val sharedPreferences =
            requireContext().getSharedPreferences("ModePrefs", Context.MODE_PRIVATE)
        val isCheckboxChecked =
            sharedPreferences.getBoolean("checkbox_state", false) // Default to false if not found

        // Set the checkbox state based on the saved checkbox state
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
            val sharedPreferencesMode =
                requireContext().getSharedPreferences("ModePrefs", Context.MODE_PRIVATE)
            sharedPreferencesMode.edit().putInt("theme_mode", newMode).apply()
            // Save the checkbox state
            sharedPreferences.edit().putBoolean("checkbox_state", binding.changeMode.isChecked)
                .apply()
        }

        alarmService = AlarmService(requireContext())
        binding.setReminder.setOnClickListener {
            showTimePickerDialog { alarmService.setRepetitiveAlarm(it) }
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
            Calendar.getInstance().apply {
                this.set(Calendar.SECOND, 0)
                this.set(Calendar.MILLISECOND, 0)
                TimePickerDialog(
                    context,
                    0,
                    { _, hour, minute ->
                        this.set(Calendar.HOUR_OF_DAY, hour)
                        this.set(Calendar.MINUTE, minute)
                        callback(this.timeInMillis)
                        scheduleDailyAlarm(context, hour, minute)
                        Toast.makeText(
                            context,
                            "Daily reminder set for $hour:$minute",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    this.get(Calendar.HOUR_OF_DAY),
                    this.get(Calendar.MINUTE),
                    false
                ).show()
            }
        }
    }

    private fun scheduleDailyAlarm(context: Context, hour: Int, minute: Int) {
        val alarmManager: AlarmManager? =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
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

        // Set the alarm to repeat daily
        alarmManager?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            ALARM_INTERVAL_MILLIS,
            pendingIntent // Use the PendingIntent with the action set
        )
    }

    private fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        // Cancel the alarm
        alarmManager.cancel(alarmIntent)
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