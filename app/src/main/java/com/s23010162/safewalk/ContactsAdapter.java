package com.s23010162.safewalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private List<EmergencyContact> contactList;

    public ContactsAdapter(List<EmergencyContact> contactList) {
        this.contactList = contactList;
    }

    public void setContacts(List<EmergencyContact> contacts) {
        this.contactList = contacts;
        notifyDataSetChanged(); // In a real app, use DiffUtil for better performance
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact contact = contactList.get(position);
        holder.tvContactName.setText(contact.name);
        holder.tvContactPhone.setText(contact.phoneNumber);
        holder.tvContactRelation.setText(contact.relationship);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactName, tvContactPhone, tvContactRelation;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactPhone = itemView.findViewById(R.id.tvContactPhone);
            tvContactRelation = itemView.findViewById(R.id.tvContactRelation);
        }
    }
} 