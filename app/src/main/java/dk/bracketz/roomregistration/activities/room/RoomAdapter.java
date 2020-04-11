package dk.bracketz.roomregistration.activities.room;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dk.bracketz.roomregistration.R;
import dk.bracketz.roomregistration.model.Room;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.MyViewHolder> {

    private final List<Room> rooms ;
    private RoomAdapter.OnItemClickListener onItemClickListener;

    public RoomAdapter(List<Room> rooms) {
        this.rooms = rooms;
    }

    @NonNull
    @Override
    public RoomAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.room_row, parent, false);
        //View v = makeView(parent.getContext());
        Log.d("MyTag", v.toString());
        RoomAdapter.MyViewHolder vh = new RoomAdapter.MyViewHolder(v);
        Log.d("MyTag", "onCreateViewHolder called");
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RoomAdapter.MyViewHolder holder, int position) {
        Room dataItem = rooms.get(position);
        holder.nameView.setText(dataItem.getName());
        holder.descriptionView.setText(dataItem.getDescription());
        holder.capacityView.setText(dataItem.getCapacity().toString());

        Log.d("MyTag", "onBindViewHolder called " + position);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // https://www.javatpoint.com/android-recyclerview-list-example
        final TextView nameView, descriptionView, capacityView;

        MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.room_row_name);
            descriptionView = itemView.findViewById(R.id.room_row_description);
            capacityView = itemView.findViewById(R.id.room_row_capacity);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, getAdapterPosition(), rooms.get(getAdapterPosition()));
            }
        }
    }

    public void setOnItemClickListener(RoomAdapter.OnItemClickListener itemClickListener) {
        this.onItemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Room reservation);
    }
}
