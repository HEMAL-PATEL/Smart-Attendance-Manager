package com.application.anant.smartattendancemanager.Presenter;

import android.app.Activity;
import android.support.v4.content.res.ResourcesCompat;
import android.text.InputType;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.application.anant.smartattendancemanager.Model.SubjectsModel;
import com.application.anant.smartattendancemanager.R;
import com.application.anant.smartattendancemanager.View.AddSubjectsView;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

public class MainActivityPresenter implements SubjectsModel.OnDataSaved {

    private int editTextID = 1;
    private Activity activity;
    private AddSubjectsView addSubjectsView;
    private SubjectsModel subjectsModel;

    private int count = 0;

    public MainActivityPresenter(Activity activity, String UID, AddSubjectsView addSubjectsView,
                                 SubjectsModel subjectsModel) {
        this.activity = activity;
        this.addSubjectsView = addSubjectsView;
        this.subjectsModel = subjectsModel;
    }

    public void saveData(DatabaseReference mDatabase) {
        HashMap<String, String> subjects = new HashMap();
        for (int i = 1; i <= editTextID; i++) {
            EditText editText = activity.findViewById(i);
            try {
                String subject = editText.getText().toString();
                if (subject != null && subject.length() > 0)
                    subjects.put(subject, "0/0");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (subjects.size() == 0) {
            Toast.makeText(activity, R.string.add_least_subject, Toast.LENGTH_LONG).show();
            return;
        } else {
            subjectsModel.saveSubjectsMain(this::onSaved, subjects, mDatabase);
        }
    }

    public void createEditTextView(LinearLayout linearLayout) {
        try {
            activity.findViewById(editTextID - 1)
                    .setOnTouchListener(null);
        } catch (NullPointerException e) {
            //Do nothing
        }
        EditText editText = new EditText(activity);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, activity.getResources().getDisplayMetrics());
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, activity.getResources().getDisplayMetrics());
        editText.setId(editTextID);
        editText.setBackgroundResource(R.drawable.edit_text_border);
        editText.setHint(R.string.edit_text_hint);
        editText.setPadding(padding, padding, padding, padding);
        editText.setTypeface(ResourcesCompat.getFont(activity, R.font.product_sans));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
        lp.setMargins(padding, padding, padding, padding);
        editText.setLayoutParams(lp);
        linearLayout.addView(editText);
        editTextID++;
        activity.findViewById(editTextID - 1)
                .setOnTouchListener((v, event) -> {
                    createEditTextView(linearLayout);
                    return false;
                });
        if (count < 1) {
            count++;
            createEditTextView(linearLayout);
        }
    }

    @Override
    public void onSaved(boolean isSaved) {
        if (isSaved) {
            addSubjectsView.onDataSaved();
        } else
            Toast.makeText(activity, "Subjects name must not contain '/', '.', '#', '$', '[', or ']'",
                    Toast.LENGTH_SHORT).show();
    }
}
