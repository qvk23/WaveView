package gst.trainingcourse.sampleproject

import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.seekBar
import kotlinx.android.synthetic.main.activity_main.wv_progress

class MainActivity : AppCompatActivity() {

    private val borderColor: Int = Color.parseColor("#44FFFFFF")
    private var borderWidth = 10
    private var value = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wv_progress.setBorder(borderWidth, borderColor)
        seekBar.apply {
            max = 100
            wv_progress.run {
                setProgressValue(progress / 100f)
                setTextTitle(progress.toString())
            }
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    wv_progress.setTextTitle(progress.toString())
                    wv_progress.setProgressValue(progress / 100f)
                    value = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    wv_progress.setProgressValue(value / 100f)
                }
            })
        }
    }
}