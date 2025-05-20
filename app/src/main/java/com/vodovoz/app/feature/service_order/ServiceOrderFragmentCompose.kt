package com.vodovoz.app.feature.service_order

import androidx.fragment.app.Fragment import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ServiceOrderFragment : Fragment() {

    private val viewModel: ServiceOrderViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchData()
    }


}
