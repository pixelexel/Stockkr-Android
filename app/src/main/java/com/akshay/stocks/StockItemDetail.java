package com.akshay.stocks;

public class StockItemDetail {
    private String ticker;
    private String name;
    private double last;

    public StockItemDetail(String name, String ticker, double last) {
        this.ticker = ticker;
        this.name = name;
        this.last = last;
    }

    public Double getLast() {
        return last;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }
}
