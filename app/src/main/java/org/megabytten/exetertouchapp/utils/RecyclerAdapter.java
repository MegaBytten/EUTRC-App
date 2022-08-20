package org.megabytten.exetertouchapp.utils;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import org.megabytten.exetertouchapp.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>{

    //sortedList and adapter interactions
    private Comparator<Training> dateComparator = (o1, o2) -> { //compares training.year, .month, and .day to order them
        if (o1.getDateYear() == o2.getDateYear()){

            if (o1.getDateMonth() == o2.getDateMonth()){ //year, month both same. Classify according to day

                if (o1.getDateDay() > o2.getDateDay()){
                    return 1;
                } else if (o1.getDateDay() < o2.getDateDay()){
                    return -1;
                } else {
                    return 0;
                }

            } else { //year is same, but month is different. Classify according to month

                if (o1.getDateMonth() > o2.getDateMonth()){
                    return 1;
                } else if (o1.getDateMonth() < o2.getDateMonth()){
                    return -1;
                } else {
                    return 0;
                }
            }


        } else { //year is different, then can classify according to year

            if (o1.getDateYear() > o2.getDateYear()){
                return 1;
            } else if (o1.getDateYear() < o2.getDateYear()){
                return -1;
            } else {
                return 0;
            }
        }
    };

    private final SortedList.Callback<Training> mCallback = new SortedList.Callback<Training>() {

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public int compare(Training o1, Training o2) { //this uses the predefined dateComparator to sort trainigns by date.
            return dateComparator.compare(o1, o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(Training oldItem, Training newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(Training item1, Training item2) {
            return item1.getId() == item2.getId();
        }
    };
    SortedList<Training> sortedList = new SortedList<>(Training.class, mCallback);

    //other variables
    private ArrayList<Training> trainingsList;

    private TextView selectedTrainingTitleTxt;
    private String selectedTrainingId;

    //constructor
    public RecyclerAdapter(ArrayList<Training> trainingsList){
        this.trainingsList = trainingsList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        //define all views from deletetrainings_item.xml here
        private TextView trainingTitleTxt, id, date, team, location, drills;
        private LinearLayout linLay;

        public MyViewHolder(final View view){
            super(view);
            trainingTitleTxt = view.findViewById(R.id.trainingTitleTxt);
            id = view.findViewById(R.id.id);
            date = view.findViewById(R.id.date);
            team = view.findViewById(R.id.team);
            location = view.findViewById(R.id.location);
            drills = view.findViewById(R.id.drills);
            linLay = view.findViewById(R.id.linLay);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.deletetrainings_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.MyViewHolder holder, int position) {
        //change views by accessing them through: 'holder.viewName'
        Training tr = sortedList.get(position);
        holder.trainingTitleTxt.setText("Training #" + tr.getId());
        holder.id.setText(String.valueOf(tr.getId()));
        holder.date.setText(tr.getFormattedDate());
        holder.location.setText(tr.getLocation());
        holder.team.setText(tr.getTeam());
        holder.drills.setText(tr.getDrills());



        holder.linLay.setOnClickListener(v -> {
            if (selectedTrainingTitleTxt != null){
                selectedTrainingTitleTxt.setTextColor(Color.BLACK);
            }
            selectedTrainingTitleTxt = holder.trainingTitleTxt;
            selectedTrainingTitleTxt.setTextColor(Color.RED);
            selectedTrainingId = holder.id.getText().toString();

            Toast.makeText(v.getContext(), "Training #" + tr.getId() + " selected.", Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public int getItemCount() {
        return sortedList.size();
    }

    /*
    ################################################################################################
    ####################### Adding/Removing Data from RecyclerView #################################
    ####### - Uses sortedList to add/remove items correctly (keeps training in same order) #########
    ################################################################################################
    ################################################################################################
     */

    public void addItem(Training tr){
        sortedList.add(tr);
    }

    public void addItems(ArrayList<Training> trs){
        sortedList.addAll(trs);
    }

    public void removeItem(Training tr){
        sortedList.remove(tr);
    }

    public void removeItems(ArrayList<Training> trs){
        sortedList.beginBatchedUpdates();
        for (Training tr : trs) {
            sortedList.remove(tr);
        }
        sortedList.endBatchedUpdates();
    }

    //this method accepts a list of trainings, removes trainings from sortedList if they are not found in trs. Then all trs are added.
    public void replaceAll(ArrayList<Training> trs) {
        sortedList.beginBatchedUpdates();

        for (int i = sortedList.size() - 1; i >= 0; i--) {
            Training tr = sortedList.get(i);
            boolean matched = false;

            for (Training filteredTr: trs){
                if (filteredTr.getId() == tr.getId()) matched = true;
            }

            if (!matched){
                sortedList.remove(tr);
            }
        }
        sortedList.addAll(trs);
        sortedList.endBatchedUpdates();
    }

    public TextView getSelectedTrainingTitleTxt() {
        return selectedTrainingTitleTxt;
    }

    public String getSelectedTrainingId() {
        return selectedTrainingId;
    }
}
