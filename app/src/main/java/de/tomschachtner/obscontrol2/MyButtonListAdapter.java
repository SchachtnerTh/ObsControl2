package de.tomschachtner.obscontrol2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class MyButtonListAdapter extends RecyclerView.Adapter<MyButtonListAdapter.ViewHolder> {

    private String[] mData;
    private LayoutInflater mInflater;
    private OnItemClickListener mClickListener;

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
     *
     * @param context System context (Activity)
     * @param data Values to be shown in the RecyclerView
     */
    public MyButtonListAdapter(Context context, String[] data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
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
     * the class definition (here: <MyButtonListAdapter.ViewHolder>) and then automatically creates
     * an instance of that class for every element in the data source that the adapter encounters.)
     *
     * @param parent
     * @param viewType
     * @return The newly inflated ViewHolder
     */
    @NonNull
    @NotNull
    @Override
    public MyButtonListAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.buttongrid, parent, false);
        return new ViewHolder(view);
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
    public void onBindViewHolder(@NonNull @NotNull MyButtonListAdapter.ViewHolder holder, int position) {
        holder.myTextView.setText(mData[position]);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mData.length;
    }

    String getItem(int id) {
        return mData[id];
    }

    /**
     * Here, we populate the class member mClickListener. This property holds an object which
     * implements the OnItemClickListener. This method is called from the main activity and provides
     * an argument which object to use as that listener
     *
     * @param itemClickListener is set by the calling object to whatever object that shall be the
     *                          listener.
     */
    void setClickListener(MyButtonListAdapter.OnItemClickListener itemClickListener) {
        this.mClickListener = (OnItemClickListener) itemClickListener;
    }

    /**
     * Here, we define the OnItemClickListener interface. This interface makes sure, that "everyone"
     * who "claims to be" an OnItemClickListener, really implements the OnItemClick method.
     * This is needed by the OnClickListener in the ViewHolder class, as there, the OnItemClick
     * method is invoked in an object implementing the OnItemClickListener interface.
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
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
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

}
