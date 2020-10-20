@objc(GeolocationPlugin) class GeolocationPlugin : CDVPlugin {

    @IBOutlet var callbackId: String!

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

    }

    @objc(Start:)
    func Start(_ command: CDVInvokedUrlCommand){
        self.callbackId = command.callbackId;
        let waitBetween = command.arguments[0] as? String;
        let waitBetweenValue = command.arguments[1] as? String;
        let minDistance = command.arguments[2] as? String;
        let minDistanceValue = command.arguments[3] as? String;


    }

    @objc(Stop:)
    func Stop(_ command: CDVInvokedUrlCommand){
        self.callbackId = command.callbackId;
    }

}