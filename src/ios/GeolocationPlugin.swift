import CoreLocation
import UIKit
@objc(GeolocationPlugin) class GeolocationPlugin : CDVPlugin, CLLocationManagerDelegate {

    var callbackId: String!
    var manager: CLLocationManager!
    
    //let manager: CLLocationManager = CLLocationManager()
    
    var mode: String!
    var capturing: Bool!
    
    override func pluginInitialize() {
        super.pluginInitialize()
        //NSLog("Location Manager inicialization...")
        self.manager = CLLocationManager()
        self.manager.delegate = self
        self.manager.desiredAccuracy = kCLLocationAccuracyBest
        self.manager.allowsBackgroundLocationUpdates = true
        self.manager.pausesLocationUpdatesAutomatically = false
        //NSLog("Location Manager inicialized.")
        self.capturing = false
    }

    @objc(coolMethod:)
    func coolMethod(_ command: CDVInvokedUrlCommand){
        self.callbackId = command.callbackId;
        let message = command.arguments[0] as? String;

        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: message
        )
        
        self.commandDelegate!.send(pluginResult, callbackId: self.callbackId)
    }

    @objc(Get:)
    func Get(_ command: CDVInvokedUrlCommand){
        self.callbackId = command.callbackId;
        self.mode = "request"
        if CLLocationManager.locationServicesEnabled(){
            NSLog("Location Services are enabled.")
            switch CLLocationManager.authorizationStatus(){
            case .notDetermined, .restricted, .denied:
                NSLog(".notDetermined, .restricted, .denied")
                //NSLog("Requesting authorization...")
                self.manager.requestAlwaysAuthorization()
                
                let pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: "Authorization requested."
                )
                pluginResult?.setKeepCallbackAs(true)
                self.commandDelegate!.send(pluginResult, callbackId: self.callbackId)
                return
            case .authorizedAlways, .authorizedWhenInUse:
                NSLog(".authorizedAlways, .authorizedWhenInUse")
                //self.manager.startUpdatingLocation()
                self.manager.requestLocation()
                
                let jsonString = "{\"status\":\"waiting\"}"
                
                let pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: jsonString
                )
                
                pluginResult?.setKeepCallbackAs(true)
                self.commandDelegate!.send(pluginResult, callbackId: self.callbackId)
                return
            @unknown default:
                NSLog("default")
                break
            }
        }else{
            NSLog("Location services are not enabled.")
            let alert = UIAlertController(title: "Location Services Disabled", message: "Please turn on location services.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Turn On", style: .default, handler: { action in
                NSLog("Openning settings...")
                //UIApplication.shared.open(URL(string:UIApplication.openSettingsURLString)!) //Opens settings, not location services
                let url = URL(string:UIApplication.openSettingsURLString)
                if UIApplication.shared.canOpenURL(url!){
                    // can open succeeded.. opening the url
                    UIApplication.shared.open(url!, options: [:], completionHandler: nil)
                }
            }))
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: { action in
                NSLog("cancel")
            }))
            self.viewController.present(alert, animated: true, completion: nil)
        }
        
        
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR,
            messageAs: "Location was not requested."
        )
        pluginResult?.setKeepCallbackAs(false)
        self.commandDelegate!.send(pluginResult, callbackId: self.callbackId)
    }

    @objc(Start:)
    func Start(_ command: CDVInvokedUrlCommand){
        if !capturing{
            self.callbackId = command.callbackId;
            self.mode = "capturing"
            let waitBetween = command.arguments[0] as? String
            let waitBetweenValue = command.arguments[1] as? String
            let minDistance = command.arguments[2] as? String
            let minDistanceValue = Double(command.arguments[3] as! String)!
            
            self.manager.distanceFilter = minDistanceValue
            NSLog("Minimum distance value\(minDistanceValue)")
            self.capturing = true
            self.manager.startUpdatingLocation()

            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: "{ \"status\":\"\(mode!)\"}"
            )
            pluginResult?.setKeepCallbackAs(true)
            self.commandDelegate!.send(pluginResult, callbackId: self.callbackId)
        }else{
            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: "{ \"status\":\"already capturing\"}"
            )
            pluginResult?.setKeepCallbackAs(true)
            self.commandDelegate!.send(pluginResult, callbackId: self.callbackId)
        }
    }

    @objc(Stop:)
    func Stop(_ command: CDVInvokedUrlCommand){
        if capturing{
            self.callbackId = command.callbackId;
            self.mode = "stoped"
            
            self.manager.stopUpdatingLocation()
            self.capturing = false
            
            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: "{ \"status\":\"\(mode!)\"}"
            )
            pluginResult?.setKeepCallbackAs(false)
            self.commandDelegate!.send(pluginResult, callbackId: self.callbackId)
        }else{
            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: "{ \"status\":\"capture already stoped\"}"
            )
            pluginResult?.setKeepCallbackAs(false)
            self.commandDelegate!.send(pluginResult, callbackId: self.callbackId)
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation])
    {
        NSLog("location updated...");
        guard let first = locations.first else{
            return
        }
        
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: "{ \"status\":\"\(mode!)\", \"latitude\": \"\(first.coordinate.latitude)\",\"longitude\": \"\(first.coordinate.longitude)\", \"altitude\":\"\(first.altitude)\"}"
        )
        if mode == "capturing"{
            pluginResult?.setKeepCallbackAs(true)
        }else{
            pluginResult?.setKeepCallbackAs(false)
        }
        self.commandDelegate!.send(pluginResult, callbackId: self.callbackId)
    }
    
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        NSLog("Authorization changed...")
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error){
        NSLog("An error occurred...")
    }
    
}
/*
extension GeolocationPlugin: CLLocationManagerDelegate{
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation])
    {
        NSLog("location updated...");
        guard let first = locations.first else{
            return
        }
        manager.stopUpdatingLocation()
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: "\(first.coordinate.longitude) | \(first.coordinate.latitude)"
        )
        
        self.commandDelegate!.send(pluginResult, callbackId: self.callbackId)
    }
    
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        NSLog("Authorization changed...");
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error){
        NSLog("An error occurred...")
    }
}
*/
