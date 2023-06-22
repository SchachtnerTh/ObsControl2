package de.tomschachtner.obscontrol;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.tomschachtner.obscontrol.obsdata.OBSAudioSource;
import de.tomschachtner.obscontrol.obsdata.ObsTransitionsList;

public class OBSAudioSourcesSlidersAdapter
        extends RecyclerView.Adapter<OBSAudioSourcesSlidersAdapter.ViewHolder>
        implements OBSWebSocketClient.ObsAudioChangedListener {

    //TODO: Determine the structure of the audioSourcesList structure
    public ArrayList<OBSAudioSource> mData;
    private LayoutInflater mInflater;

    //TODO: Change listeners to match the sliders' messages
    private OnMuteButtonChangedListener mMuteButtonChangedListener;
    private OnVolumeChangedListener mVolumeChangedListener;
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
    public OBSAudioSourcesSlidersAdapter(Context context, ArrayList<OBSAudioSource> data) {
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
    public OBSAudioSourcesSlidersAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.volume_slider, parent, false);
        return new OBSAudioSourcesSlidersAdapter.ViewHolder(view);
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
    public void onBindViewHolder(@NonNull @NotNull OBSAudioSourcesSlidersAdapter.ViewHolder holder, int position) {
        holder.audioSourceName.setText(mData.get(position).name);
        holder.toggleMuteButton.setText("");
        holder.toggleMuteButton.setChecked(mData.get(position).isMuted);
        holder.volumeBar.setMax(1000);
        holder.volumeBar.setMin(0);
        holder.volumeBar.setProgress(mData.get(position).volume);
        holder.volumeBar.setTag(mData.get(position).name);
        holder.toggleMuteButton.setOnClickListener(holder);
        holder.volumeBar.setOnSeekBarChangeListener(holder);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    String getItem(int id) {
        return mData.get(id).name;
    }

    void setVolumeChangedListener(OnVolumeChangedListener volumeChangedListener) {
        this.mVolumeChangedListener = volumeChangedListener;
    }

    void setMuteButtonChangedListener(OnMuteButtonChangedListener muteButtonChangedListener) {
        this.mMuteButtonChangedListener = muteButtonChangedListener;
    }

    @Override
    public void onObsAudioChanged(ArrayList<OBSAudioSource> audioSources) {
        ((MainActivity)ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mData = audioSources;
                //notifyDataSetChanged();
            }
        });
    }

    public interface OnMuteButtonChangedListener {
        void onMuteButtonChanged(View view, int position);
    }

    public interface OnVolumeChangedListener {
        void onVolumeChanged(View view, int volume, boolean fromUser, int position);
        void onVolumeStartTracking(View view, int position);
        void onVolumeStopTracking(View view, int position);
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
            implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
        // Here, we define the Views which are part of one item in a RecyclerView layout
        TextView audioSourceName;
        ToggleButton toggleMuteButton;
        SeekBar volumeBar;


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
            audioSourceName = itemView.findViewById(R.id.audio_source_name);
            toggleMuteButton = itemView.findViewById(R.id.toggleMuteButton);
            volumeBar = itemView.findViewById(R.id.volumeBar);
            // The view gets an OnClickListener attached in order to react upon clicks.
            // as the ViewHolder class implements the interface View.OnClickListener, the
            // class itself can be used as the listener (this)
            toggleMuteButton.setOnClickListener(this);
            volumeBar.setOnSeekBarChangeListener(this);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int volume, boolean fromUser) {
            int position = getAdapterPosition();
            if (mVolumeChangedListener != null) mVolumeChangedListener.onVolumeChanged(seekBar, volume, fromUser, getAdapterPosition());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (mVolumeChangedListener != null) mVolumeChangedListener.onVolumeStartTracking(seekBar, getAdapterPosition());
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mVolumeChangedListener != null) mVolumeChangedListener.onVolumeStopTracking(seekBar, getAdapterPosition());
        }

        @Override
        public void onClick(View view) {
            if (mMuteButtonChangedListener != null) mMuteButtonChangedListener.onMuteButtonChanged(view, getAdapterPosition());
        }

    }



}