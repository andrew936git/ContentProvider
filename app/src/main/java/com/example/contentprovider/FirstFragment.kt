package com.example.contentprovider

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.contentprovider.databinding.FragmentFirstBinding
import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager

class FirstFragment : Fragment(), CustomAdapter.NoteClickListener {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private var contactList = mutableListOf<Contact>()
    private var adapter: CustomAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED){
            permissionContact.launch(Manifest.permission.READ_CONTACTS)
        }
        else getContact()
    }

    @SuppressLint("Recycle", "Range", "NotifyDataSetChanged")
    private fun getContact(){
        val phones = requireContext().contentResolver?.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        while (phones!!.moveToNext()){
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val contact = Contact(name, phoneNumber)
            contactList.add(contact)
        }
        phones.close()
        adapter = CustomAdapter(requireContext(), contactList, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }


    private fun callTheNumber(contact: Contact){
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:${contact.phone}")
        startActivity(intent)
    }

    private val permissionContact = registerForActivityResult(
        ActivityResultContracts.RequestPermission())
    { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Разрешение получено", Toast.LENGTH_SHORT).show()
            getContact()
        }
        else Toast.makeText(context, "В разрешении отказано...", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCallClicked(contact: Contact) {
        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED){
            permissionContact.launch(Manifest.permission.CALL_PHONE)
            adapter?.notifyDataSetChanged()
        }
        else callTheNumber(contact)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onMessageClick(contact: Contact) {

        val bundle = Bundle()
        bundle.putSerializable("contact", contact)
        val sendSMSFragment = SendSMSFragment()
        sendSMSFragment.arguments = bundle
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(R.id.fragment_container, sendSMSFragment)
        transaction?.addToBackStack(null)
        transaction?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction?.commit()


    }
}

