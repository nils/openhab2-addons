# PioneerAVR Binding Configuration

## Binding configuration

The binding can auto-discover the Pioneer AVRs present on your local network. The auto-discovery is enabled by default. To disable it, you can create a file in the services directory called pioneeravr.cfg with the following content:

```
org.openhab.pioneeravr:enableAutoDiscovery=false
```

This configuration parameter only control the PioneerAVR auto-discovery process, not the openHAB auto-discovery. Moreover,
if the openHAB auto-discovery is disabled, the PioneerAVR auto-discovery is disabled too.

## Thing configuration

In the things folder, create a file called pioneeravr.things (or any other name) and configure your AVRs inside.

The binding can control AVRs through the local network or through a Serial connection if the AVR is directly connected to your computer.


AVR models/thing-types that are supported by the binding:

* **VSX-1022**
* **VSX-1021**
* **VSX-1020**
* **VSX-921**

The thing-type ConfigurablePioneerAVR should be used to test unsupported AVRs. Once the parameters are found for the model, please contact the binding maintainer to add the model in the supported list.

The following parameters are available for all thing-types:

* **ipAddress**: the hostname/ipAddress of the AVR on the local network. (mandatory if useSerial is false)
* **tcpPort**: the port number to use to connect to the AVR. If default value does not work, you may try 8102. (optional, default to 23)
* **useSerial**: use the serial port to communicate with the AVR instead of IP. (optional, default to false)
* **serialPort**: the name of the serial port to use on the computer. (mandatory if userSerial is true)

The ConfigurablePioneerAvr thing-type also defines the below parameters:

* **nbZones**: defines the number of zones supported by the AVR. (default to 1)
* **setVolumeCommandEnabled**: enable/disable the use of the SetVolume command. Not all AVR are compatible with this command. If nothing happens when volume is changed, disable it. (default to true).
* **burstMessageDelay**: the delay in milliseconds between 2 messages in burst mode. This mode is used to set the volume when setVolumeCommandEnabled is false. If the volume change is not accurate, set a higher value. (default to 10).
* **setBurstModeEnabled**: enable/disable the use of the burst mode. If you cannot find a value first the burstMessageDelay, you may try to disable the burst mode. (default to true).
* **volumeMinDbZone1**: set the min volume value of zone 1 in dB (default to -80)
* **volumeMaxDbZone1**: set the max volume value of zone 1 in dB (default to 12)
* **volumeStepDbZone1**: set the number of dB the volume is increased/decreased for each step of zone 1 (default to 0.5)
* **volumeMinDbZone2**: set the min volume value of zone 2 in dB (default to -80)
* **volumeMaxDbZone2**: set the max volume value of zone 2 in dB (default to 0)
* **volumeStepDbZone2**: set the number of dB the volume is increased/decreased for each step of zone 2 (default to 1)
* **volumeMinDbZone3**: set the min volume value of zone 3 in dB (default to -80)
* **volumeMaxDbZone3**: set the max volume value of zone 3 in dB (default to 0)
* **volumeStepDbZone3**: set the number of dB the volume is increased/decreased for each step of zone 3 (default to 1)
* **volumeMinDbZone4**: set the min volume value of zone HD in dB (default to -80)
* **volumeMaxDbZone4**: set the max volume value of zone HD in dB (default to 0)
* **volumeStepDbZone4**: set the number of dB the volume is increased/decreased for each step of zone HD (default to 1)


Example for supported model:

```
pioneeravr:VSX-921:testVsx921IP [ ipAddress="192.168.1.25", tcpPort=8102 ]
pioneeravr:VSX-921:TestVsx921Serial [ useSerial=true, serialPort="COM9" ]
```

Example for unknown model:

```
pioneeravr:ConfigurablePioneerAVR:testConfigurableIPDefault [ ipAddress="192.168.1.25", tcpPort=8102 ]
pioneeravr:ConfigurablePioneerAVR:testConfigurableIPTweak [ ipAddress="192.168.1.25", tcpPort=8102 nbZones=2, setVolumeCommandEnabled=false, burstMessageDelay=50, setBurstModeEnabled=true ]
```


## Channels

For each zone (replace X by the zone number):
* **zoneX#power**: power On/Off the AVR. Receive power events.
* **zoneX#volumeDimmer**: Increase/Decrease the volume on the AVR or set the volume as %. Receive volume change events (in %).  
* **zoneX#volumeDb**: Set the volume of the AVR in dB (from -80.0 to 12 with 0.5 dB steps). Receive volume change events (in dB).
* **zoneX#mute**: Mute/Unmute the AVR. Receive mute events.
* **zoneX#setInputSource**: Set the input source of the AVR. See input source mapping for more details. Receive source input change events with the input source ID.

For an AVR:
* **displayInformation**: Receive display events. Reflect the display on the AVR front panel.


## Input Source Mapping

Here after are the ID values of the input sources:

* 04: DVD
* 25: BD
* 05: TV/SAT
* 15: DVR/BDR
* 10: VIDEO 1(VIDEO)
* 14: VIDEO 2
* 19: HDMI 1
* 20: HDMI 2
* 21: HDMI 3
* 22: HDMI 4
* 23: HDMI 5
* 26: HOME MEDIA GALLERY(Internet Radio)
* 17: iPod/USB
* 18: XM RADIO
* 01: CD
* 03: CD-R/TAPE
* 02: TUNER
* 00: PHONO
* 12: MULTI CH IN
* 33: ADAPTER PORT
* 27: SIRIUS
* 31: HDMI (cyclic)


## Full example

* demo.Things:

```
pioneeravr:VSX-921:vsx921 [ ipAddress="192.168.1.25" ]
```

* demo.items:

```
/* Pioneer AVR Items */
Switch vsx921PowerSwitch		"Power"								(All)	{ channel="pioneeravr:VSX-921:vsx921:zone1#power" }
Switch vsx921MuteSwitch			"Mute"					<none>		(All)	{ channel="pioneeravr:VSX-921:vsx921:zone1#mute" }
Dimmer vsx921VolumeDimmer		"Volume [%.1f] %"		<none>		(All)	{ channel="pioneeravr:VSX-921:vsx921:zone1#volumeDimmer" }
Number vsx921VolumeNumber		"Volume [%.1f] dB"		<none>		(All)	{ channel="pioneeravr:VSX-921:vsx921:zone1#volumeDb" }
String vsx921InputSourceSet		"Input"					<none>		(All)	{ channel="pioneeravr:VSX-921:vsx921:zone1#setInputSource" }
String vsx921InformationDisplay "Information [%s]"		<none> 		(All)	{ channel="pioneeravr:VSX-921:vsx921:displayInformation" }
```

* demo.sitemap:

```
sitemap demo label="Main Menu"
{
	Frame label="Pioneer AVR" {
		Switch item=vsx921PowerSwitch
		Switch item=vsx921MuteSwitch mappings=[ON="Mute", OFF="Un-Mute"] 
		Slider item=vsx921VolumeDimmer
		Setpoint item=vsx921VolumeNumber minValue=-80 maxValue=12 step=0.5
		Switch item=vsx921InputSourceSet mappings=[04="DVD", 15="DVR/BDR", 25="BD"]
		Text item=vsx921InformationDisplay
	}
}
```



