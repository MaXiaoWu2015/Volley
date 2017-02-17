/*     */ package com.android.volley.toolbox;
/*     */ 
/*     */ import android.content.Context;
/*     */ import android.text.TextUtils;
/*     */ import android.util.AttributeSet;
/*     */ import android.view.ViewGroup.LayoutParams;
/*     */ import android.widget.ImageView;
/*     */ import android.widget.ImageView.ScaleType;
/*     */ import com.android.volley.VolleyError;
/*     */ 
/*     */ public class NetworkImageView extends ImageView
/*     */ {
/*     */   private String mUrl;
/*     */   private int mDefaultImageId;
/*     */   private int mErrorImageId;
/*     */   private ImageLoader mImageLoader;
/*     */   private ImageLoader.ImageContainer mImageContainer;
/*     */ 
/*     */   public NetworkImageView(Context context)
/*     */   {
/*  53 */     this(context, null);
/*     */   }
/*     */ 
/*     */   public NetworkImageView(Context context, AttributeSet attrs) {
/*  57 */     this(context, attrs, 0);
/*     */   }
/*     */ 
/*     */   public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
/*  61 */     super(context, attrs, defStyle);
/*     */   }
/*     */ 
/*     */   public void setImageUrl(String url, ImageLoader imageLoader)
/*     */   {
/*  77 */     this.mUrl = url;
/*  78 */     this.mImageLoader = imageLoader;
/*     */ 
/*  80 */     loadImageIfNecessary(false);
/*     */   }
/*     */ 
/*     */   public void setDefaultImageResId(int defaultImage)
/*     */   {
/*  88 */     this.mDefaultImageId = defaultImage;
/*     */   }
/*     */ 
/*     */   public void setErrorImageResId(int errorImage)
/*     */   {
/*  96 */     this.mErrorImageId = errorImage;
/*     */   }
/*     */ 
/*     */   void loadImageIfNecessary(boolean isInLayoutPass)
/*     */   {
/* 104 */     int width = getWidth();
/* 105 */     int height = getHeight();
/* 106 */     ImageView.ScaleType scaleType = getScaleType();
/*     */ 
/* 108 */     boolean wrapWidth = false; boolean wrapHeight = false;
/* 109 */     if (getLayoutParams() != null) {
/* 110 */       wrapWidth = getLayoutParams().width == -2;
/* 111 */       wrapHeight = getLayoutParams().height == -2;
/*     */     }
/*     */ 
/* 116 */     boolean isFullyWrapContent = (wrapWidth) && (wrapHeight);
/* 117 */     if ((width == 0) && (height == 0) && (!isFullyWrapContent)) {
/* 118 */       return;
/*     */     }
/*     */ 
/* 123 */     if (TextUtils.isEmpty(this.mUrl)) {
/* 124 */       if (this.mImageContainer != null) {
/* 125 */         this.mImageContainer.cancelRequest();
/* 126 */         this.mImageContainer = null;
/*     */       }
/* 128 */       setDefaultImageOrNull();
/* 129 */       return;
/*     */     }
/*     */ 
/* 133 */     if ((this.mImageContainer != null) && (this.mImageContainer.getRequestUrl() != null)) {
/* 134 */       if (this.mImageContainer.getRequestUrl().equals(this.mUrl))
/*     */       {
/* 136 */         return;
/*     */       }
/*     */ 
/* 139 */       this.mImageContainer.cancelRequest();
/* 140 */       setDefaultImageOrNull();
/*     */     }
/*     */ 
/* 145 */     int maxWidth = (wrapWidth) ? 0 : width;
/* 146 */     int maxHeight = (wrapHeight) ? 0 : height;
/*     */ 
/* 150 */     ImageLoader.ImageContainer newContainer = this.mImageLoader.get(this.mUrl, 
/* 151 */       new ImageLoader.ImageListener(isInLayoutPass)
/*     */     {
/*     */       public void onErrorResponse(VolleyError error) {
/* 154 */         if (NetworkImageView.this.mErrorImageId != 0)
/* 155 */           NetworkImageView.this.setImageResource(NetworkImageView.this.mErrorImageId);
/*     */       }
/*     */ 
/*     */       public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate)
/*     */       {
/* 165 */         if ((isImmediate) && (this.val$isInLayoutPass)) {
/* 166 */           NetworkImageView.this.post(new Runnable(response)
/*     */           {
/*     */             public void run() {
/* 169 */               NetworkImageView.1.this.onResponse(this.val$response, false);
/*     */             }
/*     */           });
/* 172 */           return;
/*     */         }
/*     */ 
/* 175 */         if (response.getBitmap() != null)
/* 176 */           NetworkImageView.this.setImageBitmap(response.getBitmap());
/* 177 */         else if (NetworkImageView.this.mDefaultImageId != 0)
/* 178 */           NetworkImageView.this.setImageResource(NetworkImageView.this.mDefaultImageId);
/*     */       }
/*     */     }
/*     */     , maxWidth, maxHeight, scaleType);
/*     */ 
/* 184 */     this.mImageContainer = newContainer;
/*     */   }
/*     */ 
/*     */   private void setDefaultImageOrNull() {
/* 188 */     if (this.mDefaultImageId != 0) {
/* 189 */       setImageResource(this.mDefaultImageId);
/*     */     }
/*     */     else
/* 192 */       setImageBitmap(null);
/*     */   }
/*     */ 
/*     */   protected void onLayout(boolean changed, int left, int top, int right, int bottom)
/*     */   {
/* 198 */     super.onLayout(changed, left, top, right, bottom);
/* 199 */     loadImageIfNecessary(true);
/*     */   }
/*     */ 
/*     */   protected void onDetachedFromWindow()
/*     */   {
/* 204 */     if (this.mImageContainer != null)
/*     */     {
/* 207 */       this.mImageContainer.cancelRequest();
/* 208 */       setImageBitmap(null);
/*     */ 
/* 210 */       this.mImageContainer = null;
/*     */     }
/* 212 */     super.onDetachedFromWindow();
/*     */   }
/*     */ 
/*     */   protected void drawableStateChanged()
/*     */   {
/* 217 */     super.drawableStateChanged();
/* 218 */     invalidate();
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.NetworkImageView
 * JD-Core Version:    0.5.4
 */