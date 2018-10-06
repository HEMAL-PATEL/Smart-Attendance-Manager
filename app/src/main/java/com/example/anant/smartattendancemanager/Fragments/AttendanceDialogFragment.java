package com.example.anant.smartattendancemanager.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.anant.smartattendancemanager.R;

public class AttendanceDialogFragment extends DialogFragment {

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(int classAttended, int totalClasses);
    }

    NoticeDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement NoticeDialogListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_attendance, null);
        final EditText attendedEditText = v.findViewById(R.id.class_attended);
        final EditText conductedEditText = v.findViewById(R.id.class_conducted);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(v)
                // Add action buttons
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (attendedEditText.getText().toString().length() > 0
                                && conductedEditText.getText().toString().length() > 0) {
                            int classAtteded = Integer.parseInt(attendedEditText.getText().toString());
                            int totalClasses = Integer.parseInt(conductedEditText.getText().toString());
                            // Send the positive button event back to the host activity
                            mListener.onDialogPositiveClick(classAtteded, totalClasses);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Close the Dialog and do nothing
                        AttendanceDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();

    }
}
