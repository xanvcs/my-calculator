package com.example.mycalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.lang.ArithmeticException

class MainActivity : AppCompatActivity() {

    private var tvInput: TextView? = null
    var lastNumeric : Boolean = false
    var lastDot : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvInput = findViewById(R.id.tvInput)
    }

    fun onDigit(view: View) {
        tvInput?.append((view as Button).text)
        lastNumeric = true
        lastDot = false


    }

    fun onClear(view: View) {
        tvInput?.text = ""
    }

    fun onDecimalPoint(view: View){
        if(lastNumeric && !lastDot){
            tvInput?.append(".")
            lastNumeric = false
            lastDot = true
        }
    }

    fun onOperator(view: View){
        tvInput?.text?.let{

            if(lastNumeric && !isOperatorAdded(it.toString())) {
                tvInput?.append((view as Button).text)
                lastNumeric = false
                lastDot = false
            }
        }

    }

    fun onEqual(view: View) {
        if(lastNumeric) {
            var tvValue = tvInput?.text.toString()
            var prefix = ""
            try{
                if(tvValue.startsWith("-")){
                    prefix = "-"
                    tvValue = tvValue.substring(1)
                }
                if(tvValue.contains("-")) {
                    val splitValue = tvValue.split("-")

                    var one = splitValue[0] // 99
                    var two = splitValue[1] // 1

                    if(prefix.isNotEmpty()){
                        one = prefix + one
                    }

                    tvInput?.text = removeZeroAfterDot((one.toDouble() - two.toDouble()).toString())
                }else if(tvValue.contains("+")) {
                    val splitValue = tvValue.split("+")

                    var one = splitValue[0] // 99
                    var two = splitValue[1] // 1

                    if(prefix.isNotEmpty()){
                        one = prefix + one
                    }

                    tvInput?.text = removeZeroAfterDot((one.toDouble() + two.toDouble()).toString())
                }else if(tvValue.contains("/")) {
                    val splitValue = tvValue.split("/")

                    var one = splitValue[0] // 99
                    var two = splitValue[1] // 1

                    if(prefix.isNotEmpty()){
                        one = prefix + one
                    }

                    tvInput?.text = removeZeroAfterDot((one.toDouble() / two.toDouble()).toString())
                }else if(tvValue.contains("*")) {
                    val splitValue = tvValue.split("*")

                    var one = splitValue[0] // 99
                    var two = splitValue[1] // 1

                    if(prefix.isNotEmpty()){
                        one = prefix + one
                    }

                    tvInput?.text = removeZeroAfterDot((one.toDouble() * two.toDouble()).toString())
                }


            }catch (e: ArithmeticException){
                e.printStackTrace()
            }
        }
    }

    private fun removeZeroAfterDot(result: String) : String {
        var value = result
        if(result.contains(".0"))
            value = result.substring(0, result.length - 2)

        return value
    }

    private fun isOperatorAdded(value : String) : Boolean {
        return if(value.startsWith("-")) {
            false
        }else{
            value.contains("/")
                    || value.contains("*")
                    || value.contains("+")
                    || value.contains("-")
        }
    }

    companion object {
        private var trajectories: HashMap<String, Trajectory>? = null
        private val trajectoriesLock = ReentrantLock()

        fun generateTrajectories() {

            val genTrajectoryThread = Thread {
                if (trajectories == null) {
                    println("INFO: Trajectories loading...")
                    trajectoriesLock.lock()

                    trajectories = HashMap<String, Trajectory>()

                    val pathNames = ArrayList<String>()

                    val deployDirectory = Paths.get(Filesystem.getDeployDirectory().toString(), "PathWeaver/Paths")
                    val listOfFiles = deployDirectory.toFile().listFiles()

//                    for (file in listOfFiles) {
//                        pathNames.add("/" + file.name)
//                        // No filter is needed for now since onl files in deploy directory are path files.
//                    }

                    for (file in listOfFiles) {
                        // System.out.println(String.format("Adding Pathname: %s", pathName));
                        val trajPack = TrajectoryPacket.generateTrajectoryPacket(file)

                        val trajectory = TrajectoryGenerator.generateTrajectory(
                            Pose2d(trajPack.firstX, trajPack.firstY, Rotation2d.fromDegrees(trajPack.startAngle)),
                            trajPack.pathRead,
                            Pose2d(trajPack.lastX, trajPack.lastY, Rotation2d.fromDegrees(trajPack.endAngle)),
                            TrajectoryConfig(7.0, 7.0).setReversed(trajPack.reversed)
//                            TrajectoryConfig(3.0, 3.0).setReversed(trajPack.reversed)
//                            TrajectoryConfig(2.0, 4.0)
                        )

                        trajectories!![file.name] = trajectory
                    }
                    trajectoriesLock.unlock()
                    println("INFO: Trajectories loaded")


                }
            }

            genTrajectoryThread.isDaemon = true
            genTrajectoryThread.start()
        }

        fun getTrajectories(): HashMap<String, Trajectory>? {
            var curTrajectories: HashMap<String, Trajectory>? = null

            if (trajectories != null) {
                println("INFO: trajectories is not null")
            }

            if (trajectories != null && trajectoriesLock.tryLock()) {
                curTrajectories = trajectories
                trajectoriesLock.unlock()
            }

            return curTrajectories
        }
    }


}