package com.akshay.stocks;

public class StockItem {
    private String mTicker;
    private String mName;
    private String mShares;
    private double mLast;
    private double mChange;

    public StockItem(String name, String ticker, Double last, Double change) {
        mTicker = ticker;
        mName = name;
        mLast = last;
        mChange = change;
    }
    public Double getLast() {
        return mLast;
    }

    public String getTicker(){
        return mTicker;
    }

    public String getName(){
        return mName;
    }

    public Double getChange(){return mChange;}

}
