package com.cdel.stream.purchases.model;

/**
 * User: Bill Bejeck
 * Date: 2/20/16
 * Time: 9:55 AM
 */
public class RewardAccumulator {

    private String customerName;
    private double purchaseTotal;

    private RewardAccumulator(String customerName, double purchaseTotal) {
        this.customerName = customerName;
        this.purchaseTotal = purchaseTotal;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getPurchaseTotal() {
        return purchaseTotal;
    }

    @Override
    public String toString() {
        return "RewardAccumulator{" +
                "customerName='" + customerName + '\'' +
                ", purchaseTotal=" + purchaseTotal +
                '}';
    }

    public static Builder builder(Purchase purchase){return new Builder(purchase);}

    public static final class Builder {
        private String customerName;
        private double purchaseTotal;

        private Builder(Purchase purchase){
           this.customerName = purchase.getLastName()+","+purchase.getFirstName();
           this.purchaseTotal = purchase.getPrice() * purchase.getQuantity();
        }


        public RewardAccumulator build(){
            return new RewardAccumulator(customerName,purchaseTotal);
        }

    }
}