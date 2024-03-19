package vk.test.customclock

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class FragmentXML: Fragment(R.layout.fragment_xml) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.buttonDynam).setOnClickListener {
            findNavController().navigate(FragmentXMLDirections.actionFragmentXMLToFragmentDynamically())
        }
    }
}