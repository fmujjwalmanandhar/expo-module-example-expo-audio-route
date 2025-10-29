package expo.modules.audioroute

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.net.URL
import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager

class ExpoAudioRouteModule : Module() {
  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  private var audioManager: AudioManager? = null
  private var deviceCallback: AudioDeviceCallback? = null

  override fun definition() = ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoAudioRoute')` in JavaScript.
    Name("ExpoAudioRoute")
    Events("onAudioRouteChange")
    onCreate {
      audioManager = appContext.reactContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager;
    }

    private fun startObservingRouteChanges() {
         val am = audioManager ?: return
         if (deviceCallback != null) return

         deviceCallback = object : AudioDeviceCallback() {
             override fun onAudioDevicesAdded(added: Array<out AudioDeviceInfo>?) {
                 sendEvent("onAudioRouteChange", mapOf("route" to currentRoute()))
               }
             override fun onAudioDevicesRemoved(removed: Array<out AudioDeviceInfo>?) {
                 sendEvent("onAudioRouteChange", mapOf("route" to currentRoute()))
               }
           }
         am.registerAudioDeviceCallback(deviceCallback, null)
       }
    private fun stopObservingRouteChanges() {
        val am = audioManager ?: return
        deviceCallback?.let { am.unregisterAudioDeviceCallback(it) }
        deviceCallback = null
       }

    AsyncFunction("getCurrentRouteAsync"){
      currentRoute()

        OnStartObserving("onAudioRouteChange") {
              startObservingRouteChanges()
            }

        OnStopObserving("onAudioRouteChange")  {
              stopObservingRouteChanges()
            }
  }

  private fun currentRoute(): String {
    val am = audioManager ?: return "unknown"
    val outputs = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

    // Check in priority order: wired > bluetooth > speaker
    val wiredTypes = listOf(
      AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
      AudioDeviceInfo.TYPE_WIRED_HEADSET,
    )

    val bluetoothTypes = listOf(
      AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
      AudioDeviceInfo.TYPE_BLUETOOTH_SCO
    )

    val speakerTypes = listOf(
      AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
      AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
    )

    val device = outputs.firstOrNull { it.type in wiredTypes }
      ?: outputs.firstOrNull { it.type in bluetoothTypes }
      ?: outputs.firstOrNull { it.type in speakerTypes }

    return when (device?.type) {
      in wiredTypes -> "wiredHeadset"
      in bluetoothTypes -> "bluetooth"
      in speakerTypes -> "speaker"
      else -> "unknown"
    }
  }
}
