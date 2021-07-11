package de.tomschachtner.obscontrol.util;

public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemSwiped(int position);
}
