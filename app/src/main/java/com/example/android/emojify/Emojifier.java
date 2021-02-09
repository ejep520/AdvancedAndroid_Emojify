package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class Emojifier {

    private final static String LOG_TAG = "Emojifier";
    private final static double SMILING_PROB_THRESHOLD = 0.15;
    private final static double EYE_OPEN_PROB_THRESHOLD = 0.5;
     // private final static float EMOJI_SCALE_FACTOR = 0.9f;

    public static Bitmap detectFaces(Context context, Bitmap bitmap) {
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);
        detector.release();
        Bitmap returnVal = bitmap;
        if (faces.size() == 0) {
            Toast.makeText(context, R.string.no_faces_message, Toast.LENGTH_SHORT).show();
        } else {
            for (int counter = 0; counter < faces.size(); counter++) {
                Face face = faces.valueAt(counter);
                returnVal = whichEmoji(context, face, returnVal);
            }
        }
        return returnVal;
    }

    private static Bitmap whichEmoji(@NonNull Context context, @NonNull Face face,
                                     @NonNull Bitmap inPic) {
        float smilingProb = face.getIsSmilingProbability();
        float leftOpenProb = face.getIsLeftEyeOpenProbability();
        float rightOpenProb = face.getIsRightEyeOpenProbability();
        Log.d(LOG_TAG, "whichEmoji: smilingProb = " + smilingProb);
        Log.d(LOG_TAG, "whichEmoji: leftOpenProb = " + leftOpenProb);
        Log.d(LOG_TAG, "whichEmoji: rightOpenProb = " + rightOpenProb);

        boolean smiling = smilingProb > SMILING_PROB_THRESHOLD;
        boolean leftEyeClosed = leftOpenProb < EYE_OPEN_PROB_THRESHOLD;
        boolean rightEyeClosed = rightOpenProb < EYE_OPEN_PROB_THRESHOLD;

        Emoji emoji;
        Bitmap emojiPic;

        if (smiling) {
            if (leftEyeClosed && !rightEyeClosed) {
                emoji = Emoji.LEFT_WINK;
                emojiPic = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.leftwink);
            } else if (rightEyeClosed && !leftEyeClosed) {
                emoji = Emoji.RIGHT_WINK;
                emojiPic = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.rightwink);
            } else if (leftEyeClosed) {
                emoji = Emoji.CLOSED_EYE_SMILE;
                emojiPic = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.closed_smile);
            } else {
                emoji = Emoji.SMILE;
                emojiPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smile);
            }
        } else {
            if (leftEyeClosed && !rightEyeClosed) {
                emoji = Emoji.LEFT_WINK_FROWN;
                emojiPic = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.leftwinkfrown);
            } else if (rightEyeClosed && !leftEyeClosed) {
                emoji = Emoji.RIGHT_WINK_FROWN;
                emojiPic = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.rightwinkfrown);
            } else if (leftEyeClosed) {
                emoji = Emoji.CLOSED_EYE_FROWN;
                emojiPic = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.closed_frown);
            } else {
                emoji = Emoji.FROWN;
                emojiPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.frown);
            }
        }

        Log.d(LOG_TAG, "whichEmoji: " + emoji.name());

        return addBitmapToFace(inPic, emojiPic, face);
    }

    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap,
                                          Face face) {
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // float scaleFactor = EMOJI_SCALE_FACTOR;

        float emojiPositionX = (face.getPosition().x + face.getWidth() / 2.0f) -
                emojiBitmap.getWidth() / 2.0f;
        float emojiPositionY = (face.getPosition().y + face.getHeight() / 2.0f) -
                emojiBitmap.getHeight() / 3.0f;

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }
}
