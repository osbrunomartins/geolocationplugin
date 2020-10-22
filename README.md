# geolocationplugin
This plugin provides information about the device's location, such as latitude and longitude.

## Instalation

```sh
cordova plugin add https://github.com/osbrunomartins/geolocationplugin.git
```

## Available Features
- Current location of the device;
- Continuously capture the devices location;

### Get
Returns the current locations of the device.
```javascript
cordova.plugins.GeolocationPlugin.getLocation(success, fail);
```

### Start
Starts the continuous capture of the devices location.
```javascript
cordova.plugins.GeolocationPlugin.startCapture(valueOfWaitBetween, valueOfMinimumDistance, success, fail);
```

- valueOfWaitBetween: Time between each request for location in milliseconds.
- valueOfMinimumDistance: Minimum distance between each updated location in meters.

### Stop
Stops the recording of location.
```javascript
cordova.plugins.GeolocationPlugin.stopCapture(success, fail);
```

### JSON Format
The plugin returns the location in the given format:

```json
{
  "status": "theStatus",
  "latitude": "latitudeValue",
  "longitude": "longitudeValue",
  "altitude": "altitudeValue"
}
```

Possible statuses:
- status: waiting, request, capturing, stopped

## Supported Platforms
- Android 8.0+
- iOS 9.0 +

## Examples
Add the buttons and labels to your html.

```html
<button id="getlocation" >Get Location</button>
<p>Wait Between Requests</p>
<input id="wait_between" placeholder="1000" type="text" />
<p>Minimum Distance</p>
<input id="min_distance" placeholder="100" type="text" />
<button id="startcapture" >Start Capture</button>
<button id="stopcapture" >Stop Capture</button>
<p id="text" class="results"></p>
```

Add the click events.

```javascript
var getlocation = document.getElementById("getlocation");
var starcapture = document.getElementById("startcapture");
var stopcapture = document.getElementById("stopcapture");

function success(message){
  var text = document.getElementById("text");
  text.innerHTML = "Success: " + message;
}

function fail(message){
  var text = document.getElementById("text");
  text.innerHTML = "Error: " + message;
}

getlocation.addEventListener("click",function(){
  cordova.plugins.GeolocationPlugin.getLocation(success, fail);
});

function successStarCapture(message){
  var text = document.getElementById("text");
  var previousText = text.innerHTML;
  text.innerHTML = previousText + "<br>" + message;
}

startcapture.addEventListener("click", function(){
  var text = document.getElementById("text");
  text.innerHTML = "";
  var input_time = document.getElementById("wait_between");
  var input_distance = document.getElementById("min_distance");
  cordova.plugins.GeolocationPlugin.startCapture(input_time.value, input_distance.value, successStarCapture, fail);
});

stopcapture.addEventListener("click", function(){
  cordova.plugins.GeolocationPlugin.stopCapture(successStarCapture, fail);
});
```
