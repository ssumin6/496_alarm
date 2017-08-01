package kaist.alarm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by q on 2017-07-28.
 */

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback{
    ItemTouchHelperListener listener;

    private Paint p = new Paint();
    private Context mContext;

    public ItemTouchHelperCallback(ItemTouchHelperListener listener, Context context){
        this.listener = listener;
        mContext = context;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            final int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        }else if (((RecyclerViewAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).isGroup){
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            Toast.makeText(mContext, "그룹 알람은 삭제할 수 없습니다.",Toast.LENGTH_SHORT).show();
            return makeMovementFlags(dragFlags, 0 );
        } else {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = ItemTouchHelper.START;
            return makeMovementFlags(dragFlags, swipeFlags);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

    }


    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target){
        return (listener.onItemMove(source.getAdapterPosition(), target.getAdapterPosition()));
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction){
        Log.i("ITEM","SWIPED");
        listener.onItemRemove(viewHolder.getAdapterPosition());

        //left 왠지 모르겠지만 잘 안됨
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
        Bitmap icon;
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
            View itemView =  viewHolder.itemView;
            float height = (float) itemView.getBottom() - (float) itemView.getTop();
            float width = height /3;
            if (dX <=0){
                p.setColor(Color.parseColor("#D32F2F"));
                RectF background = new RectF((float) itemView.getRight()+dX, (float)itemView.getTop(), (float)itemView.getRight(),(float)itemView.getBottom());
                c.drawRect(background,p);
                icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_delete_white);
                RectF icon_dest = new RectF((float) itemView.getRight() -2*width, (float)itemView.getTop()+width, (float) itemView.getRight()- width, (float)itemView.getBottom() - width);
                c.drawBitmap(icon, null, icon_dest, p);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

}

interface ItemTouchHelperListener{
    boolean onItemMove(int fromPosition, int toPosition);
    void onItemRemove(int position);
}
