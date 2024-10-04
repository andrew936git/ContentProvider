package com.example.contentprovider

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class CustomAdapter(private val context: Context, private var contacts: MutableList<Contact>, private val listener: NoteClickListener):
    RecyclerView.Adapter<CustomAdapter.ContactViewHolder>(){

    inner class ContactViewHolder(itemView: View):
        RecyclerView.ViewHolder(itemView) {
        private val nameTV: TextView = itemView.findViewById(R.id.nameTV)
        private val phoneNumberTV: TextView = itemView.findViewById(R.id.phoneNumberTV)
        val callIV: ImageView = itemView.findViewById(R.id.callIV)
        val messageIV: ImageView = itemView.findViewById(R.id.messageIV)

        fun bind(contact: Contact) {
            nameTV.text = contact.name
            phoneNumberTV.text = contact.phone
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Contact>){
        contacts.clear()
        contacts.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val viewHolder = ContactViewHolder(LayoutInflater
            .from(context)
            .inflate(R.layout.list_item, parent, false))
        viewHolder.callIV.setOnClickListener {
            listener.onCallClicked(contacts[viewHolder.adapterPosition])
        }
        viewHolder.messageIV.setOnClickListener {
            listener.onMessageClick(contacts[viewHolder.adapterPosition])
        }
        return viewHolder
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
    }

    interface NoteClickListener{
        fun onCallClicked(contact: Contact)
        fun onMessageClick(contact: Contact)
    }
}