package com.mumu.utils;

import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * Created by luliang on 5/17/15.
 */
public class CommonUtils {

    public static void draweeSupportGif(SimpleDraweeView drawee, String url) {
        int width = 150, height = 150;
        Uri uri = Uri.parse(url);
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(width, height))
                .build();

//        DraweeController controller = Fresco.newDraweeControllerBuilder()
//                .setImageRequest(request)
//                .setAutoPlayAnimations(true)
//                .build();
//        drawee.setController(controller);

        PipelineDraweeController controller2 = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(drawee.getController())
                .setAutoPlayAnimations(true)
                .setImageRequest(request)
                .build();
        drawee.setController(controller2);
    }

    public static int dp2px(Context context, int dp){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int)dm.density * dp;
    }
}
