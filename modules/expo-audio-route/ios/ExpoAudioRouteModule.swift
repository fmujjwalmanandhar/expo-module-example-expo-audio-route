import ExpoModulesCore
import AVFoundation

public class ExpoAudioRouteModule: Module {
  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  public func definition() -> ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoAudioRoute')` in JavaScript.
    Name("ExpoAudioRoute")
      
    Events("onAudioRouteChange")
      
    AsyncFunction("getCurrentRouteAsync"){
        self.currentRoute()
      }
  }
    
    private func currentRoute(): String {
        let session = AVAudioSession.sharedInstance()
        let outputs = session.currentRoute.outputs
        
        guard let first = outputs.first else {
               return "unknown"
           }
        
        switch first.portType {
            case .headphones, .headsetMic:
                return "wiredHeadset"
            case .bluetoothA2DP, .bluetoothLE, .bluetoothHFP:
                return "bluetooth"
            case .builtInSpeaker:
                return "speaker"
            default:
                return "unknown"
           }
     }
}
