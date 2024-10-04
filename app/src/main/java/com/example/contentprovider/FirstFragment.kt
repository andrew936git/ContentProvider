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
import android.content.ContentProviderOperation
import android.content.pm.PackageManager
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.RawContacts
import android.util.Log
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager

class FirstFragment : Fragment(), CustomAdapter.NoteClickListener {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private var contactList = arrayListOf<Contact>()
    private var adapter: CustomAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            setTitle("Мои контакты")
        }

        binding.exitIV.setOnClickListener {
            requireActivity().finishAffinity()
        }

        binding.searchIV.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelableArrayList("list", contactList)
            val searchFragment = SearchFragment()
            val transaction = fragmentManager?.beginTransaction()
            searchFragment.arguments = bundle
            transaction?.replace(R.id.fragment_container, searchFragment)
            transaction?.addToBackStack(null)
            transaction?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction?.commit()
        }

        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED){
            permissionContact.launch(Manifest.permission.READ_CONTACTS)
            adapter!!.notifyDataSetChanged()
        }
        else getContact()

        binding.saveBT.setOnClickListener {

            if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED){
                permissionContact.launch(Manifest.permission.WRITE_CONTACTS)
            }
            else {
                addContact()
                adapter!!.notifyDataSetChanged()
                contactList.clear()
                getContact()
                binding.nameET.text.clear()
                binding.phoneET.text.clear()
            }

        }

    }


    private fun addContact() {
        val name = binding.nameET.text.toString()
        val phone = binding.phoneET.text.toString()
        val listCPO = ArrayList<ContentProviderOperation>()

        listCPO.add(
            ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        listCPO.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, name)
                .build()
            )

        listCPO.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, phone)
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                .build()
        )

        Toast.makeText(context, "Контакт добавлен", Toast.LENGTH_SHORT).show()
        try {
            requireContext().contentResolver.applyBatch(ContactsContract.AUTHORITY, listCPO)
        }catch (e: Exception){
            Log.e("Exception", e.message!!)
        }
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

