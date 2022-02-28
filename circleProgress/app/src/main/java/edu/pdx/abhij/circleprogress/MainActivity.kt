/**************************************************************************************************
ECE558_22W Project2: Android MQTT Client app in Kotlin to interface with a cloud broker
Here, we are using HiveMQ's free Broker service to host our topics for the project

Basic overview:
The Project is to write an Android-based control app communicating via MQTT with a RaspberryPi connected
to sensors and simple electronic components. We can either host the MQTT server on the Rpi or use a
cloud broker service like HiveMQ. Both the Rpi and Android app are MQTT clients.

Android side Requirement:

The MQTT client on the Android will publish topics for the following:
    - interval (user-entered value in the Android app)
    - LED (the user can choose to turn LED on or off by pressing a button on the Android app)
The MQTT client on the Android will subscribe to the following topics:
    - Button Status (“pressed” or “not pressed”)
    - Temperature
    - Humidity

Author: Abhijeet Prem

 References:
    https://medium.com/swlh/android-and-mqtt-a-simple-guide-cb0cbba1931c
    https://medium.com/@chaitanya.bhojwani1012/eclipse-paho-mqtt-android-client-using-kotlin-56129ff5fbe7
    https://www.hivemq.com/blog/mqtt-client-library-enyclopedia-paho-android-service/
    https://www.emqx.com/en/blog/android-connects-mqtt-using-kotlin
    https://www.youtube.com/watch?v=NpURY3zE8o8&ab_channel=anoop4Real
    https://www.youtube.com/watch?v=ghck5NILaiI
***************************************************************************************************/

package edu.pdx.abhij.circleprogress

//imports
import android.annotation.SuppressLint
import android.content.ContentValues.*
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import org.eclipse.paho.android.service.MqttAndroidClient
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.ramotion.fluidslider.FluidSlider
import nl.bryanderidder.themedtogglebuttongroup.ThemedButton
import org.eclipse.paho.client.mqttv3.*

// contant global variables used in the program
const val serverURI  = "tcp://broker.hivemq.com:1883"
const val username = ""
const val pwd = ""
const val topicLight ="abhijRoom1/LED"
const val topicTemperature = "abhijRoom1/temperature"
const val topicInterval = "abhijRoom1/Interval"
const val topicHumidity = "abhijRoom1/Humidity"
const val topicSwitchState = "abhijRoom1/lightSwitch"

