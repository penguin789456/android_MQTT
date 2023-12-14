package com.example.myapplication

import android.content.Context
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var btn:Button
    private lateinit var showText1:TextView
    private val Topic="mhn"
    private val myQos=1
    private val tag="TAG"
    private lateinit var mqttClient:MqttAndroidClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn=findViewById<Button>(R.id.button)
        showText1=findViewById<TextView>(R.id.editTextTextPersonName)

        connect(this)

        btn.setOnClickListener{
            if (mqttClient.isConnected){
                var myMsg=showText1.text.toString()
                publish(Topic,1,false,myMsg)
            }else{
                Log.d("sent MSG","connect not ready")
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }

    private fun disconnect() {
        if (mqttClient.isConnected){
            try {
                mqttClient.disconnect(null,object:IMqttActionListener{
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d("disconnect","disconnect")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d("disconnect","failed to disconnect")
                    }

                })
            }catch (ex:IOException){
                Log.d("disconnect","${ex.printStackTrace()}")
            }
        }

    }

    private fun connect(context:Context) {
        val URL="tcp://broker.emqx.io:1883"
        val userName="testUser"
        val passWD="123456"
        mqttClient = MqttAndroidClient(context,URL,"location")
        mqttClient.setCallback(object :MqttCallback{
            override fun connectionLost(cause: Throwable?) {

                Log.d("connection","ERROR:Lost")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {

                Log.d("connection","SER:$topic,MEG:$message")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

                Log.d("connection","connection fin")
            }
        })
        val options=MqttConnectOptions()
        options.userName=userName
        options.password=passWD.toCharArray()

        try {
            mqttClient.connect(options,null,object:IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(tag,"post success")
                    subscribe(Topic,myQos)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(tag,"Connection failure\n ${exception?.printStackTrace()}")
                }
            })
        }catch (ex:MqttException){
            Log.d("connection error", ex.printStackTrace().toString())
        }
    }

    private fun subscribe(topic:String,myQos:Int) {
        try {
            mqttClient.subscribe(topic,myQos,null,object:IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(tag,"topic:$topic success")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(tag,"topic:$topic fail")
                }
            })
        }catch (ex:IOException){
            Log.d(tag,"$ex")
        }
    }
    fun publish(topic: String,Qos:Int,retain:Boolean,MSG:String){
        try {
            val message=MqttMessage()
            message.payload=MSG.toByteArray()
            message.qos=Qos
            message.isRetained=retain

            mqttClient.publish(topic,message,null,object : IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(tag,"publish success msg:$MSG topic:$topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(tag,"publish topic:$topic MSG:$MSG error:${exception}")
                }

            })
        }catch (ex:MqttException){
            Log.d(tag,"error ${ex.printStackTrace()}")
        }
    }
}