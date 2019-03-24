/*
 * Copyright (c) 2017 m2049r
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

package com.m2049r.xmrwallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.m2049r.xmrwallet.layout.CandidateInfoAdapter;
import com.m2049r.xmrwallet.model.CandidateInfo;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.exchange.api.ExchangeApi;
import com.m2049r.xmrwallet.service.exchange.api.ExchangeCallback;
import com.m2049r.xmrwallet.service.exchange.api.ExchangeRate;
import com.m2049r.xmrwallet.util.Helper;
import com.m2049r.xmrwallet.util.OkHttpHelper;
import com.m2049r.xmrwallet.widget.Toolbar;
import com.m2049r.xmrwallet.xmrto.api.VotingChainApi;
import com.m2049r.xmrwallet.xmrto.network.VotingChainApiImpl;
import com.m2049r.xmrwallet.xmrto.network.NetworkCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

public class WalletFragment extends Fragment
        implements CandidateInfoAdapter.OnInteractionListener {
    private CandidateInfoAdapter adapter;

    private List<CandidateInfo> candidateInfoLists;

    private NumberFormat formatter = NumberFormat.getInstance();

    private TextView tvStreetView;
    private LinearLayout llBalance;
    private FrameLayout flExchange;
    private TextView tvBalance;
    private TextView tvUnconfirmedAmount;
    private TextView tvProgress;
    private ImageView ivSynced;
    private ProgressBar pbProgress;
    private Button bReceive;
    private Button bSend;

    private RecyclerView recyclerView = null;

    private Spinner sCurrency;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        candidateInfoLists = new ArrayList<>();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (activityCallback.hasWallet())
            inflater.inflate(R.menu.wallet_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        tvStreetView = view.findViewById(R.id.tvStreetView);
        llBalance = view.findViewById(R.id.llBalance);
        flExchange = view.findViewById(R.id.flExchange);
        ((ProgressBar) view.findViewById(R.id.pbExchange)).getIndeterminateDrawable().
                setColorFilter(getResources().getColor(R.color.trafficGray),
                        android.graphics.PorterDuff.Mode.MULTIPLY);

        tvProgress = view.findViewById(R.id.tvProgress);
        pbProgress = view.findViewById(R.id.pbProgress);
        tvBalance = view.findViewById(R.id.tvBalance);
        showBalance(Helper.getDisplayAmount(0));
//        tvUnconfirmedAmount = view.findViewById(R.id.tvUnconfirmedAmount);
        showUnconfirmed(0);
        ivSynced = view.findViewById(R.id.ivSynced);

        // sCurrency = view.findViewById(R.id.sCurrency);
        // ArrayAdapter currencyAdapter = ArrayAdapter.createFromResource(getContext(), R.array.currency, R.layout.item_spinner_balance);
        // currencyAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_item);
        // sCurrency.setAdapter(currencyAdapter);

        // bSend = view.findViewById(R.id.bSend);
 //       bReceive = view.findViewById(R.id.bReceive);

        recyclerView = view.findViewById(R.id.list);

        getVotingChainApi("candidate/getall").call("", new NetworkCallback() {
            @Override
            public void onSuccess(JSONArray array) {
                Timber.d("getVotingChainApi - candidate getall success");
                try {
                for (int i = 0; i < array.length(); i++){

                    JSONObject jo = array.getJSONObject(i);

                    CandidateInfo candidate = new CandidateInfo(jo.getString("name"), jo.getString("lastname"),
                            jo.getString("address"));
                    candidateInfoLists.add(candidate);

                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            // Stuff that updates the UI
                            adapter = new CandidateInfoAdapter(candidateInfoLists, getActivity());
                            recyclerView.setAdapter(adapter);

                            activityCallback.forceUpdate();
                        }
                    });


                }
                }
                catch (JSONException ex) {
                    Timber.d(ex.getLocalizedMessage());
                }
            }

            @Override
            public void onSuccess(JSONObject jsonObject) {
                Timber.d("getVotingChainApi - candidate getall success");
            }

            @Override
            public void onError(Exception ex) {
                Timber.d("getVotingChainApi - candidate getall error");
                ex.printStackTrace();
            }
        });

            //if(this.balance <= 0.5)
            //    recyclerView.setVisibility(View.INVISIBLE);
            //else
            //    recyclerView.setVisibility(View.VISIBLE);



        // bSend.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         activityCallback.onSendRequest();
        //     }
        // });
        // bReceive.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         activityCallback.onWalletReceive();
        //     }
        // });

        // sCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        //     @Override
        //     public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        //         refreshBalance();
        //     }

        //     @Override
        //     public void onNothingSelected(AdapterView<?> parentView) {
        //         // nothing (yet?)
        //     }
        // });

        if (activityCallback.isSynced()) {
            onSynced();
        }

        activityCallback.forceUpdate();

        return view;
    }

    void showBalance(String balance) {
        showBalance(balance, "");
    }

    void showBalance(String balance, String walletName) {

        float balanceNum = 0;
        int voted = -1;
        SharedPreferences sp = null;

        if(walletName != "" && getContext() != null) {
            sp = getContext().getApplicationContext().getSharedPreferences(walletName, MODE_PRIVATE);
            voted = sp.getInt("voted", -1);
        }

        try {
            balanceNum = Float.valueOf(balance);

            if(voted == 0 && balanceNum < 1.0) {
                tvBalance.setText("Profilo non abilitato");
                if(recyclerView != null)
                    recyclerView.setVisibility(View.INVISIBLE);
            }
            else if(voted == 0 && balanceNum >= 1.0) {
                tvBalance.setText("Profilo abilitato\nSeleziona il candidato:");
                if(recyclerView != null)
                    recyclerView.setVisibility(View.VISIBLE);
            }
            else if(voted == 1) {
                tvBalance.setText("Voto espresso correttamente!");
                SharedPreferences.Editor edit = sp.edit();
                edit.putInt("voted", 2);
                edit.apply();
                if(recyclerView != null)
                    recyclerView.setVisibility(View.INVISIBLE);
            }
            else if(voted == 2) {
                tvBalance.setText("Hai già espresso il tuo voto!");
                if(recyclerView != null)
                    recyclerView.setVisibility(View.INVISIBLE);
            }
            else {
                tvBalance.setText("Verifica profilo in corso...");
                if(recyclerView != null)
                    recyclerView.setVisibility(View.INVISIBLE);
            }

        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }

        if (!activityCallback.isStreetMode()) {
            llBalance.setVisibility(View.VISIBLE);
            tvStreetView.setVisibility(View.INVISIBLE);
        } else {
            llBalance.setVisibility(View.INVISIBLE);
            tvStreetView.setVisibility(View.VISIBLE);
        }
    }

    void showUnconfirmed(double unconfirmedAmount) {
        // if (!activityCallback.isStreetMode()) {
        //     String unconfirmed = Helper.getFormattedAmount(unconfirmedAmount, true);
        //     tvUnconfirmedAmount.setText(getResources().getString(R.string.xmr_unconfirmed_amount, unconfirmed));
        // } else {
        //     tvUnconfirmedAmount.setText(null);
        // }
    }

    void updateBalance() {
        if (isExchanging) return; // wait for exchange to finish - it will fire this itself then.
        // at this point selection is XMR in case of error
        String displayB;
        double amountA = Double.parseDouble(Wallet.getDisplayAmount(unlockedBalance)); // crash if this fails!
        if (!Helper.CRYPTO.equals(balanceCurrency)) { // not XMR
            double amountB = amountA * balanceRate;
            displayB = Helper.getFormattedAmount(amountB, false);
        } else { // XMR
            displayB = Helper.getFormattedAmount(amountA, true);
        }
        showBalance(displayB);
    }

    String balanceCurrency = Helper.CRYPTO;
    double balanceRate = 1.0;

    private final ExchangeApi exchangeApi = Helper.getExchangeApi();

    void refreshBalance() {
        double unconfirmedXmr = Double.parseDouble(Helper.getDisplayAmount(balance - unlockedBalance));
        showUnconfirmed(unconfirmedXmr);
        if (sCurrency.getSelectedItemPosition() == 0) { // XMR
            double amountXmr = Double.parseDouble(Wallet.getDisplayAmount(unlockedBalance)); // assume this cannot fail!
            showBalance(Helper.getFormattedAmount(amountXmr, true));
        } else { // not XMR
            String currency = (String) sCurrency.getSelectedItem();
            Timber.d(currency);
            if (!currency.equals(balanceCurrency) || (balanceRate <= 0)) {
                showExchanging();
                exchangeApi.queryExchangeRate(Helper.CRYPTO, currency,
                        new ExchangeCallback() {
                            @Override
                            public void onSuccess(final ExchangeRate exchangeRate) {
                                if (isAdded())
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            exchange(exchangeRate);
                                        }
                                    });
                            }

                            @Override
                            public void onError(final Exception e) {
                                Timber.e(e.getLocalizedMessage());
                                if (isAdded())
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            exchangeFailed();
                                        }
                                    });
                            }
                        });
            } else {
                updateBalance();
            }
        }
    }

    boolean isExchanging = false;

    void showExchanging() {
        isExchanging = true;
        tvBalance.setVisibility(View.GONE);
        flExchange.setVisibility(View.VISIBLE);
        sCurrency.setEnabled(false);
    }

    void hideExchanging() {
        isExchanging = false;
        tvBalance.setVisibility(View.VISIBLE);
        flExchange.setVisibility(View.GONE);
        sCurrency.setEnabled(true);
    }

    public void exchangeFailed() {
        sCurrency.setSelection(0, true); // default to XMR
        double amountXmr = Double.parseDouble(Wallet.getDisplayAmount(unlockedBalance)); // assume this cannot fail!
        showBalance(Helper.getFormattedAmount(amountXmr, true));
        hideExchanging();
    }

    public void exchange(final ExchangeRate exchangeRate) {
        hideExchanging();
        if (!Helper.CRYPTO.equals(exchangeRate.getBaseCurrency())) {
            Timber.e("Not XMR");
            sCurrency.setSelection(0, true);
            balanceCurrency = Helper.CRYPTO;
            balanceRate = 1.0;
        } else {
            int spinnerPosition = ((ArrayAdapter) sCurrency.getAdapter()).getPosition(exchangeRate.getQuoteCurrency());
            if (spinnerPosition < 0) { // requested currency not in list
                Timber.e("Requested currency not in list %s", exchangeRate.getQuoteCurrency());
                sCurrency.setSelection(0, true);
            } else {
                sCurrency.setSelection(spinnerPosition, true);
            }
            balanceCurrency = exchangeRate.getQuoteCurrency();
            balanceRate = exchangeRate.getRate();
        }
        updateBalance();
    }

    // Callbacks from CandidateInfoAdapter
    @Override
    public void onInteraction(final View view, final CandidateInfo infoItem) {
        activityCallback.onTxDetailsRequest(infoItem);
    }

    // called from activity

    public void onRefreshed(final Wallet wallet, final boolean full) {
        Timber.d("onRefreshed(%b)", full);

        showBalance(String.valueOf(wallet.getUnlockedBalance()), wallet.getName());
        // if (full) {
        //     List<CandidateInfo> list = new ArrayList<>();
        //     final long streetHeight = activityCallback.getStreetModeHeight();
        //     Timber.d("StreetHeight=%d", streetHeight);
        //     for (CandidateInfo info : wallet.getHistory().getAll()) {
        //         Timber.d("TxHeight=%d", info.blockheight);
        //         if (info.isPending || (info.blockheight >= streetHeight)) list.add(info);
        //     }
        //     adapter.setInfos(list);
        //     adapter.notifyDataSetChanged();
        // }
        // updateStatus(wallet);
    }

    private VotingChainApi vcApi = null;


    private final VotingChainApi getVotingChainApi(String relativePath) {
        if (vcApi == null) {
            synchronized (this) {
                if (vcApi == null) {
                    vcApi = new VotingChainApiImpl(OkHttpHelper.getOkHttpClient(),
                            Helper.getVotingChainBaseUrl(relativePath));
                }
            }
        }
        return vcApi;
    }

    static JSONObject createRequest(final String name, final String address) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("address", address);
        return jsonObject;
    }

    public void onSynced() {
        if (!activityCallback.isWatchOnly()) {
            // bSend.setVisibility(View.VISIBLE);
            // bSend.setEnabled(true);
        }
        if (isVisible()) enableAccountsList(true); //otherwise it is enabled in onResume()
    }

    boolean walletLoaded = false;

    public void onLoaded() {
        walletLoaded = true;
        showReceive();
    }

    private void showReceive() {
        if (walletLoaded) {
            // bReceive.setVisibility(View.VISIBLE);
            // bReceive.setEnabled(true);
        }
    }

    private String syncText = null;

    public void setProgress(final String text) {
        syncText = text;
        tvProgress.setText(text);
    }

    private int syncProgress = -1;

    public void setProgress(final int n) {
        syncProgress = n;
        if (n > 100) {
            pbProgress.setIndeterminate(true);
            pbProgress.setVisibility(View.VISIBLE);
        } else if (n >= 0) {
            pbProgress.setIndeterminate(false);
            pbProgress.setProgress(n);
            pbProgress.setVisibility(View.VISIBLE);
        } else { // <0
            pbProgress.setVisibility(View.INVISIBLE);
        }
    }

    void setActivityTitle(Wallet wallet) {
        if (wallet == null) return;
        walletTitle = wallet.getName();
        String watchOnly = (wallet.isWatchOnly() ? getString(R.string.label_watchonly) : "");
        walletSubtitle = wallet.getAccountLabel();
        activityCallback.setTitle(walletTitle, walletSubtitle);
        Timber.d("wallet title is %s", walletTitle);
    }

    private long firstBlock = 0;
    private String walletTitle = null;
    private String walletSubtitle = null;
    private long unlockedBalance = 0;
    private long balance = 0;

    private int accountIdx = -1;

    private void updateStatus(Wallet wallet) {
        if (!isAdded()) return;
        Timber.d("updateStatus()");
        if ((walletTitle == null) || (accountIdx != wallet.getAccountIndex())) {
            accountIdx = wallet.getAccountIndex();
            setActivityTitle(wallet);
        }
        balance = wallet.getBalance();
        unlockedBalance = wallet.getUnlockedBalance();
        refreshBalance();
        String sync = "";
        if (!activityCallback.hasBoundService())
            throw new IllegalStateException("WalletService not bound.");
        Wallet.ConnectionStatus daemonConnected = activityCallback.getConnectionStatus();
        if (daemonConnected == Wallet.ConnectionStatus.ConnectionStatus_Connected) {
            if (!wallet.isSynchronized()) {
                long daemonHeight = activityCallback.getDaemonHeight();
                long walletHeight = wallet.getBlockChainHeight();
                long n = daemonHeight - walletHeight;
                sync = getString(R.string.status_syncing) + " " + formatter.format(n) + " " + getString(R.string.status_remaining);
                if (firstBlock == 0) {
                    firstBlock = walletHeight;
                }
                int x = 100 - Math.round(100f * n / (1f * daemonHeight - firstBlock));
                if (x == 0) x = 101; // indeterminate
                setProgress(x);
                ivSynced.setVisibility(View.GONE);
            } else {
                sync = getString(R.string.status_synced) + " " + formatter.format(wallet.getBlockChainHeight());
                ivSynced.setVisibility(View.VISIBLE);
            }
        } else {
            sync = getString(R.string.status_wallet_connecting);
            setProgress(101);
        }
        setProgress(sync);
        // TODO show connected status somewhere
    }

    Listener activityCallback;

    // Container Activity must implement this interface
    public interface Listener {
        boolean hasBoundService();

        void forceUpdate();

        Wallet.ConnectionStatus getConnectionStatus();

        long getDaemonHeight(); //mBoundService.getDaemonHeight();

        void onSendRequest();

        void onTxDetailsRequest(CandidateInfo info);

        boolean isSynced();

        boolean isStreetMode();

        long getStreetModeHeight();

        boolean isWatchOnly();

        String getTxKey(String txId);

        void onWalletReceive();

        boolean hasWallet();

        Wallet getWallet();

        void setToolbarButton(int type);

        void setTitle(String title, String subtitle);

        void setSubtitle(String subtitle);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            this.activityCallback = (Listener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
        activityCallback.setTitle(walletTitle, walletSubtitle);
        //activityCallback.setToolbarButton(Toolbar.BUTTON_CLOSE); // TODO: Close button somewhere else
        activityCallback.setToolbarButton(Toolbar.BUTTON_NONE);
        setProgress(syncProgress);
        setProgress(syncText);
        showReceive();
        if (activityCallback.isSynced()) enableAccountsList(true);
    }

    @Override
    public void onPause() {
        enableAccountsList(false);
        super.onPause();
    }

    public interface DrawerLocker {
        void setDrawerEnabled(boolean enabled);
    }

    private void enableAccountsList(boolean enable) {
        if (activityCallback instanceof DrawerLocker) {
            ((DrawerLocker) activityCallback).setDrawerEnabled(enable);
        }
    }

}
