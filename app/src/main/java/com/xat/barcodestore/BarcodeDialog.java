package com.xat.barcodestore;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class BarcodeDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("haha").setTitle(R.string.barcode);
        AlertDialog dialog = builder.create();
        return dialog;
    }
}