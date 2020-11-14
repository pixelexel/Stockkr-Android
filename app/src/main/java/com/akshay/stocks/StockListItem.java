package com.akshay.stocks;

public class StockListItem {
    private String mTicker;
    private String mName;
    private String mShares;
    private double mLast;
    private double mChange;

    public StockListItem(String name, String ticker) {
        mTicker = ticker;
        mName = name;
    }

    public String getTicker(){
        return mTicker;
    }

    public String getName(){
        return mName;
    }

}
