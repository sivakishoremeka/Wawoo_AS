package com.wawoo.data;

import com.google.gson.annotations.Expose;

public class PaytermPaymentdatum {

    @Expose
    private Integer id;
    @Expose
    private String chargeType;
    @Expose
    private Integer chargeDuration;
    @Expose
    private String contractType;
    @Expose
    private Double price;
    @Expose
    private Integer contractDuration;
    @Expose
    private Double finalAmount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getChargeType() {
        return chargeType;
    }


    public void setChargeType(String chargeType) {
        this.chargeType = chargeType;
    }

    public Integer getChargeDuration() {
        return chargeDuration;
    }


    public void setChargeDuration(Integer chargeDuration) {
        this.chargeDuration = chargeDuration;
    }


    public String getContractType() {
        return contractType;
    }


    public void setContractType(String contractType) {
        this.contractType = contractType;
    }


    public Double getPrice() {
        return price;
    }


    public void setPrice(Double price) {
        this.price = price;
    }


    public Integer getContractDuration() {
        return contractDuration;
    }


    public void setContractDuration(Integer contractDuration) {
        this.contractDuration = contractDuration;
    }


    public Double getFinalAmount() {
        return finalAmount;
    }


    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

}
