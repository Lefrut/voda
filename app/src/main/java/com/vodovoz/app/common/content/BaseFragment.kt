package com.vodovoz.app.common.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.databinding.FragmentBaseFlowBinding

abstract class BaseFragment : Fragment() {

    private var _viewBinding: FragmentBaseFlowBinding? = null
    private val viewBinding
        get() = _viewBinding!!

    protected val contentView: View
        get() = requireView().findViewById(R.id.container_base)


    protected abstract fun layout(): Int
    protected open fun update() {}
    protected open fun initView() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _viewBinding = FragmentBaseFlowBinding.inflate(inflater, container, false)

        viewBinding.containerBase.runCatching {
            layoutResource = layout()
            val inflate = inflate()
            inflate.id = viewBinding.containerBase.id
            inflate
        }

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    protected fun showLoader() {
    }

    protected fun hideLoader() {
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }



    protected fun initToolbar(
        titleText: String,
        showSearch: Boolean = false,
        showNavBtn: Boolean = true,
        addAction: Boolean = false,
        provider: MenuProvider? = null,
        doAfterTextChanged: (query: String) -> Unit = {},
    ) {
        with(viewBinding.appBarDef) {
            if (addAction) {
                tbToolbar.overflowIcon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_more_actions)
            }
            if (provider != null) {
                tbToolbar.addMenuProvider(provider)
            }
            viewBinding.appbarLayout.isVisible = true
            imgSearch.visibility = if(showSearch) View.VISIBLE else View.INVISIBLE
            if (showNavBtn) {
                imgBack.isVisible = true
                tvTitle.setPadding(0, 0, 0, 0)
            } else {
                imgBack.isVisible = false
                tvTitle.setPadding(50, 0, 0, 0)
            }
            root.isVisible = true
            tvTitle.text = titleText

            imgBack.setOnClickListener { findNavController().popBackStack() }

            imgSearch.setOnClickListener {
                llTitleContainer.visibility = View.GONE
                llSearchContainer.visibility = View.VISIBLE
            }
            imgClear.setOnClickListener {
                etSearch.setText("")
                llTitleContainer.visibility = View.VISIBLE
                llSearchContainer.visibility = View.GONE
            }
            etSearch.doAfterTextChanged { query ->
                when (query.toString().isEmpty()) {
                    true -> imgClear.visibility = View.GONE
                    false -> imgClear.visibility = View.VISIBLE
                }

                doAfterTextChanged.invoke(query.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (viewBinding.appBarDef.etSearch.text.isNullOrBlank().not()) {
            viewBinding.appBarDef.llTitleContainer.visibility = View.GONE
            viewBinding.appBarDef.llSearchContainer.visibility = View.VISIBLE
        }
    }
}