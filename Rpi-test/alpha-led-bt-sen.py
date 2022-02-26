# reference: https://learn.adafruit.com/circuitpython-on-raspberrypi-linux/digital-i-o
#            starter code provided in client.py by Roy
#            https://www.hivemq.com/blog/mqtt-client-library-paho-python/

# imports
import time
import board
import digitalio
import adafruit_ahtx0
import paho.mqtt.client as paho
from paho import mqtt

# global variables
interval = 2
led_stat = "off"
previousTime = 0
enFlag = False

# Create sensor object, communicating over the board's default I2C bus
i2c = board.I2C()                               # uses board.SCL and board.SDA
sensor = adafruit_ahtx0.AHTx0(i2c)              # creating a sesnor object for the ATH sensor

# setting up object for LED
led = digitalio.DigitalInOut(board.D18)         # creating an LED object for GPIO pin 18
led.direction = digitalio.Direction.OUTPUT      # setting the direction for the pin as Output
led.value = False                               # setting the defult of the LED as off

# setting up object for Button
button = digitalio.DigitalInOut(board.D4)       # creating a button object for GPIO pin 4
button.direction = digitalio.Direction.INPUT    # setting the direction for the pin as Input
button.pull = digitalio.Pull.UP                 # enabling internel pull-up resistor for the button pin

# setting callbacks for different events to see if it works, print the message etc.
def on_connect(client, userdata, flags, rc, properties=None):
    print("CONNACK received with code %s." % rc)
    global enFlag
    #print(str(rc))
    if str(rc) == "Success":
        enFlag = True

# with this callback you can see if your publish was successful
def on_publish(client, userdata, mid, properties=None):
    print("mid: " + str(mid))

# print which topic was subscribed to
def on_subscribe(client, userdata, mid, granted_qos, properties=None):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))

# On reciving any message from the a topic
def on_message(client, userdata, msg):
    global led_stat
    global interval
    print(msg.topic + " " + str(msg.qos) + " " + str(msg.payload))
    t = str(msg.topic) 
    # check the topic
    if t == "abhijRoom1/LED":

        led_stat = str(msg.payload, encoding="UTF_8")
        
        if led_stat == "on":
            led.value = True
        elif led_stat == "off":
            led.value = False
        else:
            print("unknown command")

    elif t == "abhijRoom1/Interval":
        interval = int(msg.payload)

# using MQTT version 5 here
# userdata is user defined data of any type, updated by user_data_set()
# client_id is the given name of the client
client = paho.Client(client_id="", userdata=None, protocol=paho.MQTTv5)
client.on_connect = on_connect

# enable TLS for secure connection
client.tls_set(tls_version=mqtt.client.ssl.PROTOCOL_TLS)
# set username and password
client.username_pw_set("abhij8prem", "Hiveroy101")
# connect to HiveMQ Cloud on port 8883 (default for MQTT)
client.connect("1f32c3fb6c9b410ea7551bcd5307890c.s2.eu.hivemq.cloud", 8883)

# setting callbacks, use separate functions like above for better visibility
client.on_subscribe = on_subscribe
client.on_message = on_message
client.on_publish = on_publish

# subscribe to the incoming topics from the application
client.subscribe("abhijRoom1/LED", qos=1)           
client.subscribe("abhijRoom1/Interval", qos=1)

# starting the loop manually
client.loop_start()

while True:

    currentTime = time.time()                       # reading the current 
    #print("enFlag: {}".format(enFlag))
    if currentTime-previousTime >= interval and enFlag == True:        # checking if the time is elapsed to publish
        previousTime = currentTime       

        # read the sensor values
        temperature = sensor.temperature
        humidity = sensor.relative_humidity

        # publishing the humidity and temperature readings
        client.publish("abhijRoom1/temperature", payload = temperature, qos=1)
        print("\nPublished on abhijRoom1/temperature : %f C" % temperature)
        client.publish("abhijRoom1/humidity", payload = humidity, qos=1)
        print("\nPublished on abhijRoom1/Humidity: %0.1f %%" % humidity)
        client.publish("abhijRoom1/lightSwitch", payload = not button.value, qos=1)
        print("\nPublished on abhijRoom1/lightSwitch: {} " .format(not button.value))

#client.loop_stop()