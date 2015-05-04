/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.examples.datagenerator.events;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class PurchaseEvent implements Event{

    private final long purchaseDate;
    private final String user;
    private final float totalAmount;
    private final String city;
    private final String postalCode;
    private final String state;
    private final String country;
    private final String customerType;
    private final String paymentType;

    public PurchaseEvent(long purchaseDate, String user, float totalAmount, String city, String postalCode, String state, String country, String customerType, String paymentType) {
        this.purchaseDate = purchaseDate;
        this.user = user;
        this.totalAmount = totalAmount;
        this.city = city;
        this.postalCode = postalCode;
        this.state = state;
        this.country = country;
        this.customerType = customerType;
        this.paymentType = paymentType;
    }

    public String toJsonOutput(){

        Gson gson = new Gson();
        JsonObject eventObject = new JsonObject();
        eventObject.addProperty("purchaseDate", purchaseDate);
        eventObject.addProperty("user", user);
        eventObject.addProperty("totalAmount", totalAmount);
        eventObject.addProperty("city", city);
        eventObject.addProperty("postalCode", postalCode);
        eventObject.addProperty("state", state);
        eventObject.addProperty("country", country);
        eventObject.addProperty("customerType", customerType);
        eventObject.addProperty("paymentType", paymentType);
        return gson.toJson(eventObject);
    }

    public static PurchaseEvent getInstance(Object[] args) {
        long purchaseDate = Long.parseLong(args[0].toString());
        String user = args[1].toString();
        float totalAmount = Float.parseFloat(args[2].toString());
        String city = args[3].toString();
        String postalCode = args[4].toString();
        String state = args[5].toString();
        String country = args[6].toString();
        String customerType = args[7].toString();
        String paymentType = args[8].toString();

        return new PurchaseEvent(purchaseDate,
                user,
                totalAmount,
                city,
                postalCode,
                state,
                country,
                customerType,
                paymentType);
    }

    public long getPurchaseDate() {
        return purchaseDate;
    }

    public String getUser() {
        return user;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getCustomerType() {
        return customerType;
    }

    public String getPaymentType() {
        return paymentType;
    }
}
