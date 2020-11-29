package com.akshay.stocks;

public class StockItem {
    private String mTicker;
    private String mName;
    private String mShares;
    private double mLast;
    private double mChange;

    public StockItem(String name, String ticker, Double last) {
        mTicker = ticker;
        mName = name;
        mLast = last;
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

}
