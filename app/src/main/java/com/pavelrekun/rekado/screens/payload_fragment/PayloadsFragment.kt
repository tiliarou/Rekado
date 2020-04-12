package com.pavelrekun.rekado.screens.payload_fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pavelrekun.penza.services.extensions.tintContrast
import com.pavelrekun.rekado.R
import com.pavelrekun.rekado.base.BaseFragment
import com.pavelrekun.rekado.databinding.FragmentPayloadsBinding
import com.pavelrekun.rekado.services.Constants
import com.pavelrekun.rekado.services.Events
import com.pavelrekun.rekado.services.utils.LoginUtils
import com.pavelrekun.rekado.services.dialogs.DialogsShower
import com.pavelrekun.rekado.services.extensions.viewBinding
import com.pavelrekun.rekado.services.payloads.PayloadDownloadHelper
import com.pavelrekun.rekado.services.payloads.PayloadHelper
import com.pavelrekun.rekado.services.utils.MemoryUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PayloadsFragment : BaseFragment(R.layout.fragment_payloads) {

    private val binding by viewBinding(FragmentPayloadsBinding::bind)
    private val viewModel by lazy { ViewModelProvider(this).get(PayloadsViewModel::class.java) }

    private lateinit var adapter: PayloadsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initWithTitle(R.string.navigation_payloads)
        initObservers()
        initList()
        initClickListeners()
        initDesign()
        initRefreshListener()
    }

    private fun initObservers() {
        viewModel.isProgressShowing.observe(viewLifecycleOwner, Observer { isShowing ->
            if (isShowing) showProgress() else hideProgress()
        })

        viewModel.fetchSchemaResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                PayloadDownloadHelper.Result.SUCCESS -> {
                    DialogsShower.showPayloadsUpdatesDialog(getBaseActivity(), result.schema, viewModel)
                }
                else -> {
                }
            }
        })

        viewModel.updatePayloadResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                PayloadDownloadHelper.Result.SUCCESS -> {
                    updateList()
                }
                else -> {
                }
            }
        })

        viewModel.downloadPayloadResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                PayloadDownloadHelper.Result.SUCCESS -> {
                    updateList()
                    LoginUtils.info("Payload downloaded successfully.")
                    Toast.makeText(getBaseActivity(), getString(R.string.payloads_download_status_success), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(getBaseActivity(), getString(R.string.payloads_download_status_error), Toast.LENGTH_SHORT).show()
                    LoginUtils.error("Failed to download payload.")
                }
            }
        })
    }

    private fun initList() {
        MemoryUtils.parseBundledSchema()

        adapter = PayloadsAdapter(PayloadHelper.getAllPayloads())

        binding.payloadsList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = this@PayloadsFragment.adapter
        }
    }

    private fun initDesign() {
        binding.payloadsAddUrl.tintContrast()
        binding.payloadsAdd.tintContrast()
    }

    private fun updateList() {
        if (this::adapter.isInitialized) {
            adapter.updateList()
        }
    }

    private fun initClickListeners() {
        binding.payloadsAddUrl.setOnClickListener { DialogsShower.showPayloadsDownloadDialog(getBaseActivity(), viewModel) }
        binding.payloadsAdd.setOnClickListener { addPayload() }
    }

    private fun addPayload() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = Constants.MIME_BINARY
        intent.putExtra(Intent.EXTRA_TITLE, R.string.dialog_file_chooser_payload_title)
        startActivityForResult(intent, Constants.KEY_OPEN_PAYLOAD)
    }

    private fun showProgress() {
        if (!binding.payloadsLayoutRefresh.isRefreshing) {
            binding.payloadsLayoutRefresh.isRefreshing = true
        }
    }

    private fun hideProgress() {
        if (binding.payloadsLayoutRefresh.isRefreshing) {
            binding.payloadsLayoutRefresh.isRefreshing = false
        }
    }

    private fun initRefreshListener() {
        binding.payloadsLayoutRefresh.setOnRefreshListener {
            viewModel.fetchExternalSchema()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Events.UpdatePayloadsListEvent) {
        updateList()
    }

    override fun onResume() {
        super.onResume()

        if (this::adapter.isInitialized) {
            initList()
        }

        viewModel.fetchExternalSchema()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

}