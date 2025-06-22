package com.s23010162.safewalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EmergencyContactsAdapter extends RecyclerView.Adapter<EmergencyContactsAdapter.ContactViewHolder> {

    private List<EmergencyContact> contacts;
    private final OnContactDeleteListener deleteListener;

    public interface OnContactDeleteListener {
        void onDelete(EmergencyContact contact);
    }

    public EmergencyContactsAdapter(List<EmergencyContact> contacts, OnContactDeleteListener deleteListener) {
        this.contacts = contacts;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact contact = contacts.get(position);
        holder.tvName.setText(contact.name);
        holder.tvRelationship.setText(contact.relationship);
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(contact));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void setContacts(List<EmergencyContact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvRelationship;
        ImageButton btnDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvContactName);
            tvRelationship = itemView.findViewById(R.id.tvContactRelationship);
            btnDelete = itemView.findViewById(R.id.btnDeleteContact);
        }
    }
} 