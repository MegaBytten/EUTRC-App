package org.megabytten.exetertouchapp.utils;

import android.content.Context;

import org.megabytten.exetertouchapp.utils.Training;

import java.util.ArrayList;

public class CustomTextView extends androidx.appcompat.widget.AppCompatTextView {

    private ArrayList<Training> trainings = new ArrayList<>();

    //TextView specific data
    private boolean isClicked;
    //Training-associated data
    private int id;

    public CustomTextView(Context context) {
        super(context);
    }


    public void addTraining(Training training){
        trainings.add(training);
    }

    public void removeTraining(Training training){
        trainings.remove(training);
    }

    public ArrayList<Training> getTrainings() {
        return trainings;
    }

    public void setTrainings(ArrayList<Training> trainings) {
        this.trainings = trainings;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

}
