# ece558-project2

## MQTT based IoT Android project

The objective is to write an Android-based control app communicating via MQTT with a
RaspberryPi connected to sensors and simple electronic components. An MQTT server is
hosted on your RaspberryPi, and both the Android and the RaspberryPi are MQTT clients. All
the messages are sent over WiFi using the MQTT messaging protocol.

### Key points relevant to the project:

•	In this project I used HiveMQ’s broker service to host the server. 

•	For hardware, I have used a Raspberry PI4 and wrote the client script in python using the paho mqtt client libraries.

•	Had set up the Rpi as a headless devise and used MobaXterm to SSH to the device, it made programming smoother, as I was able to use my favorite ide (Visual Studio code) on my host mashing to write the code.

•	Nothing fancy with the seniors, the AHT20 sensor, an LED and a pushbutton all integrated on a breadboard and wired-up using jumper wires to the pi
