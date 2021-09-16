package com.lucasrivaldo.cloneifood.helper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.lucasrivaldo.cloneifood.R;


import dmax.dialog.SpotsDialog;

public class AlertDialogUtil {

    // PAYMENT TYPE STATIC STRINGS
    public static final CharSequence[] PAY_TYPES  = new String[]{
            "Card", "Cash", "Pix"
    };

    public interface ReturnAlertData {
        void returnFilterData(String text, String requestCode);
    }

    public static void deleteTransactionAlert(Context context,
                                       DialogInterface.OnClickListener posiBtn,
                                       DialogInterface.OnClickListener negBtn){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("Delete transaction:");
        alertDialog.setMessage("Are you sure, you want to delete this transaction?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("YES", posiBtn);
        alertDialog.setNegativeButton("NO", negBtn);

        AlertDialog alert =  alertDialog.create();
        alert.show();
    }

    public static void permissionValidationAlert(Activity activity){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Permissions denied:");
        builder.setMessage("To keep using the App, you need to accept the Requested permissions.");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirm",
                (dialog, which) -> SystemPermissions.validatePermissions(activity, 1));
        builder.setNegativeButton("Deny", (dialogInterface, i) -> activity.finish());

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static android.app.AlertDialog progressDialogAlert(Activity activity, String message){

        return new SpotsDialog.Builder()
                .setContext(activity)
                .setCancelable(false)
                .setMessage(message)
                .build();
    }

    public static void editTextDialog
            (Activity activity, String type, ReturnAlertData returnOnClick){

        View editLayout = activity.getLayoutInflater().inflate(R.layout.layout_edit, null);
        EditText editText = editLayout.findViewById(R.id.editTextLayout);
        editText.setHint(type);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Write a "+type+":");
        builder.setView(editLayout);
        builder.setPositiveButton("Confirm",
                (dialogInterface, i) ->{
                    String text = editText.getText().toString();

                    if (IfoodHelper.invalidText(text)) {

                        Toast.makeText(activity, "Empty Field", Toast.LENGTH_SHORT).show();
                        AlertDialog dialog = builder.create();
                        dialog.show();


                    } else {

                        returnOnClick.returnFilterData(text, type);

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void confirmOrderDataAlert(Activity activity,
                                             DialogInterface.OnClickListener clickListener,
                                             String restName, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Confirm your "+restName+" order:\n");
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton("Confirm", clickListener);
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void confirmOrderAlert(Activity activity,
                                         DialogInterface.OnClickListener clickListener,
                                         DialogInterface.OnCancelListener cancelListener){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Accept requested order:");
        builder.setMessage("If you're available to serve this customer press \"Confirm\" to confirm,"
                            + " or \"Cancel\" to cancel and finish this order. ");
        builder.setCancelable(true);
        builder.setPositiveButton("Confirm", clickListener);
        if (cancelListener!=null) builder.setOnCancelListener(cancelListener);
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }


    public static void selectPaymentTypeAlert(Activity activity,
                                              String restName,
                                              ReturnAlertData returnOnClick){

        View editLayout = activity.getLayoutInflater().inflate(R.layout.layout_edit, null);
        EditText editText = editLayout.findViewById(R.id.editTextLayout);
        editText.setHint("Observations:");

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(editLayout);
        builder.setTitle("Select your "+restName+" payment method:");

        builder.setSingleChoiceItems(PAY_TYPES, 0, (dialogInterface, i) -> {

            String obs = editText.getText().toString();
            returnOnClick.returnFilterData(obs, ""+i);

            dialogInterface.dismiss();

        });

        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }
}