package com.cdel.stream.purchases.processes;
import org.apache.kafka.streams.processor.AbstractProcessor;

import com.cdel.stream.purchases.model.Purchase;

/**
 * User: Bill Bejeck
 * Date: 2/20/16
 * Time: 9:19 AM
 */
public class CreditCardAnonymizer extends AbstractProcessor<String, Purchase> {

    private static final String CC_NUMBER_REPLACEMENT="xxxx-xxxx-xxxx-";

    @Override
    public void process(String key, Purchase purchase) {
          String last4Digits = purchase.getCreditCardNumber().split("-")[3];
          Purchase updated = Purchase.builder(purchase).creditCardNumber(CC_NUMBER_REPLACEMENT+last4Digits).build();
          context().forward(key,updated);
          context().commit();
    }
}