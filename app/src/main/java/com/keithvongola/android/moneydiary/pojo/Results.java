package com.keithvongola.android.moneydiary.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Results {
        @SerializedName("rate")
        @Expose
        private List<Rate> rate = null;

        public List<Rate> getRate() {
            return rate;
        }

        public void setRate(List<Rate> rate) {
        this.rate = rate;
    }

}
