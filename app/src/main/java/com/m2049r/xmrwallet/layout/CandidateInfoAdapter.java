package com.m2049r.xmrwallet.layout;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.fragment.send.SendFragment;
import com.m2049r.xmrwallet.model.CandidateInfo;
import com.m2049r.xmrwallet.util.Helper;

import com.m2049r.xmrwallet.data.TxData;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.model.PendingTransaction;

import java.util.List;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;


public class CandidateInfoAdapter extends RecyclerView.Adapter<CandidateInfoAdapter.ViewHolder> {


    public static final String KEY_NAME = "name";
    public static final String KEY_LASTNAME = "lastname";
    public static final String KEY_ADDRESS = "address";

    // we define a list from the CandidateInfo java class

    private List<CandidateInfo> candidateInfoList;
    private Context context;

    public interface OnInteractionListener {
        void onInteraction(View view, CandidateInfo item);
    }

    public CandidateInfoAdapter(List<CandidateInfo> candidateInfoList, Context context) {

        // generate constructors to initialise the List and Context objects

        this.candidateInfoList = candidateInfoList;
        this.context = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // this method will be called whenever our ViewHolder is created
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_candidate_btn, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        // this method will bind the data to the ViewHolder from whence it'll be shown to other Views

        final CandidateInfo candidateInfo = candidateInfoList.get(position);
        holder.name.setText(candidateInfo.getName());
        holder.lastname.setText(candidateInfo.getLastname());
        holder.address.setText(candidateInfo.getAddress());
        holder.voting.setText("Stiamo trasmettendo il tuo voto per " + candidateInfo.getName() + " " + candidateInfo.getLastname() + "...");
        holder.linearLayout.setSelected(holder.linearLayout.isSelected() ? true : false);
        holder.linearLayout.setPressed(holder.linearLayout.isPressed() ? true : false);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                holder.linearLayout.setBackgroundColor(Color.parseColor("#ff6105"));
                holder.voting.setVisibility(View.VISIBLE);
                Wallet myWallet = WalletManager.getInstance().getWallet();
                long minBalance = 1000000000000L;
                Timber.w("Voting Chain Log:: Check getUnlockedBalance::" + myWallet.getUnlockedBalance());
                if(myWallet.getUnlockedBalance() >= minBalance) {
                    CandidateInfo candidateInfo1 = candidateInfoList.get(position);
                    TxData txData = new TxData(
                            candidateInfo1.getAddress(),
                            "", //paymentId
                            Wallet.getAmountFromString("1.0"), //amount - type long,
                            SendFragment.MIXIN, //mixin,
                            PendingTransaction.Priority.Priority_Default //amount
                    );

                    PendingTransaction pendingTransaction = myWallet.createTransaction(txData);
                    boolean success = pendingTransaction.commit("", true);
                    if (success) {
                        myWallet.disposePendingTransaction();

                        // save data into share SharePreference
                        SharedPreferences sp = context.getSharedPreferences(myWallet.getName(), MODE_PRIVATE);
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putInt("voted", 1);
                        edit.apply();

                        boolean rc = myWallet.store();
                        if (!rc) {
                            Timber.w("Wallet store failed: %s", myWallet.getErrorString());
                        }
                        notifyDataSetChanged();
                    }
                    else {
                        holder.voting.setText("Errore nella trasmissione del voto!");
                        Timber.w("Voting Chain Log:: No success on sending transaction.\nError: " + pendingTransaction.getErrorString());
                        Timber.w("Voting Chain Log:: Amount of getUnlockedBalance::" + myWallet.getUnlockedBalance());
                    }

                    // Intent skipIntent = new Intent(v.getContext(), ProfileActivity.class);
                    // skipIntent.putExtra(KEY_NAME, candidateInfo1.getName());
                    // skipIntent.putExtra(KEY_LASTNAME, candidateInfo1.getLastname());
                    // skipIntent.putExtra(KEY_ADDRESS, candidateInfo1.getAddress());
                    // v.getContext().startActivity(skipIntent);
                }
                else {
                    holder.voting.setText("Errore nella trasmissione del voto!");
                    Timber.w("Not enough money to make transaction");
                }
            }
        });

    }

    @Override

    //return the size of the listItems (candidateInfo)

    public int getItemCount() {
        return candidateInfoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {

        // define the View objects

        public TextView name;
        public TextView lastname;
        public TextView address;
        public TextView voting;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            // initialize the View objects

            name = (TextView) itemView.findViewById(R.id.candidate_name);
            lastname = (TextView) itemView.findViewById(R.id.candidate_lastname);
            address = (TextView) itemView.findViewById(R.id.candidate_address);
            voting = (TextView) itemView.findViewById(R.id.candidate_voting);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.candidate_linearLayout);
        }

    }
}
