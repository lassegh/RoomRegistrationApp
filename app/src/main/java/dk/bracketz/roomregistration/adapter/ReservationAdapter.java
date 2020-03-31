package dk.bracketz.roomregistration.adapter;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import dk.bracketz.roomregistration.R;
import dk.bracketz.roomregistration.model.Reservation;
import dk.bracketz.roomregistration.model.User;
import dk.bracketz.roomregistration.restconsuming.ApiUtils;
import dk.bracketz.roomregistration.restconsuming.ModelService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.MyViewHolder> {

    private static List<Reservation> reservations ;
    private ReservationAdapter.OnItemClickListener onItemClickListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.GERMAN);

    public ReservationAdapter(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public boolean deleteItem(int position) {
        Reservation mRecentlyDeletedItem = reservations.get(position);
        if (User.getInstance().isSomeoneLoggedIn() && mRecentlyDeletedItem.getUserId().equals(User.getInstance().firebaseUser.getEmail())){
            reservations.remove(position);
            notifyItemRemoved(position);
            ModelService modelStoreService = ApiUtils.getReservationService();
            Call<Void> deleteResponse = modelStoreService.deleteReservation(mRecentlyDeletedItem.getId());
            deleteResponse.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        User.getInstance().checkUserChoice();
                        Log.d("response",response.code()+"");
                    } else {
                        Log.d("response","Response unsuccessful");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("Error",t.getMessage());
                }
            });
            return true;
        }
        else {
            this.notifyItemChanged(position);
            return false;
        }
    }

    @NonNull
    @Override
    public ReservationAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.reservation_row, parent, false);
        //View v = makeView(parent.getContext());
        Log.d("MyTag", v.toString());
        ReservationAdapter.MyViewHolder vh = new ReservationAdapter.MyViewHolder(v);
        Log.d("MyTag", "onCreateViewHolder called");
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Reservation dataItem = reservations.get(position);
        holder.purposeView.setText(dataItem.getPurpose());
        holder.userView.setText(dataItem.getUserId());
        holder.fromtimeView.setText(dateFormat.format(dataItem.getFromTime()));
        holder.totimeView.setText(dateFormat.format(dataItem.getToTime()));

        Log.d("MyTag", "onBindViewHolder called " + position);
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // https://www.javatpoint.com/android-recyclerview-list-example
        final TextView purposeView, userView, fromtimeView, totimeView;

        MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            purposeView = itemView.findViewById(R.id.reservation_row_purpose);
            userView = itemView.findViewById(R.id.reservation_row_user);
            fromtimeView = itemView.findViewById(R.id.reservation_fromtime);
            totimeView = itemView.findViewById(R.id.reservation_totime);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, getAdapterPosition(), reservations.get(getAdapterPosition()));
            }
        }
    }

    public void setOnItemClickListener(ReservationAdapter.OnItemClickListener itemClickListener) {
        this.onItemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Reservation reservation);
    }


}


