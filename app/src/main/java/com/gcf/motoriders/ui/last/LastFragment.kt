package com.gcf.motoriders.ui.last

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gcf.motoriders.R


class LastFragment : Fragment()  {

    companion object {
        fun newInstance() = LastFragment()
    }

    private lateinit var viewModel: LastViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this).get(LastViewModel::class.java)
        return inflater.inflate(R.layout.last_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LastViewModel::class.java)
        // TODO: Use the ViewModel
    }
}