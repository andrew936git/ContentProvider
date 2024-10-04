package com.example.contentprovider

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentTransaction
import com.example.contentprovider.databinding.FragmentSendSMSBinding


class SendSMSFragment : Fragment() {
    private var _binding: FragmentSendSMSBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendSMSBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ObsoleteSdkInt")
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

        val contact = arguments?.getParcelable<Contact>("contact")
        val phoneNumber = contact?.phone
        binding.numberTV.text = phoneNumber
        binding.sendSMS.setOnClickListener{
            if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
                permissionContact.launch(Manifest.permission.READ_PHONE_STATE)

            }
            else if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionContact.launch(Manifest.permission.SEND_SMS)
            }else{
                try {
                    val text = binding.editSMS.text.toString()

                    val smsManager: SmsManager = if (Build.VERSION.SDK_INT>=23) {
                        requireContext().getSystemService(SmsManager::class.java)
                    } else{
                        SmsManager.getDefault()
                    }
                    smsManager.sendTextMessage(phoneNumber, null, text, null, null)

                    Toast.makeText(requireContext(), "Message Sent", Toast.LENGTH_LONG).show()
                    binding.editSMS.text.clear()

                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Please enter all the data.." + e.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }
    }

    private val permissionContact = registerForActivityResult(
        ActivityResultContracts.RequestPermission())
    { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Разрешение получено", Toast.LENGTH_SHORT).show()
        }
        else Toast.makeText(context, "В разрешении отказано...", Toast.LENGTH_SHORT).show()
    }

}