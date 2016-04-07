/**************************************************************************************************
 * Copyright (c) 2016-present, Produvia, LTD.
 * All rights reserved.
 * This source code is licensed under the MIT license
 **************************************************************************************************/
package produvia.com.scanner;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * A list fragment representing a list of light services.
 */
public class DevicesFragment extends Fragment implements CustomRecyclerAdapter.CustomListCallbacks {


    private View mLoadingProgressBar;

    static Callbacks mCallbacks;
    private RecyclerView mRecyclerView;
    private CustomRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView mProgressMessage;







    private RelativeLayout mLinearLayout;
    private void expand() {
        //set Visible
        mLinearLayout.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mLinearLayout.measure(widthSpec, heightSpec);

        ValueAnimator mAnimator = slideAnimator(0, mLinearLayout.getMeasuredHeight());
        mAnimator.start();
    }


    private void collapse() {
        if(mLinearLayout == null)
            return;
        int finalHeight = mLinearLayout.getHeight();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to GONE
                mLinearLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

        });
        mAnimator.start();
    }

    private ValueAnimator slideAnimator(int start, int end) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mLinearLayout.getLayoutParams();
                layoutParams.height = value;
                mLinearLayout.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    @Override
    public void onItemClicked(CustomListItem item, View v, int position) {
        if(!(item instanceof DeviceCard))
            return;

        mLinearLayout = (RelativeLayout) v.findViewById(R.id.details);

        if(mLinearLayout != null && mLinearLayout.getVisibility()== View.GONE) {
            item.showDetails(true);
            expand();
        }
        else {
            item.showDetails(false);
            collapse();
        }


        if(item instanceof DeviceCard){
            item.onClick();
            notifyDataSetChanged();
            return;
        }
        if(mCallbacks != null)
            mCallbacks.onItemSelected(item, v, position);


    }

    @Override
    public void onToggleClicked(CustomListItem toggleItem, boolean value) {

    }



    private void notifyDataSetChanged(){
        if(mAdapter == null)
            return;
        new Thread(){
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }
        }.start();
    }


    @Override
    public void onLeftImageClicked(CustomListItem item,View v, int position) {
        onItemClicked(item, v,position );
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected(CustomListItem hub, View v, int position);
        void onViewCreated(CustomRecyclerAdapter adapter);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(CustomListItem c, View v, int position) {
        }

        @Override
        public void onViewCreated(CustomRecyclerAdapter adapter) {

        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DevicesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        mLoadingProgressBar = view.findViewById(R.id.custom_horizontal_progressbar);
        mProgressMessage = ((TextView)view.findViewById(R.id.progressbar_message));
        mProgressMessage.setText("Scanning...");


        mRecyclerView = (RecyclerView)view.findViewById(R.id.categorylist);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter:
        mAdapter = new CustomRecyclerAdapter(mRecyclerView, getActivity(), DevicesActivity.mDevices);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (DevicesActivity.mDevices != null && DevicesActivity.mDevices.size() > 0)
                    mLoadingProgressBar.setVisibility(View.GONE);

            }

        });
        mRecyclerView.setAdapter(mAdapter);

        mCallbacks.onViewCreated(mAdapter);
        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks.onViewCreated(null);
        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onPause() {
        mAdapter.mCallbacks = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.mCallbacks = this;
        showError();
    }

    public void showError(){
        if(!DevicesActivity.mErrorOccurred)
            return;
        View view = getView();
        if( view == null)
            return;
        TextView pmessage = ((TextView) view.findViewById(R.id.progressbar_message));
        ImageView lights_not_found = (ImageView) view.findViewById(R.id.lights_not_found);
        View progress_spinner = view.findViewById(R.id.progressbar);
        if (pmessage != null) {
            pmessage.setText(DevicesActivity.mErrorMessage);
            pmessage.setTextSize(20);
        }
        if (progress_spinner != null)
            progress_spinner.setVisibility(View.GONE);

        if (lights_not_found != null)
            lights_not_found.setVisibility(View.VISIBLE);

    }


}