// Mqtt Client class, has all the necessary function to interact with MQTT
class MQTTClient(context: Context?,
                 serverURI: String,
                 clientID: String = "") {

    private var mqttClient = MqttAndroidClient(context, serverURI, clientID)   // creating an object for MQTTAndroid client

    //defining default objects
    private val defaultCbConnect = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(this.javaClass.name, "(Default) Connection success")
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.d(this.javaClass.name, "Connection failure: ${exception.toString()}")
        }
    }

    private val defaultCbClient = object : MqttCallback {
        override fun messageArrived(topic: String?, message: MqttMessage?) {
            Log.d(this.javaClass.name, "Receive message: ${message.toString()} from topic: $topic")
        }

        override fun connectionLost(cause: Throwable?) {
            Log.d(this.javaClass.name, "Connection lost ${cause.toString()}")
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            Log.d(this.javaClass.name, "Delivery completed")
        }
    }

    private val defaultCbSubscribe = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(this.javaClass.name, "Subscribed to topic")
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.d(this.javaClass.name, "Failed to subscribe topic")
        }
    }

    private val defaultCbUnsubscribe = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(this.javaClass.name, "Unsubscribed to topic")
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.d(this.javaClass.name, "Failed to unsubscribe topic")
        }
    }
    private val defaultCbPublish = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(this.javaClass.name, "Message published to topic")
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.d(this.javaClass.name, "Failed to publish message to topic")
        }
    }
    private val defaultCbDisconnect = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(this.javaClass.name, "Disconnected")
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.d(this.javaClass.name, "Failed to disconnect")
        }
    }

    // function to connect to a MQTT broker
    fun connect(username:   String               = "",
                password:   String               = "",
                cbConnect:  IMqttActionListener  = defaultCbConnect,
                cbClient:   MqttCallback         = defaultCbClient) {
        mqttClient.setCallback(cbClient)
        val options = MqttConnectOptions()
        options.userName = username
        options.password = password.toCharArray()

        try {
            mqttClient.connect(options, null, cbConnect)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    // Function to subscribe to a topic
    fun subscribe(topic:        String,
                  qos:          Int                 = 1,
                  cbSubscribe:  IMqttActionListener = defaultCbSubscribe) {
        if(mqttClient.isConnected()){
        try {
            mqttClient.subscribe(topic, qos, null, cbSubscribe)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
        }
    }

    // function to unsubscribe from a topic
    fun unsubscribe(topic:          String,
                    cbUnsubscribe:  IMqttActionListener = defaultCbUnsubscribe) {
        try {
            mqttClient.unsubscribe(topic, null, cbUnsubscribe)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    // function to publish to a topic
    fun publish(topic:      String,
                msg:        String,
                qos:        Int                 = 1,
                retained:   Boolean             = false,
                cbPublish:  IMqttActionListener = defaultCbPublish) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, cbPublish)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

//---> function to disconnect form the Broker
    fun disconnect(cbDisconnect: IMqttActionListener = defaultCbDisconnect ) {
        try {
            mqttClient.disconnect(null, cbDisconnect)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}
//>>>>>> end of MQTTClient class

@Suppress("PrivatePropertyName")
class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    // creating private variables for the class MainActivity
    private var temperature: Float = 0.0f                   // variable to store the incoming temperature value
    private var humidity: Float = 0.0f                      // variable to store the incoming humidity value
    private lateinit var mqttClient : MQTTClient            // creating a object for the MQTTClient, that will be initialized later
    private lateinit var mqttClientID: String               // creating a variable mqttClientID, that will be initialized later

    override fun onCreate(savedInstanceState: Bundle?) {    // function that will be execute when the app is created
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setUpSlider()                                       // setting up slider initial settings
        // open mQTT Broker communication
        mqttClientID = MqttClient.generateClientId()        // generating a random Client Id
        mqttClient = MQTTClient(this@MainActivity, serverURI, mqttClientID)

        mqttClient.connect(                                 // function to connect to the
            username,
            pwd,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) { // if connection was successful execute the rest
                    Log.d(TAG, "Connection success")
                    Toast.makeText(this@MainActivity, "MQTT Connection success", Toast.LENGTH_SHORT).show()
                    // Subscribing to all the three topics from other system
                    mqttClient.subscribe(topicTemperature)
                    mqttClient.subscribe(topicHumidity)
                    mqttClient.subscribe(topicSwitchState)
                    setUpButton()                           // calling setup button function
                    setUpCircleProgress()                   // calling Circle progress function

                }
                // error handler code if unable to connect
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(this.javaClass.name, "Connection failure: ${exception.toString()}")

                    Toast.makeText(this@MainActivity, "MQTT Connection fails: ${exception.toString()}",
                        Toast.LENGTH_SHORT).show()
                }
            },
            object : MqttCallback {                                                         // call back function when messages are updated in remote topics
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val msg = "Receive message: ${message.toString()} from topic: $topic"   // message to log is created for debugging
                    Log.d(TAG, msg)
                    //Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()     // uncomment to display the incoming message in the app as tost view

                    // update the global variables base on the incoming topic
                    if (topic == topicTemperature){
                        temperature = (message.toString().toFloat())
                        setUpCircleProgress()
                    }
                    else if (topic == topicHumidity){
                        humidity = (message.toString().toFloat())
                        setUpCircleProgress()
                    }
                    else if (topic == topicSwitchState){
                        updateSwitchStatus(message.toString())
                    }
                }
                // error handling code, if connection is lost in between
                override fun connectionLost(cause: Throwable?) {
                    Log.d(TAG, "Connection lost ${cause.toString()}")
                }
                // logging if delivery is completed
                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(TAG, "Delivery complete")
                }
            })  // end of connect function
        }

    // application functions
    @SuppressLint("SetTextI18n")
    private fun setUpSlider() {
        val slider = findViewById<FluidSlider>(R.id.fluidSlider)
        val text = findViewById<TextView>(R.id.intervalTV)

        val max = 60    // 1 min
        val min = 1     // 1 sec
        val total = max - min
// initializing the slider position and other visible features
        slider.position = .1f%total
        slider.startText ="$min"
        slider.endText = "$max"
        slider.positionListener = {
                pos -> text.text = "Interval: "+(min +(total * pos)).toInt().toString()+" Sec" // update the text view based on the slider position
                slider.bubbleText = (min +(total * pos)).toInt().toString()                 // update the bubble text based on the position
        }

        slider.beginTrackingListener = {
            // do something when tracking
            // maybe play some tunes! ^_^
        }

        slider.endTrackingListener = {

            // call the publish function to publish the updated interval value to the topic
            mqttClient.publish(topicInterval,slider.bubbleText.toString(),1,true) // on ending tracking slider push that value to the topic
        }
    }

    // function for displaying
    @SuppressLint("SetTextI18n")
    private fun setUpCircleProgress(){

        val humidityProgressBar = findViewById<CircularProgressBar>(R.id.humidityProgressBar)
        val temperatureProgressBar = findViewById<CircularProgressBar>(R.id.temperatureProgressBar)
        val text1 = findViewById<TextView>(R.id.temperatureTV)
        val text2 = findViewById<TextView>(R.id.humidityTV)
        val temp = (((temperature*(9f/5f)+32f)*10f).toInt()).toFloat()/10f // converting temperature to Fahrenheit

        // setting circularProgressBar 1
        temperatureProgressBar.apply {
            progressMax = 120f                                          // max temperate on scale is 120 °F
            setProgressWithAnimation(temp, 1000)                // animation function for the widget
            progressBarWidth = 10f
            backgroundProgressBarWidth = 10f
            backgroundProgressBarColor = Color.LTGRAY                   // setting background to light gray
            roundBorder = true
            startAngle = 180f
            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT
            text1.text = "$temp°F"
        }

        // setting circularProgressBar 1
        humidityProgressBar.apply {
            progressMax = 100f                                         // here the humidity is shown in % so 100 is the max
            setProgressWithAnimation(humidity, 1000)
            progressBarWidth = 10f
            backgroundProgressBarWidth = 10f
            backgroundProgressBarColor = Color.LTGRAY
            roundBorder = true
            startAngle = 180f
            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT
            val hum = (((humidity*10).toInt()).toFloat())/10         // rounding to 2 decimal palaces
            text2.text = "$hum%"
        }
    }

    private fun setUpButton(){

        val lightButton = findViewById<ThemedButton>(R.id.btn1)
        var btn = false                                          // default button state if OFF
        lightButton.bgColor = Color.RED                          // setting the default background color as red
        mqttClient.publish(topicLight,"off",1,true)          // setting the default as off and value is retained

        //selection listener
        lightButton.setOnClickListener() {
             btn = !btn                                         // toggling the button state
            if (btn) {
                lightButton.bgColor = Color.GREEN
                // publish led on
                mqttClient.publish(topicLight,"on")
            }
            else {
                lightButton.bgColor = Color.RED
                //publish led off
                mqttClient.publish(topicLight,"off")
            }
        }
    }
    // function to update the status of the switch that's remote
    private fun updateSwitchStatus(status: String){
        val button = findViewById<ThemedButton>(R.id.btn2)

        if(status =="False"){
            button.text = "Button is up"        // it means that the button is not pressed
            button.bgColor = Color.GRAY         // set the background color to gray
        }
        else if (status =="True"){
            button.text = "Button is down"
            button.bgColor = Color.GREEN
        }

    }
}