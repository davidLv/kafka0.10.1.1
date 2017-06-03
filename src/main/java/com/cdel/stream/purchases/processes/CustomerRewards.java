package com.cdel.stream.purchases.processes;

import org.apache.kafka.streams.processor.AbstractProcessor;

import com.cdel.stream.purchases.model.Purchase;
import com.cdel.stream.purchases.model.RewardAccumulator;

/**
 * User: Bill Bejeck
 * Date: 2/20/16
 * Time: 9:44 AM
 */
public class CustomerRewards extends AbstractProcessor<String,Purchase> {

    @Override
    public void process(String key, Purchase value) {
        RewardAccumulator accumulator = RewardAccumulator.builder(value).build();
        context().forward(key,accumulator);
        context().commit();

    }
}