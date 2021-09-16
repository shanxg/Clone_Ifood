package com.lucasrivaldo.cloneifood.helper;

import android.content.Context;
import android.graphics.Color;

import com.lucasrivaldo.cloneifood.R;

import static com.lucasrivaldo.cloneifood.helper.AlertDialogUtil.PAY_TYPES;

public class IfoodHelper {

    public interface DataListener{
        void onReturnData(Object... values);
    }

    // MY STATIC LOG TAGS
    public static final String TAG = "USER_TEST_ERROR";
    public static final String TEST = "USER_TEST";

    // ORDER STATUS STATIC VALUES
    public static final String CANCELLED  = "cancelled";
    public static final String PENDING  = "pending";
    public static final String CONFIRMED  = "confirmed";
    public static final String READY  = "ready";
    public static final String FINALIZED  = "finalized";

    public static String getPayType(String payType){
        int i = Integer.parseInt(payType);

        return String.valueOf(PAY_TYPES[i]);
    }

    public static int getStatusColor(Context context, String status){

        if (PENDING.equals(status)) {
            return context.getResources().getColor(R.color.myLightRed);
        }else if (CONFIRMED.equals(status)){
            return context.getResources().getColor(android.R.color.black);
        }else if (READY.equals(status)) {
            return context.getResources().getColor(R.color.myGreen);
        }else
            return context.getResources().getColor(android.R.color.secondary_text_light);
    }

    public static boolean invalidText(String text){
        return (text == null || text.isEmpty());
    }


}

/** ####################################  INITIALIZE  #################################### **/
/** ##################################  CLICK LISTENERS  ################################## **/
/** ####################################  LISTENERS  ##################################### **/
/** ####################################  MY METHODS  #################################### **/
/** ###############################  ACTIVITY LIFE-CYCLE  ################################ **/
/** #################################  ACTIVITY PROCESS  ################################## **/
/** ####################################  HELPERS  #################################### **/




/** ##############################  PERSONAL TESTS AND UTILITIES  ################################
 *
 *   // RADIO BUTTON COLOR CONTROLLER METHOD
 *
 *     private void controlRadioButtonsColor() {
 *         if (Build.VERSION.SDK_INT >= 21) {
 *             ColorStateList colorStateList = new ColorStateList(
 *                     new int[][]{
 *                             new int[]{-android.R.attr.state_enabled}, // Disabled
 *                             new int[]{android.R.attr.state_enabled}   // Enabled
 *                     } , new int[]{
 *                             Color.BLACK, // disabled
 *                             Color.BLUE   // enabled
 *                     });
 *
 *             radio.setButtonTintList(colorStateList); // set the color tint list
 *             radio.invalidate(); // Could not be necessary
 *         }
 *     }
 *
 *
 *     // ANOTHER FORMAT METHOD I'VE TESTED ( WORKS FINE )
 *
 *     public static String myFormatPrice(double price){
 *
 *         DecimalFormat moneyFormat = new DecimalFormat("0.00");
 *
 *         String currencyFormat = "R$ "+moneyFormat.format(price);
 *         String finalPrice = currencyFormat.replaceAll("\\.", ",");
 *
 *         return finalPrice;
 *     }
 *
 *
 * **/