package com.example.expensetrackerapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Helper {
    // Get image resource from arrays.xml using the index
    public static int getImageResourceId(int arraysXmlIndex, Context context) {
        int defaultResourceId = R.drawable.ic_default_category; // if category img not found
        // Get access to arrays.xml
        try (TypedArray images = context.getResources().obtainTypedArray(R.array.image_array)) {
            int resourceId = images.getResourceId(arraysXmlIndex, -1); // -1 is default if out of bounds
            if (resourceId >= 0) {
                return resourceId;
            }
            else {
                return defaultResourceId;
            }
        } catch (Exception e) {
            Log.e("Helper", "getImageResourceId: failed to get images array", e);
        }
        return defaultResourceId;
    }

    public static List<Integer> getImageResourceIds(Context context) {
        List<Integer> imgResourceIds = new ArrayList<>();
        // Get access to arrays.xml
        try (TypedArray images = context.getResources().obtainTypedArray(R.array.image_array)) {
            for (int i = 0; i < images.length(); i ++) {
                int resourceId = images.getResourceId(i, -1); // -1 is default if out of bounds
                if (resourceId >= 0) {
                    imgResourceIds.add(resourceId);
                }
            }
        } catch (Exception e) {
            Log.e("Helper", "getImageResourceIds: failed to get images array", e);
        }
        return imgResourceIds;
    }

    /**
     * returns the index inside arrays.xml representing the default category icon
     * @param context used for retrieving array
     * @return the index of the default icon or -1 if failed to retrieve.
     */
    public static int getDefaultImgIndex(Context context) {
        int imgIndex = -1;
        try (TypedArray images = context.getResources().obtainTypedArray(R.array.image_array)) {
            imgIndex = images.length()-1;
        }
        catch (Exception e) {
            Log.e("Helper", "getDefaultImgIndex: failed to retrieve image_array", e);
        }
        return -1;
    }


}
