package com.example.bustracker

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils.indexOf
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val button = findViewById<Button>(R.id.button)
        val busList = findViewById<Spinner>(R.id.busList)
        val textView = findViewById<TextView>(R.id.textResult)


        //populate busList using API call
        //gets JSON object for bus list
        val getRoutesUrl =
            "http://ctabustracker.com/bustime/api/v2/getroutes?key=KcP2F9GisX8YZ8dZ7fXPwDzP7&format=json"
        var busArray = arrayListOf<String>("Select ")
        val routesRequest = JsonObjectRequest(
            Request.Method.GET, getRoutesUrl, null,
            Response.Listener { response ->
                //converts JSON object to arraylist

                val routesJsonArray = response.getJSONObject("bustime-response").getJSONArray("routes")
                for (i in 0 until routesJsonArray.length()) {
                    val r = routesJsonArray.getJSONObject(i)
                    busArray.add(r.getString("rt") + " - " + r.getString("rtnm"))
                }
            },
            Response.ErrorListener {textView.text = "Network error :("}//handle error
        )
        MySingleton.getInstance(this).addToRequestQueue(routesRequest)
        //populate spinner
        val dataAdapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.spinner_item, busArray)
        busList.adapter = dataAdapter
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        busList.onItemSelectedListener = this

        val dirList = findViewById<Spinner>(R.id.directionList)
        dirList.onItemSelectedListener = this

        val stopList = findViewById<Spinner>(R.id.stopList)
        stopList.onItemSelectedListener = this

        //Get results with button
        button.setOnClickListener {
            try{
                val rtFull = busList.selectedItem.toString()
                val rt = rtFull.substring(0, (rtFull.indexOf(' ')))
                val stpFull = stopList.selectedItem.toString()
                val stpid = stpFull.substring(stpFull.lastIndexOf('(')+1, stpFull.lastIndexOf(')'))
                val predUrl =
                    "http://www.ctabustracker.com/bustime/api/v2/getpredictions?key=KcP2F9GisX8YZ8dZ7fXPwDzP7&rt=$rt&stpid=$stpid&format=json"
                val predRequest = JsonObjectRequest(Request.Method.GET, predUrl, null,
                    Response.Listener { response ->
                        //converts JSON object to arraylist

                        try{
                            val predTime = response.getJSONObject("bustime-response").getJSONArray("prd").getJSONObject(0).getString("prdctdn")
                            if (predTime == "DUE"){
                                textView.text = "Your bus is due now!"
                            }
                            else{
                                textView.text = "Your bus will arrive in $predTime minutes."
                            }
                        }
                        catch(e: Exception){
                            textView.text = "Bus route is out of service."
                        }
                    },
                    Response.ErrorListener {textView.text = "Network error :("}//handle error
                )
                MySingleton.getInstance(this).addToRequestQueue(predRequest)
            }
            catch(e: Exception){textView.text = "Make sure to fill all selections."}
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}//nothing

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        if(parent.id == R.id.busList){
            val rtFull = parent.getItemAtPosition(position).toString()
            val rt = rtFull.substring(0, (rtFull.indexOf(' ')))
            val directionUrl =
                "http://www.ctabustracker.com/bustime/api/v2/getdirections?key=KcP2F9GisX8YZ8dZ7fXPwDzP7&rt=" + rt + "&format=json"
            val dirArray = arrayListOf<String>("Select ")
            val dirRequest = JsonObjectRequest(Request.Method.GET, directionUrl, null,
                Response.Listener { response ->
                    //converts JSON object to arraylist

                    //val textView = findViewById<TextView>(R.id.textResult)
                    //textView.text = response.toString()
                    try{
                        val dirJsonArray = response.getJSONObject("bustime-response").getJSONArray("directions")                    //for (i in 0 until 2) {
                        dirArray.add(dirJsonArray.getJSONObject(0).getString("dir"))
                        dirArray.add(dirJsonArray.getJSONObject(1).getString("dir"))
                    }
                    catch(e: Exception){
                        //do nothing
                    }
                },
                Response.ErrorListener {}//handle error
            )
            MySingleton.getInstance(this).addToRequestQueue(dirRequest)
            val dirList = findViewById<Spinner>(R.id.directionList)
            val dataAdapter2: ArrayAdapter<String> = ArrayAdapter(this, R.layout.spinner_item, dirArray)
            dirList.adapter = dataAdapter2
            dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        else if(parent.id == R.id.directionList){
            val dirList = findViewById<Spinner>(R.id.directionList)
            val dir = dirList.selectedItem

            val rtFull = busList.selectedItem.toString()
            val rt = rtFull.substring(0, (rtFull.indexOf(' ')))

            val stopListUrl =
                "http://www.ctabustracker.com/bustime/api/v2/getstops?key=KcP2F9GisX8YZ8dZ7fXPwDzP7&rt=$rt&dir=$dir&format=json"
            var stopArray = arrayListOf<String>("Select ")
            val stopRequest = JsonObjectRequest(
                Request.Method.GET, stopListUrl, null,
                Response.Listener { response ->
                    //converts JSON object to arraylist
                    try{
                        val stopsJsonArray = response.getJSONObject("bustime-response").getJSONArray("stops")
                        for (i in 0 until stopsJsonArray.length()) {
                            val r = stopsJsonArray.getJSONObject(i)
                            stopArray.add(r.getString("stpnm") + " (" + r.getString("stpid") + ")")
                        }
                    }
                    catch (e: Exception){}
                },
                Response.ErrorListener {}//handle error
            )
            MySingleton.getInstance(this).addToRequestQueue(stopRequest)
            val dataAdapter3: ArrayAdapter<String> = ArrayAdapter(this, R.layout.spinner_item, stopArray)
            stopList.adapter = dataAdapter3
            dataAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

    }
}//comment to test git
