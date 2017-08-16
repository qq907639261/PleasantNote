package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SelectPhotoDialogFragment extends DialogFragment implements
        View.OnClickListener {

    private OnSelectPhotoDialogFragmentListener mListener;

    public static SelectPhotoDialogFragment newInstance() {
        return new SelectPhotoDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_select_photo_dialog, container, false);

        Button takePictureButton = inflate.findViewById(R.id.takePictureButton);
        Button selectImageButton = inflate.findViewById(R.id.selectImageButton);

        takePictureButton.setOnClickListener(this);
        selectImageButton.setOnClickListener(this);

        return inflate;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectPhotoDialogFragmentListener) {
            mListener = (OnSelectPhotoDialogFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        if (mListener == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.takePictureButton:
                mListener.onTakePictureButtonClick();
                break;
            case R.id.selectImageButton:
                mListener.onSelectImageButtonClick();
                break;
            default:
        }

        getFragmentManager().beginTransaction()
                .remove(this)
                .commit();
    }

    interface OnSelectPhotoDialogFragmentListener {

        void onTakePictureButtonClick();
        void onSelectImageButtonClick();
    }
}
