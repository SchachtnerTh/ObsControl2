package de.tomschachtner.obscontrol;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import de.tomschachtner.obscontrol.obsdata.ObsTransitionsList;

public class OBSTransitionsButtonsAdapter
            extends RecyclerView.Adapter<de.tomschachtner.obscontrol.OBSTransitionsButtonsAdapter.ViewHolder>
            implements OBSWebSocketClient.ObsTransitionsChangedListener {

    private ObsTransitionsList mData;
    private LayoutInflater mInflater;
    private OnTransitionsClickListener mClickListener;
    private OnTransitionsLongClickListener mLongClickListener;
    private Context ctx;

    /**
     * The constructor creates the adapter object.
     * An adapter is a design pattern which connects some view element (here a RecyclerView) to a
     * data source, like an array or a database.
     *
     * During construction of a class instance, we obtain a reference to the LayoutInflater, as that
     * will be used later to inflate the child layouts for all the elements.
     *
     * We also get the list of data (here: an array) which should be included in the RecyclerView
     * This is also saved as an instance property for later use.
     *  @param context System context (Activity)
     * @param data Values to be shown in the RecyclerView
     */
    public OBSTransitionsButtonsAdapter(Context context, ObsTransitionsList data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.ctx = context;
    }

    /**
     * The onCreateViewHolder override makes sure to really inflate the object using the corres-
     * ponding layout XML file. The method "inflate" does exactly that. The corresponding inflater
     * is already available at Activity level and can be obtained via the static method
     * LayoutInflater.from(). This has already be done in the constructor and the inflater was saved
     * as a class property for later re-use.
     *
     * (I think, that here is, where the magic happens: in onCreateViewHolder, the framework
     * recognizes with type of ViewHolder is to be used (by looking at the template argument from
     * the class definition (here: <OBSSceneButtonsAdapter.ViewHolder>) and then automatically creates
     * an instance of that class for every element in the data source that the adapter encounters.)
     *
     * @param parent (unknown)
     * @param viewType (unknown)
     * @return The newly inflated ViewHolder
     */
    @NonNull
    @NotNull
    @Override
    public de.tomschachtner.obscontrol.OBSTransitionsButtonsAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.buttongrid, parent, false);
        return new OBSTransitionsButtonsAdapter.ViewHolder(view);
    }

/**
     * In the onBindViewHolder, the contents of the ViewHolder's views are filled with actual values
     * from the underlying dataset.
     *
     * @param holder provides access to the instance of the ViewHolder that is currently being
     *               processed and which should be filled with values.
     * @param position indicates at which position in the RecyclerView we currently are. This should
     *                 help determining which element of the dataset should be used to fill the
     *                 ViewHolder's views with. (That might me database IDs or just array indices.)
     */
    @Override
    public void onBindViewHolder(@NonNull @NotNull de.tomschachtner.obscontrol.OBSTransitionsButtonsAdapter.ViewHolder holder, int position) {
        holder.myTextView.setText(mData.transitions.get(position));
        if (mData.transitions.get(position).equals(mData.getCurrentTransition())) {
//            holder.myTextView.setBackgroundColor(Color.rgb(0xaa, 0xaa, 0xff));
            holder.myTextView.setBackgroundResource(R.drawable.active_transition);
            holder.myTextView.setTextColor(Color.DKGRAY);
        } else {
//            holder.myTextView.setBackgroundColor(Color.rgb(0x00, 0x00, 0xff));
            holder.myTextView.setBackgroundResource(R.drawable.non_active_transition);
            holder.myTextView.setTextColor(Color.BLACK);
        }
        holder.myTextView.setOnClickListener(holder);
        holder.myTextView.setOnLongClickListener(holder);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mData.transitions.size();
    }

    String getItem(int id) {
        return mData.transitions.get(id);
    }

    /**
     * Here, we populate the class member mClickListener. This property holds an object which
     * implements the OnItemClickListener. This method is called from the main activity and provides
     * an argument which object to use as that listener
     *
     * @param itemClickListener is set by the calling object to whatever object that shall be the
     *                          listener.
     */
    void setTransitionsClickListener(OnTransitionsClickListener transitionsClickListener) {
        this.mClickListener = transitionsClickListener;
    }

    void setTransitionsLongClickListener(OnTransitionsLongClickListener transitionsLongClickListener) {
        this.mLongClickListener = transitionsLongClickListener;
    }

    @Override
    public void onObsTransitionsChanged(ObsTransitionsList obsTransitionsList) {
        //mData = obsScenesList;
        ((MainActivity)ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mData = obsTransitionsList;
                notifyDataSetChanged();
            }
        });

    }

    /**
     * Here, we define the OnSceneClickListener interface. This interface makes sure, that "everyone"
     * who "claims to be" an OnSceneClickListener, really implements the onSceneClick method.
     * This is needed by the OnSceneClickListener in the ViewHolder class, as there, the onSceneClick
     * method is invoked in an object implementing the OnSceneClickListener interface.
     */
    public interface OnTransitionsClickListener {
        void onTransitionClick(View view, int position);
    }

    public interface OnTransitionsLongClickListener {
        void onTransitionLongClick(View view, int position);
    }

    /**
     * Using the ViewHolder pattern in Android allows for a smoother scrolling and for less
     * looking-up views and loading and inflating them.
     * The below class represents exactly one item in the RecyclerView which - in turn - can consist
     * of multiple Views. All the views are regarded as one item by the RecyclerView.
     * For more details,
     * @see https://stackoverflow.com/questions/21501316/what-is-the-benefit-of-viewholder-pattern-in-android
     *
     */
    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        // Here, we define the Views which are part of one item in a RecyclerView layout
        TextView myTextView;

        /**
         * The constructor creates an instance of the class.
         * It is called for each and every item in the RecyclerView.
         * First, it calls its super-constructor, then it binds the Views of the ViewHolder
         * to class variables to allow for value binding later in the callbacks...
         *
         * @param itemView is filled by the framework with the actual View to be instantiated
         */
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.info_text);
            // The view gets an OnClickListener attached in order to react upon clicks.
            // as the ViewHolder class implements the interface View.OnClickListener, the
            // class itself can be used as the listener (this)
            itemView.setOnClickListener(this);
        }

        /**
         * This override is called when a View is clicked, as its OnClickListener is set to the
         * current class, this class implements the View.OnClickListener interface and this
         * interface guarantees that a method onClick(View view) is provided. This is this very
         * method...
         *
         * In this case, the onClick callback does not really handle the onClick event.
         * It rather forwards it to another event handler... (Things are getting confusing here...)
         *
         * @param view Filled by the framework with the View that has been clicked and whose click
         *             has caused the event handler to be fired.
         */
        @Override
        public void onClick(View view) {
            // if the variable mClickListener really contains a valid OnClickListener,
            // that OnClickListener is called with the View that was clicked and the position
            // in the list where the user clicked. (This information is not normally available
            // in a standard OnClickListener, but added there for conveniently working with the
            // list.
            if (mClickListener != null) mClickListener.onTransitionClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (mLongClickListener != null) mLongClickListener.onTransitionLongClick(view, getAdapterPosition());
            return true;
        }
    }



}
