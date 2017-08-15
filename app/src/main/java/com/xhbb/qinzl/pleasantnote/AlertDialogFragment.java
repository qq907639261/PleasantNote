package com.xhbb.qinzl.pleasantnote;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class AlertDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    private static final String ARG_MESSAGE = "ARG_MESSAGE";
    private static final String ARG_TITLE = "ARG_TITLE";

    private OnAlertDialogFragmentListener mListener;

    public static AlertDialogFragment newInstance(CharSequence message, CharSequence title) {
        Bundle args = new Bundle();
        args.putCharSequence(ARG_MESSAGE, message);
        args.putCharSequence(ARG_TITLE, title);

        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence message = getArguments().getCharSequence(ARG_MESSAGE);
        CharSequence title = getArguments().getCharSequence(ARG_TITLE);

        return new AlertDialog.Builder(getContext())
                .setIcon(R.drawable.ic_alert_dialog)
                .setMessage(message)
                .setTitle(title)
                .setPositiveButton(R.string.confirm_button, this)
                .setNegativeButton(R.string.cancel_button, null)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (mListener != null) {
            mListener.onDialogPositiveButtonClick();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAlertDialogFragmentListener) {
            mListener = (OnAlertDialogFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    interface OnAlertDialogFragmentListener {

        void onDialogPositiveButtonClick();
    }
}
