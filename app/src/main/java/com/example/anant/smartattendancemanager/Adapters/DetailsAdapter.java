package com.example.anant.smartattendancemanager.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anant.smartattendancemanager.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.MyViewHolder> {

    private final ArrayList<String> subjectDataset;
    private Map<String, Object> attendanceMap;

    private OnItemClickListener onItemClickListener;

    private int[] drawables = {R.drawable.textview_design_green,
            R.drawable.textview_design_blue, R.drawable.textview_design_orange,
            R.drawable.textview_design_red};
    private float percentage;
    private Context context;
    private FirebaseAuth mAuth;
    private String UID;
    private DatabaseReference ref;

    public interface OnItemClickListener {
        void onItemClick(String key);

        void onAttendanceMarked();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView subject_textView;
        private TextView percentageTextView;
        private TextView bunkTextView;
        private TextView attendanceTextView;
        private ImageView attendanceMark;
        private ImageView attendanceUnmark;

        public MyViewHolder(View view) {
            super(view);
            subject_textView = view.findViewById(R.id.subject_text_view);
            attendanceTextView = view.findViewById(R.id.attendance_text_view);
            bunkTextView = view.findViewById(R.id.leave_text_view);
            percentageTextView = view.findViewById(R.id.percentage_text_view);
            percentageTextView.setOnClickListener(this);
            attendanceMark = view.findViewById(R.id.attendance_mark);
            attendanceUnmark = view.findViewById(R.id.attendance_unmark);
            attendanceMark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String key = subjectDataset.get(getAdapterPosition());
                    String classes = attendanceMap.get(key).toString();
                    int attended = Integer.parseInt(classes.substring(0, classes.indexOf("/")));
                    int noOfClasses = Integer.parseInt(classes.substring(classes.indexOf("/") + 1, classes.length()));
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, (++attended) + "/" + (++noOfClasses));
                    ref.updateChildren(result);
                    onItemClickListener.onAttendanceMarked();
                }
            });
            attendanceUnmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String key = subjectDataset.get(getAdapterPosition());
                    String classes = attendanceMap.get(key).toString();
                    int attended = Integer.parseInt(classes.substring(0, classes.indexOf("/")));
                    int noOfClasses = Integer.parseInt(classes.substring(classes.indexOf("/") + 1, classes.length()));
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, (attended) + "/" + (++noOfClasses));
                    ref.updateChildren(result);
                    onItemClickListener.onAttendanceMarked();
                }
            });

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            onItemClickListener.onItemClick(subjectDataset.get(position));
        }
    }

    public DetailsAdapter(Map<String, Object> map, OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        attendanceMap = map;
        subjectDataset = new ArrayList<>(map.keySet());
        getUID();
    }

    private void getUID() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UID = user.getUid();
        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference("/users/" + UID + "/subjects");
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DetailsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.attendance_card, parent, false);

        context = parent.getContext();

        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String subject = subjectDataset.get(position);
        holder.subject_textView.setText(subject.substring(0, 1).toUpperCase()
                + subject.substring(1).toLowerCase());
        String classes = attendanceMap.get(subject).toString();
        int attended = Integer.parseInt(classes.substring(0, classes.indexOf("/")));
        int noOfClasses = Integer.parseInt(classes.substring(classes.indexOf("/") + 1, classes.length()));
        if (noOfClasses != 0)
            percentage = ((float) attended) / noOfClasses * 100;
        else percentage = 0;
        holder.percentageTextView.setText(Integer.toString(Math.round(percentage)));
        holder.percentageTextView.setBackgroundResource(getDrawable());
        holder.attendanceTextView.setText("Attendance: " + attended + "/" + noOfClasses);
        int i = 0;
        while (percentage >= 75) {
            noOfClasses++;
            percentage = ((float) attended) / noOfClasses * 100;
            i++;
        }
        holder.bunkTextView.setText(String.format(context.getString(R.string.leave_class), ((i == 0) ? 0 : --i)));
    }

    private int getDrawable() {
        int percentageInt = Math.round(percentage);
        int drawable = 0;
        if (percentageInt >= 90) {
            drawable = drawables[0];
        } else if (percentageInt >= 75 && percentageInt < 90) {
            drawable = drawables[1];
        } else if (percentageInt >= 70 && percentageInt < 75) {
            drawable = drawables[2];
        } else if (percentageInt <= 70) {
            drawable = drawables[3];
        }
        return drawable;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return subjectDataset.size();
    }
}

