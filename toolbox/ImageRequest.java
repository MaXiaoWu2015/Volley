/*     */ package com.android.volley.toolbox;
/*     */ 
/*     */ import android.graphics.Bitmap;
/*     */ import android.graphics.Bitmap.Config;
/*     */ import android.graphics.BitmapFactory;
/*     */ import android.graphics.BitmapFactory.Options;
/*     */ import android.widget.ImageView.ScaleType;
/*     */ import com.android.volley.DefaultRetryPolicy;
/*     */ import com.android.volley.NetworkResponse;
/*     */ import com.android.volley.ParseError;
/*     */ import com.android.volley.Request;
/*     */ import com.android.volley.Request.Priority;
/*     */ import com.android.volley.Response;
/*     */ import com.android.volley.Response.ErrorListener;
/*     */ import com.android.volley.Response.Listener;
/*     */ import com.android.volley.VolleyLog;
/*     */ 
/*     */ public class ImageRequest extends Request<Bitmap>
/*     */ {
/*     */   private static final int IMAGE_TIMEOUT_MS = 1000;
/*     */   private static final int IMAGE_MAX_RETRIES = 2;
/*     */   private static final float IMAGE_BACKOFF_MULT = 2.0F;
/*     */   private final Response.Listener<Bitmap> mListener;
/*     */   private final Bitmap.Config mDecodeConfig;
/*     */   private final int mMaxWidth;
/*     */   private final int mMaxHeight;
/*     */   private ImageView.ScaleType mScaleType;
/*  52 */   private static final Object sDecodeLock = new Object();
/*     */ 
/*     */   public ImageRequest(String url, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight, ImageView.ScaleType scaleType, Bitmap.Config decodeConfig, Response.ErrorListener errorListener)
/*     */   {
/*  74 */     super(0, url, errorListener);
/*  75 */     setRetryPolicy(
/*  76 */       new DefaultRetryPolicy(1000, 2, 2.0F));
/*  77 */     this.mListener = listener;
/*  78 */     this.mDecodeConfig = decodeConfig;
/*  79 */     this.mMaxWidth = maxWidth;
/*  80 */     this.mMaxHeight = maxHeight;
/*  81 */     this.mScaleType = scaleType;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public ImageRequest(String url, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight, Bitmap.Config decodeConfig, Response.ErrorListener errorListener)
/*     */   {
/*  92 */     this(url, listener, maxWidth, maxHeight, 
/*  92 */       ImageView.ScaleType.CENTER_INSIDE, decodeConfig, errorListener);
/*     */   }
/*     */ 
/*     */   public Request.Priority getPriority() {
/*  96 */     return Request.Priority.LOW;
/*     */   }
/*     */ 
/*     */   private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary, ImageView.ScaleType scaleType)
/*     */   {
/* 115 */     if ((maxPrimary == 0) && (maxSecondary == 0)) {
/* 116 */       return actualPrimary;
/*     */     }
/*     */ 
/* 120 */     if (scaleType == ImageView.ScaleType.FIT_XY) {
/* 121 */       if (maxPrimary == 0) {
/* 122 */         return actualPrimary;
/*     */       }
/* 124 */       return maxPrimary;
/*     */     }
/*     */ 
/* 128 */     if (maxPrimary == 0) {
/* 129 */       double ratio = maxSecondary / actualSecondary;
/* 130 */       return (int)(actualPrimary * ratio);
/*     */     }
/*     */ 
/* 133 */     if (maxSecondary == 0) {
/* 134 */       return maxPrimary;
/*     */     }
/*     */ 
/* 137 */     double ratio = actualSecondary / actualPrimary;
/* 138 */     int resized = maxPrimary;
/*     */ 
/* 141 */     if (scaleType == ImageView.ScaleType.CENTER_CROP) {
/* 142 */       if (resized * ratio < maxSecondary) {
/* 143 */         resized = (int)(maxSecondary / ratio);
/*     */       }
/* 145 */       return resized;
/*     */     }
/*     */ 
/* 148 */     if (resized * ratio > maxSecondary) {
/* 149 */       resized = (int)(maxSecondary / ratio);
/*     */     }
/* 151 */     return resized;
/*     */   }
/*     */ 
/*     */   protected Response<Bitmap> parseNetworkResponse(NetworkResponse response)
/*     */   {
/*     */     Object Ljava/lang/Object;;
/* 157 */     monitorenter;
/*     */     try {
/* 159 */       return doParse(response);
/*     */     } catch (OutOfMemoryError e) {
/* 161 */       VolleyLog.e("Caught OOM for %d byte image, url=%s", new Object[] { Integer.valueOf(response.data.length), getUrl() });
/* 162 */       return Response.error(new ParseError(e));
/*     */     }
/*     */     finally
/*     */     {
/* 157 */       monitorexit;
/*     */     }
/*     */   }
/*     */ 
/*     */   private Response<Bitmap> doParse(NetworkResponse response)
/*     */   {
/* 171 */     byte[] data = response.data;
/* 172 */     BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
/* 173 */     Bitmap bitmap = null;
/* 174 */     if ((this.mMaxWidth == 0) && (this.mMaxHeight == 0)) {
/* 175 */       decodeOptions.inPreferredConfig = this.mDecodeConfig;
/* 176 */       bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
/*     */     }
/*     */     else {
/* 179 */       decodeOptions.inJustDecodeBounds = true;
/* 180 */       BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
/* 181 */       int actualWidth = decodeOptions.outWidth;
/* 182 */       int actualHeight = decodeOptions.outHeight;
/*     */ 
/* 185 */       int desiredWidth = getResizedDimension(this.mMaxWidth, this.mMaxHeight, 
/* 186 */         actualWidth, actualHeight, this.mScaleType);
/* 187 */       int desiredHeight = getResizedDimension(this.mMaxHeight, this.mMaxWidth, 
/* 188 */         actualHeight, actualWidth, this.mScaleType);
/*     */ 
/* 191 */       decodeOptions.inJustDecodeBounds = false;
/*     */ 
/* 194 */       decodeOptions.inSampleSize = 
/* 195 */         findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
/* 196 */       Bitmap tempBitmap = 
/* 197 */         BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
/*     */ 
/* 200 */       if ((tempBitmap != null) && (((tempBitmap.getWidth() > desiredWidth) || 
/* 201 */         (tempBitmap.getHeight() > desiredHeight)))) {
/* 202 */         bitmap = Bitmap.createScaledBitmap(tempBitmap, 
/* 203 */           desiredWidth, desiredHeight, true);
/* 204 */         tempBitmap.recycle();
/*     */       } else {
/* 206 */         bitmap = tempBitmap;
/*     */       }
/*     */     }
/*     */ 
/* 210 */     if (bitmap == null) {
/* 211 */       return Response.error(new ParseError(response));
/*     */     }
/* 213 */     return Response.success(bitmap, HttpHeaderParser.parseCacheHeaders(response));
/*     */   }
/*     */ 
/*     */   protected void deliverResponse(Bitmap response)
/*     */   {
/* 219 */     this.mListener.onResponse(response);
/*     */   }
/*     */ 
/*     */   static int findBestSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight)
/*     */   {
/* 234 */     double wr = actualWidth / desiredWidth;
/* 235 */     double hr = actualHeight / desiredHeight;
/* 236 */     double ratio = Math.min(wr, hr);
/* 237 */     float n = 1.0F;
/* 238 */     while (n * 2.0F <= ratio) {
/* 239 */       n *= 2.0F;
/*     */     }
/*     */ 
/* 242 */     return (int)n;
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.ImageRequest
 * JD-Core Version:    0.5.4
 */