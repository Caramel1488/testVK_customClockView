package vk.test.customclock

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class FragmentDynamically : Fragment(R.layout.fragment_dynamical) {

    private lateinit var clockView: CustomAnalogClockView
    private lateinit var sizeSeekBar: SeekBar
    private lateinit var buttonChangeColor: Button
    private lateinit var buttonXML: Button
    private var changeColor: Boolean = false
    private var slider: Double? = null

    private val standartColorList = listOf(
        R.color.baseColor,
        R.color.textColor,
        R.color.frameColor,
        R.color.dotsColor,
        R.color.hourHandColor,
        R.color.minuteHandColor,
        R.color.secondHandColor
    )

    private val newColorList = listOf(
        R.color.newBaseColor,
        R.color.newTextColor,
        R.color.newFrameColor,
        R.color.newDotsColor,
        R.color.newHourHandColor,
        R.color.newMinuteHandColor,
        R.color.newSecondHandColor
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        changeColor = savedInstanceState?.getBoolean(CHANGE_COLOR_KEY) ?: false

        slider = savedInstanceState?.getDouble(SLIDER_KEY)?:1.0

        getViews()

        listeners()

        observe()

        setColors(standartColorList)
    }

    private fun listeners() {
        buttonXML.setOnClickListener {
            findNavController().navigate(FragmentDynamicallyDirections.actionFragmentDynamicallyToFragmentXML())
        }

        sizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p1 != 0) {
                    clockView.setRadius(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
                slider = sizeSeekBar.progress.toDouble()/sizeSeekBar.max
            }
        })

        buttonChangeColor.setOnClickListener {
            changeColor = !changeColor
            if (changeColor) {
                setColors(newColorList)
            } else {
                setColors(standartColorList)
            }

        }
    }

    private fun observe() {
        requireView().viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                requireView().viewTreeObserver.removeOnGlobalLayoutListener(this)
                requireContext()
                val min = if (requireView().width < requireView().height) {
                    requireView().width / 2
                } else {
                    requireView().height / 4
                }

                val maxRadius = min - min / 24 - 48
                sizeSeekBar.max = maxRadius
                sizeSeekBar.progress = (maxRadius* slider!!).toInt()
            }
        })
    }

    private fun getViews() {
        clockView = requireView().findViewById(R.id.clockViewDynam)
        sizeSeekBar = requireView().findViewById(R.id.sizeSeekBar)
        buttonChangeColor = requireView().findViewById(R.id.buttonChangeColor)
        buttonXML = requireView().findViewById(R.id.buttonXML)
    }

    private fun setColors(colors: List<Int?>) {
        if (colors[0] != null) clockView.setBaseColor(
            ContextCompat.getColor(
                requireContext(),
                colors[0]!!
            )
        )
        if (colors[1] != null) clockView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                colors[1]!!
            )
        )
        if (colors[2] != null) clockView.setFrameColor(
            ContextCompat.getColor(
                requireContext(),
                colors[2]!!
            )
        )
        if (colors[3] != null) clockView.setDotsColor(
            ContextCompat.getColor(
                requireContext(),
                colors[3]!!
            )
        )
        if (colors[4] != null) clockView.setHourHandColor(
            ContextCompat.getColor(
                requireContext(),
                colors[4]!!
            )
        )
        if (colors[5] != null) clockView.setMinHandColor(
            ContextCompat.getColor(
                requireContext(),
                colors[5]!!
            )
        )
        if (colors[6] != null) clockView.setSecHandColor(
            ContextCompat.getColor(
                requireContext(),
                colors[6]!!
            )
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(CHANGE_COLOR_KEY, changeColor)
        outState.putDouble(SLIDER_KEY, slider!!)
    }

    companion object {
        private const val CHANGE_COLOR_KEY = "CHANGE_COLOR"
        private const val SLIDER_KEY = "SLIDER"
    }
}