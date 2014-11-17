package hhl3eq.virginia.edu.soundlights;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

/**
 * Created by lovo-h on 11/15/2014.
 */
public class Tools {
    public static int convertPXtoDP(Resources r, int dim) {
        /* converts px to dp*/
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dim,
                r.getDisplayMetrics()
        );
    }

    public static String prepareJSON(int choice) {
        String json = null;
        switch (choice) {
            case 0:
                json = "{\"lights\": [{\"lightId\": 0, \"red\":0,\"green\":0,\"blue\":255, \"intensity\": 0.5}],\"propagate\": true}";
                break;
            case 1:
                json = "{ \"lights\": [  {\"lightId\": 1, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 3, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 5, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 7, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 9, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 11, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 13, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 15, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 17, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 19, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 21, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 23, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 25, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 27, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 29, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 31, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}],  \"propagate\": false }";
                break;
            case 2:
                json = "{\"lights\": [{\"lightId\": 0, \"red\":0,\"green\":255,\"blue\":0, \"intensity\": 0.5}],\"propagate\": true}";
                break;
            case -1:
                json = "{\"lights\": [{\"lightId\": 0, \"red\":255,\"green\":0,\"blue\":0, \"intensity\": 0.9}],\"propagate\": true}";
                break;
        }
        return json;
    }

    public static void expand(final View v) {
        /* used to expand the menu view */
        v.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        final int targetWidth = v.getMeasuredWidth();

        v.getLayoutParams().width = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().width = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int) (targetWidth * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetWidth / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        /* used to collapse the menu view */
        final int initialWidth = v.getMeasuredWidth();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().width = initialWidth - (int) (initialWidth * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialWidth / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
}
