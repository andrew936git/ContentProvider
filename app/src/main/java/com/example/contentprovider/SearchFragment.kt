package com.example.contentprovider

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contentprovider.databinding.FragmentSearchBinding


class SearchFragment : Fragment(), CustomAdapter.NoteClickListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            setTitle("Мои контакты")
            setNavigationIcon(R.drawable.ic_home)
            setNavigationOnClickListener {
                val transaction = fragmentManager?.beginTransaction()
                transaction?.replace(R.id.fragment_container, FirstFragment())
                transaction?.addToBackStack(null)
                transaction?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                transaction?.commit()
            }
        }
        val list = arguments?.getParcelableArrayList<Contact>("list")

        binding.searchBT.setOnClickListener {
            val text = binding.searchET.text.toString()
            val searchList = arrayListOf<Contact>()
            if (list != null) {
                for (i in list){
                    if((i.name.contains(text, ignoreCase = true)) || (i.phone.contains(text)))
                        searchList.add(i)
                }
            }
            val adapter = CustomAdapter(requireContext(), searchList, this)
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

    }

    override fun onCallClicked(contact: Contact) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:${contact.phone}")
        startActivity(intent)
    }

    override fun onMessageClick(contact: Contact) {
        val bundle = Bundle()
        bundle.putParcelable("contact", contact)
        val sendSMSFragment = SendSMSFragment()
        sendSMSFragment.arguments = bundle
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(R.id.fragment_container, sendSMSFragment)
        transaction?.addToBackStack(null)
        transaction?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction?.commit()
    }


}