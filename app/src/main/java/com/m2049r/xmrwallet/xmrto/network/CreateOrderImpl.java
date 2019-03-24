/*
 * Copyright (c) 2017 m2049r et al.
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

package com.m2049r.xmrwallet.xmrto.network;

import android.support.annotation.NonNull;

import com.m2049r.xmrwallet.xmrto.api.XmrToCallback;
import com.m2049r.xmrwallet.xmrto.api.CreateOrder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class CreateOrderImpl implements CreateOrder {

    private final String state;
    private final double btcAmount;
    private final String btcDestAddress;
    private final String uuid;

    public Double getBtcAmount() {
        return btcAmount;
    }

    public String getBtcDestAddress() {
        return btcDestAddress;
    }

    public String getUuid() {
        return uuid;
    }

    public String getState() {
        return state;
    }

    CreateOrderImpl(final JSONObject jsonObject) throws JSONException {
        this.state = jsonObject.getString("state");
        this.btcAmount = jsonObject.getDouble("btc_amount");
        this.btcDestAddress = jsonObject.getString("btc_dest_address");
        this.uuid = jsonObject.getString("uuid");
    }

    public static void call(@NonNull final XmrToApiCall api, final double amount, @NonNull final String address,
                            @NonNull final XmrToCallback<CreateOrder> callback) {
        try {
            final JSONObject request = createRequest(amount, address);
            api.call("order_create", request, new NetworkCallback() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    try {
                        callback.onSuccess(new CreateOrderImpl(jsonObject));
                    } catch (JSONException ex) {
                        callback.onError(ex);
                    }
                }

                @Override
                public void onSuccess(JSONArray jsonObject) {

                }

                @Override
                public void onError(Exception ex) {
                    callback.onError(ex);
                }
            });
        } catch (JSONException ex) {
            callback.onError(ex);
        }
    }

    static JSONObject createRequest(final double amount, final String address) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("btc_amount", amount);
        jsonObject.put("btc_dest_address", address);
        return jsonObject;
    }


}
