package com.mumu.utils;

import android.net.Uri;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * Created by luliang on 5/17/15.
 */
public class CommonUtils {

    public static void draweeSupportGif(SimpleDraweeView drawee, String url) {
        Uri uri = Uri.parse(url);
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri).build();

        DraweeController controller = Fresco.newDraweeControllerBuilder().setImageRequest(request).setAutoPlayAnimations(true).build();
        drawee.setController(controller);
    }
}
